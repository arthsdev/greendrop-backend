package br.com.greendrop.backend.dto.route;

import java.time.Instant;
import java.util.UUID;

public record RouteStopDTO(
        UUID id,
        UUID collectionRequestId,
        Integer stopOrder,
        String status,
        Instant completedAt,
        String failureType,
        String failureReason,
        String notes
) {}
