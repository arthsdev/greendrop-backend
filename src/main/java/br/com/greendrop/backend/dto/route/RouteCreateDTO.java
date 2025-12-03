package br.com.greendrop.backend.dto.route;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record RouteCreateDTO(
        String name,
        LocalDate routeDate,
        UUID collectorId,
        List<UUID> collectionRequestIds,
        Instant expectedStartTime,
        Instant expectedEndTime
) {}
