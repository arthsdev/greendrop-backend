package br.com.greendrop.backend.domain.service.route;

import br.com.greendrop.backend.domain.model.*;
import br.com.greendrop.backend.domain.model.enums.*;
import br.com.greendrop.backend.domain.repository.*;
import br.com.greendrop.backend.domain.service.route.authorization.RouteAuthorizationService;
import br.com.greendrop.backend.domain.service.route.rules.RouteBusinessRulesService;
import br.com.greendrop.backend.domain.service.route.validation.RouteValidationService;
import br.com.greendrop.backend.dto.route.*;
import br.com.greendrop.backend.exception.generic.BadRequestException;
import br.com.greendrop.backend.exception.global.ErrorCode;
import br.com.greendrop.backend.exception.user.ResourceNotFoundException;
import br.com.greendrop.backend.mapper.route.RouteMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteService {

    private static final Logger log = LoggerFactory.getLogger(RouteService.class);

    private final RouteRepository routeRepository;
    private final RouteStopRepository routeStopRepository;
    private final CollectionRequestRepository collectionRequestRepository;
    private final UserRepository userRepository;
    private final RouteAuthorizationService authorization;
    private final RouteValidationService validation;
    private final RouteBusinessRulesService rules;
    private final RouteMapper mapper;

    // -------------------------
    // CREATE ROUTE
    // -------------------------
    @Transactional
    public RouteResponseDTO createRoute(RouteCreateDTO dto) {
        authorization.requireAdmin();
        validation.validateCreateDtoNonBusiness(dto.name(), dto.routeDate(), dto.collectionRequestIds());

        List<UUID> requestIds = Optional.ofNullable(dto.collectionRequestIds()).orElse(Collections.emptyList());
        checkCollectionRequestsExist(requestIds);
        checkCollectorExists(dto.collectorId());

        Route route = routeRepository.save(Route.builder()
                .id(UUID.randomUUID())
                .name(dto.name())
                .routeDate(dto.routeDate())
                .collectorId(dto.collectorId())
                .status(RouteStatus.PLANNED)
                .expectedStartTime(dto.expectedStartTime())
                .expectedEndTime(dto.expectedEndTime())
                .build());

        List<RouteStop> stops = new ArrayList<>();
        for (int i = 0; i < requestIds.size(); i++)
            stops.add(rules.buildNewStop(route, requestIds.get(i), i));
        routeStopRepository.saveAll(stops);
        route.setStops(stops);

        log.info("Route {} created by {}", route.getId(), getCurrentUserId());
        return mapper.toResponse(route);
    }

    private void checkCollectionRequestsExist(List<UUID> ids) {
        Set<UUID> found = collectionRequestRepository.findAllById(ids).stream().map(CollectionRequest::getId).collect(Collectors.toSet());
        List<UUID> missing = ids.stream().filter(id -> !found.contains(id)).toList();
        if (!missing.isEmpty()) throw new BadRequestException(ErrorCode.BAD_REQUEST, "route.create.missing_requests");
    }

    private void checkCollectorExists(UUID id) {
        if (id != null && !userRepository.existsById(id))
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "collector.not_found");
    }

    // -------------------------
    // GET ROUTES
    // -------------------------
    @Transactional(readOnly = true)
    public RouteResponseDTO getRoute(UUID id) {
        Route r = getRouteOrThrow(id);
        authorization.authorizeRead(r);
        r.setStops(routeStopRepository.findByRoute_IdOrderByStopOrderAsc(id));
        return mapper.toResponse(r);
    }

    @Transactional(readOnly = true)
    public List<RouteResponseDTO> getRoutesForCollectorOnDate(UUID collectorId, LocalDate date) {
        if (collectorId == null || date == null) throw new BadRequestException(ErrorCode.BAD_REQUEST, "invalid_params");
        if (!isAdminOrSelf(collectorId)) throw new BadRequestException(ErrorCode.BAD_REQUEST, "collector.forbidden");
        List<Route> routes = routeRepository.findByRouteDateAndCollectorId(date, collectorId);
        routes.forEach(r -> r.setStops(routeStopRepository.findByRoute_IdOrderByStopOrderAsc(r.getId())));
        return routes.stream().map(mapper::toResponse).toList();
    }

    // -------------------------
    // UPDATE STOP
    // -------------------------
    @Transactional
    public RouteStopDTO updateRouteStop(RouteStopUpdateDTO dto) {
        RouteStop stop = routeStopRepository.findById(dto.stopId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ROUTE_STOP_NOT_FOUND));
        Route route = Optional.ofNullable(stop.getRoute()).orElseGet(() -> getRouteOrThrow(stop.getRouteId()));

        authorization.requireAdminOrCollector(route);
        validation.validateRouteEditable(route);

        if (applyStopUpdate(stop, dto)) {
            routeStopRepository.save(stop);
            log.info("RouteStop {} updated by {}", stop.getId(), getCurrentUserId());
        }

        List<RouteStop> stops = routeStopRepository.findByRoute_IdOrderByStopOrderAsc(route.getId());
        if (rules.applyRouteStateTransitionsOnStopUpdate(route, stops))
            routeRepository.save(route);

        return mapper.toStopResponse(stop);
    }

    private boolean applyStopUpdate(RouteStop stop, RouteStopUpdateDTO dto) {
        if (dto.markDone() && stop.getStatus() != RouteStopStatus.DONE) rules.markStopDone(stop);
        else if (dto.failureType() != null && !dto.failureType().isBlank()) rules.markStopFailed(stop, dto.failureType(), dto.failureReason(), dto.notes());
        else if (stop.getStatus() != RouteStopStatus.SKIPPED) rules.markStopSkipped(stop, dto.notes());
        else return false;
        return true;
    }

    // -------------------------
    // ASSIGN COLLECTOR & STATUS
    // -------------------------
    @Transactional
    public void assignCollector(UUID routeId, UUID collectorId) {
        authorization.requireAdmin();
        Route r = getRouteOrThrow(routeId);
        if (r.getStatus() != RouteStatus.PLANNED) throw new BadRequestException(ErrorCode.ROUTE_ALREADY_STARTED);
        checkCollectorExists(collectorId);
        r.setCollectorId(collectorId);
        routeRepository.save(r);
    }

    @Transactional
    public RouteResponseDTO changeStatus(UUID routeId, RouteStatus newStatus) {
        Route r = getRouteOrThrow(routeId);
        authorization.authorizeWrite(r);

        if (r.getStatus() == RouteStatus.COMPLETED || r.getStatus() == RouteStatus.CANCELLED)
            throw new BadRequestException(ErrorCode.ROUTE_ALREADY_FINISHED);

        List<RouteStop> stops = routeStopRepository.findByRoute_IdOrderByStopOrderAsc(r.getId());
        if (r.getStatus() == RouteStatus.PLANNED && newStatus == RouteStatus.IN_PROGRESS) validation.validateStartPossible(r, stops);
        else if (r.getStatus() == RouteStatus.IN_PROGRESS && newStatus == RouteStatus.COMPLETED) validation.validateCompletePossible(stops);

        r.setStatus(newStatus);
        r.setStops(stops);
        routeRepository.save(r);
        return mapper.toResponse(r);
    }

    // -------------------------
    // STOP MANAGEMENT
    // -------------------------
    @Transactional
    public void addStopToRoute(UUID routeId, UUID collectionRequestId, Integer pos) {
        authorization.requireAdmin();
        Route r = getRouteOrThrow(routeId);
        if (!collectionRequestRepository.existsById(collectionRequestId)) throw new BadRequestException(ErrorCode.BAD_REQUEST, "collection_request.not_found");

        List<RouteStop> stops = routeStopRepository.findByRoute_IdOrderByStopOrderAsc(routeId);
        int insertPos = (pos == null) ? stops.size() : Math.min(Math.max(pos, 0), stops.size());
        validation.validateInsertPosition(insertPos, stops.size());

        stops.stream().filter(s -> s.getStopOrder() >= insertPos).forEach(s -> s.setStopOrder(s.getStopOrder() + 1));
        routeStopRepository.saveAll(stops);

        routeStopRepository.save(rules.buildNewStop(r, collectionRequestId, insertPos));
    }

    @Transactional
    public void removeStop(UUID stopId) {
        authorization.requireAdmin();
        RouteStop s = routeStopRepository.findById(stopId).orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ROUTE_STOP_NOT_FOUND));
        UUID routeId = s.getRouteId();
        routeStopRepository.delete(s);

        List<RouteStop> stops = routeStopRepository.findByRoute_IdOrderByStopOrderAsc(routeId);
        for (int i = 0; i < stops.size(); i++) stops.get(i).setStopOrder(i);
        routeStopRepository.saveAll(stops);
    }

    @Transactional
    public void reorderStops(UUID routeId, List<UUID> ids) {
        authorization.requireAdmin();
        if (ids == null || ids.isEmpty()) throw new BadRequestException(ErrorCode.BAD_REQUEST, "route.reorder.empty");

        List<RouteStop> stops = routeStopRepository.findByRoute_IdOrderByStopOrderAsc(routeId);
        validation.validateReorderMatches(stops, ids);

        Map<UUID, RouteStop> map = stops.stream().collect(Collectors.toMap(RouteStop::getId, s -> s));
        for (int i = 0; i < ids.size(); i++) map.get(ids.get(i)).setStopOrder(i);
        routeStopRepository.saveAll(map.values());
    }

    // -------------------------
    // HELPERS
    // -------------------------
    private Route getRouteOrThrow(UUID id) {
        return routeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ROUTE_NOT_FOUND));
    }

    private boolean isAdminOrSelf(UUID collectorId) {
        UUID current = getCurrentUserId();
        return current != null && (isCurrentUserAdmin() || current.equals(collectorId));
    }

    private UUID getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        Object p = auth.getPrincipal();
        if (p instanceof User u) return u.getId();
        if (p instanceof UserDetails ud) return userRepository.findByEmail(ud.getUsername()).map(User::getId).orElse(null);
        return null;
    }

    private boolean isCurrentUserAdmin() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;
        Object p = auth.getPrincipal();
        if (p instanceof User u) return u.getRole() == Role.ADMIN;
        if (p instanceof UserDetails ud) return userRepository.findByEmail(ud.getUsername()).map(u -> u.getRole() == Role.ADMIN).orElse(false);
        return false;
    }
}
