package br.com.greendrop.backend.dto.route;

import java.util.UUID;

public record RouteStopUpdateDTO(
        UUID stopId,
        boolean markDone,
        String failureType,
        String failureReason,
        String notes
) {}