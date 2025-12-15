package br.com.greendrop.backend.domain.service.impl;

import br.com.greendrop.backend.domain.model.CollectionRequest;
import br.com.greendrop.backend.domain.model.Route;
import br.com.greendrop.backend.domain.model.RouteStop;
import br.com.greendrop.backend.domain.model.User;
import br.com.greendrop.backend.domain.model.enums.RouteStatus;
import br.com.greendrop.backend.domain.model.enums.RouteStopStatus;
import br.com.greendrop.backend.domain.model.enums.Role;
import br.com.greendrop.backend.domain.repository.CollectionRequestRepository;
import br.com.greendrop.backend.domain.repository.RouteRepository;
import br.com.greendrop.backend.domain.repository.RouteStopRepository;
import br.com.greendrop.backend.domain.repository.UserRepository;
import br.com.greendrop.backend.domain.service.RouteService;
import br.com.greendrop.backend.domain.service.authorization.RouteAuthorizationService;
import br.com.greendrop.backend.domain.service.rules.RouteBusinessRulesService;
import br.com.greendrop.backend.domain.service.validation.RouteValidationService;
import br.com.greendrop.backend.dto.route.RouteCreateDTO;
import br.com.greendrop.backend.dto.route.RouteResponseDTO;
import br.com.greendrop.backend.dto.route.RouteStopDTO;
import br.com.greendrop.backend.dto.route.RouteStopUpdateDTO;
import br.com.greendrop.backend.exception.generic.BadRequestException;
import br.com.greendrop.backend.exception.global.ErrorCode;
import br.com.greendrop.backend.exception.user.ResourceNotFoundException;
import br.com.greendrop.backend.mapper.route.RouteMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * RouteServiceImpl — orchestrates route operations while delegating:
 *  - authorization (RouteAuthorizationService)
 *  - validation (RouteValidationService)
 *  - business rules (RouteBusinessRulesService)
 *  - mapping (RouteMapper/MapStruct)
 *
 * Purpose:
 *  - keep this class thin (orchestration only)
 *  - enable unit testing of each component in isolation
 *  - make the code easy to explain and maintain
 */
@Service
public class RouteServiceImpl implements RouteService {

    private static final Logger log = LoggerFactory.getLogger(RouteServiceImpl.class);

    private final RouteRepository routeRepository;
    private final RouteStopRepository routeStopRepository;
    private final CollectionRequestRepository collectionRequestRepository;
    private final UserRepository userRepository;
    private final RouteAuthorizationService authorization;
    private final RouteValidationService validation;
    private final RouteBusinessRulesService rules;
    private final RouteMapper mapper;

    public RouteServiceImpl(
            RouteRepository routeRepository,
            RouteStopRepository routeStopRepository,
            CollectionRequestRepository collectionRequestRepository,
            UserRepository userRepository,
            RouteAuthorizationService authorization,
            RouteValidationService validation,
            RouteBusinessRulesService rules,
            RouteMapper mapper
    ) {
        this.routeRepository = routeRepository;
        this.routeStopRepository = routeStopRepository;
        this.collectionRequestRepository = collectionRequestRepository;
        this.userRepository = userRepository;
        this.authorization = authorization;
        this.validation = validation;
        this.rules = rules;
        this.mapper = mapper;
    }

    // -------------------------
    // CREATE
    // -------------------------
    @Override
    @Transactional
    public RouteResponseDTO createRoute(RouteCreateDTO dto) {
        authorization.requireAdmin();
        validation.validateCreateDtoNonBusiness(dto.name(), dto.routeDate(), dto.collectionRequestIds());

        List<UUID> ids = Optional.ofNullable(dto.collectionRequestIds()).orElse(Collections.emptyList());

        List<CollectionRequest> found = collectionRequestRepository.findAllById(ids);
        Set<UUID> foundIds = found.stream().map(CollectionRequest::getId).collect(Collectors.toSet());
        List<UUID> missing = ids.stream().filter(id -> !foundIds.contains(id)).toList();
        if (!missing.isEmpty()) {
            log.warn("createRoute: missing collection requests {} (caller={})", missing, getCurrentUserId());
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "route.create.missing_requests");
        }

