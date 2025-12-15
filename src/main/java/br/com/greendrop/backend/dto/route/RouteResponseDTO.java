package br.com.greendrop.backend.dto.route;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record RouteResponseDTO(
        UUID id,
        String name,
        LocalDate routeDate,
        UUID collectorId,
        String status,
        Instant expectedStartTime,
        Instant expectedEndTime,
        List<RouteStopDTO> stops
) {}