        UUID collectorId = dto.collectorId();
        if (collectorId != null && !userRepository.existsById(collectorId)) {
            log.warn("createRoute: collector not found {} (caller={})", collectorId, getCurrentUserId());
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "collector.not_found");
        }

        Route route = new Route();
        route.setId(UUID.randomUUID());
        route.setName(dto.name());
        route.setRouteDate(dto.routeDate());
        route.setCollectorId(collectorId);
        route.setStatus(RouteStatus.PLANNED);
        route.setExpectedStartTime(dto.expectedStartTime());
        route.setExpectedEndTime(dto.expectedEndTime());

        route = routeRepository.save(route);

        List<RouteStop> stops = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            stops.add(rules.buildNewStop(route, ids.get(i), i));
        }
        routeStopRepository.saveAll(stops);
        route.setStops(stops);

        log.info("Route {} created by {}", route.getId(), getCurrentUserId());
        return mapper.toResponse(route);
    }

    // -------------------------
    // READ
    // -------------------------
    @Override
    @Transactional(readOnly = true)
    public RouteResponseDTO getRoute(UUID id) {
        authorization.requireAuthenticated();

        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ROUTE_NOT_FOUND));

        authorization.authorizeRead(route);

        loadStopsForRoute(route);
        return mapper.toResponse(route);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RouteResponseDTO> getRoutesForCollectorOnDate(UUID collectorId, LocalDate date) {
        authorization.requireAuthenticated();
        if (collectorId == null) throw new BadRequestException(ErrorCode.BAD_REQUEST, "collector.id.null");
        if (date == null) throw new BadRequestException(ErrorCode.BAD_REQUEST, "date.null");

        if (!isAdminOrSelf(collectorId)) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "collector.forbidden");
        }

        List<Route> routes = routeRepository.findByRouteDateAndCollectorId(date, collectorId);
        return routes.stream()
                .peek(this::loadStopsForRoute)
                .map(mapper::toResponse)
                .toList();
    }

    // -------------------------
    // UPDATE STOP
    // -------------------------
    @Override
    @Transactional
    public RouteStopDTO updateRouteStop(RouteStopUpdateDTO dto) {
        authorization.requireAuthenticated();
        if (dto == null) throw new BadRequestException(ErrorCode.BAD_REQUEST, "stop.update.dto.null");

        RouteStop stop = routeStopRepository.findById(dto.stopId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ROUTE_STOP_NOT_FOUND));

        Route route = stop.getRoute();
        if (route == null) {
            route = routeRepository.findById(stop.getRouteId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ROUTE_NOT_FOUND));
        }

        authorization.requireAdminOrCollector(route);
        validation.validateRouteEditable(route);

        boolean changed = false;

        if (dto.markDone()) {
            if (stop.getStatus() != RouteStopStatus.DONE) {
                rules.markStopDone(stop);
                changed = true;
            }
        } else {
            if (dto.failureType() != null && !dto.failureType().isBlank()) {
                rules.markStopFailed(stop, dto.failureType(), dto.failureReason(), dto.notes());
                changed = true;
            } else {
                if (stop.getStatus() != RouteStopStatus.SKIPPED) {
                    rules.markStopSkipped(stop, dto.notes());
                    changed = true;
                }
            }
        }

        if (changed) {
            routeStopRepository.save(stop);
            log.info("RouteStop {} updated by {}", stop.getId(), getCurrentUserId());
        }

        List<RouteStop> stops = routeStopRepository.findByRouteIdOrderByStopOrderAsc(route.getId());
        boolean routeChanged = rules.applyRouteStateTransitionsOnStopUpdate(route, stops);
        if (routeChanged) {
            routeRepository.save(route);
            log.info("Route {} transitioned to {} by {}", route.getId(), route.getStatus(), getCurrentUserId());
        }

        return mapper.toStopResponse(stop);
    }

    // -------------------------
    // ASSIGN COLLECTOR
    // -------------------------
    @Override
    @Transactional
    public void assignCollector(UUID routeId, UUID collectorId) {
        authorization.requireAdmin();

        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ROUTE_NOT_FOUND));

        if (route.getStatus() != RouteStatus.PLANNED) {
            throw new BadRequestException(ErrorCode.ROUTE_ALREADY_STARTED);
        }

        if (!userRepository.existsById(collectorId)) {
            log.warn("assignCollector: collector {} not found (caller={})", collectorId, getCurrentUserId());
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "collector.not_found");
        }

        route.setCollectorId(collectorId);
        routeRepository.save(route);
        log.info("Collector {} assigned to route {} by {}", collectorId, route.getId(), getCurrentUserId());
    }

    // -------------------------
    // CHANGE STATUS
    // -------------------------
    @Override
    @Transactional
    public RouteResponseDTO changeStatus(UUID routeId, RouteStatus newStatus) {
        authorization.requireAuthenticated();

        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ROUTE_NOT_FOUND));

        authorization.authorizeWrite(route);

        RouteStatus current = route.getStatus();

        if (current == RouteStatus.COMPLETED || current == RouteStatus.CANCELLED) {
            throw new BadRequestException(ErrorCode.ROUTE_ALREADY_FINISHED);
        }

        if (current == RouteStatus.PLANNED && newStatus == RouteStatus.IN_PROGRESS) {
            List<RouteStop> stops = routeStopRepository.findByRouteIdOrderByStopOrderAsc(route.getId());
            validation.validateStartPossible(route, stops);
        }

        if (current == RouteStatus.IN_PROGRESS && newStatus == RouteStatus.COMPLETED) {
            List<RouteStop> stops = routeStopRepository.findByRouteIdOrderByStopOrderAsc(route.getId());
            validation.validateCompletePossible(stops);
        }

        route.setStatus(newStatus);
        routeRepository.save(route);

        loadStopsForRoute(route);
        log.info("Route {} status changed to {} by {}", route.getId(), newStatus, getCurrentUserId());
        return mapper.toResponse(route);
    }

    // -------------------------
    // ADMIN: stop management
    // -------------------------
    @Override
    @Transactional
    public void addStopToRoute(UUID routeId, UUID collectionRequestId, Integer position) {
        authorization.requireAdmin();

        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ROUTE_NOT_FOUND));

        if (!collectionRequestRepository.existsById(collectionRequestId)) {
            log.warn("addStopToRoute: collectionRequest {} not found (caller={})", collectionRequestId, getCurrentUserId());
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "collection_request.not_found");
        }

        List<RouteStop> stops = routeStopRepository.findByRouteIdOrderByStopOrderAsc(routeId);
        int insertPos = (position == null) ? stops.size() : Math.min(Math.max(position, 0), stops.size());
        validation.validateInsertPosition(insertPos, stops.size());

        for (RouteStop s : stops) {
            if (s.getStopOrder() >= insertPos) {
                s.setStopOrder(s.getStopOrder() + 1);
            }
        }
        routeStopRepository.saveAll(stops);

        RouteStop newStop = rules.buildNewStop(route, collectionRequestId, insertPos);
        routeStopRepository.save(newStop);
        log.info("New stop {} added to route {} by {}", newStop.getId(), route.getId(), getCurrentUserId());
    }

    @Override
    @Transactional
    public void removeStop(UUID stopId) {
        authorization.requireAdmin();

        RouteStop stop = routeStopRepository.findById(stopId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ROUTE_STOP_NOT_FOUND));

        UUID routeId = stop.getRouteId();
        routeStopRepository.delete(stop);

        List<RouteStop> stops = routeStopRepository.findByRouteIdOrderByStopOrderAsc(routeId);
        for (int i = 0; i < stops.size(); i++) {
            RouteStop s = stops.get(i);
            if (s.getStopOrder() != i) s.setStopOrder(i);
        }
        routeStopRepository.saveAll(stops);

        log.info("Stop {} removed from route {} by {}", stopId, routeId, getCurrentUserId());
    }

    @Override
    @Transactional
    public void reorderStops(UUID routeId, List<UUID> stopIdsInOrder) {
        authorization.requireAdmin();
        if (stopIdsInOrder == null || stopIdsInOrder.isEmpty()) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "route.reorder.empty");
        }

        List<RouteStop> stops = routeStopRepository.findByRouteIdOrderByStopOrderAsc(routeId);
        validation.validateReorderMatches(stops, stopIdsInOrder);

        Map<UUID, RouteStop> map = stops.stream().collect(Collectors.toMap(RouteStop::getId, s -> s));
        for (int i = 0; i < stopIdsInOrder.size(); i++) {
            UUID id = stopIdsInOrder.get(i);
            RouteStop s = map.get(id);
            s.setStopOrder(i);
        }
        routeStopRepository.saveAll(new ArrayList<>(map.values()));
        log.info("Route {} stops reordered by {}", routeId, getCurrentUserId());
    }

    // -------------------------
    // PRIVATE HELPERS
    // -------------------------
    private void loadStopsForRoute(Route route) {
        if (route == null) return;
        List<RouteStop> stops = routeStopRepository.findByRouteIdOrderByStopOrderAsc(route.getId());
        route.setStops(stops);
    }

    private boolean isAdminOrSelf(UUID collectorId) {
        UUID current = getCurrentUserId();
        if (current == null) return false;
        if (isCurrentUserAdmin()) return true;
        return Objects.equals(current, collectorId);
    }

    private UUID getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;

        Object principal = auth.getPrincipal();
        if (principal instanceof User user) {
            return user.getId();
        } else if (principal instanceof UserDetails ud) {
            return userRepository.findByEmail(ud.getUsername())
                    .map(User::getId)
                    .orElse(null);
        }
        return null;
    }

    private boolean isCurrentUserAdmin() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;

        Object principal = auth.getPrincipal();
        if (principal instanceof User user) {
            return user.getRole() == Role.ADMIN;
        } else if (principal instanceof UserDetails ud) {
            return userRepository.findByEmail(ud.getUsername())
                    .map(u -> u.getRole() == Role.ADMIN)
                    .orElse(false);
        }
        return false;
    }
}
