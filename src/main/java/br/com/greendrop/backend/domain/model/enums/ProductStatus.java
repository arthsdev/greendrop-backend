package br.com.greendrop.backend.domain.model.enums;

import java.util.EnumSet;
import java.util.Set;

public enum ProductStatus {

    PENDING,
    ASSIGNED,
    IN_TRANSIT,
    COLLECTED,
    CANCELED,
    DELETED;

    private Set<ProductStatus> allowedTransitions;

    static {
        PENDING.allowedTransitions = EnumSet.of(
                ASSIGNED,   // claim
                CANCELED,
                DELETED
        );

        ASSIGNED.allowedTransitions = EnumSet.of(
                PENDING,    // unclaim
                IN_TRANSIT
        );

        IN_TRANSIT.allowedTransitions = EnumSet.of(
                COLLECTED
        );

        COLLECTED.allowedTransitions = EnumSet.noneOf(ProductStatus.class);
        CANCELED.allowedTransitions  = EnumSet.noneOf(ProductStatus.class);
        DELETED.allowedTransitions   = EnumSet.noneOf(ProductStatus.class);
    }

    public boolean canTransitionTo(ProductStatus nextStatus) {
        return nextStatus != null && allowedTransitions.contains(nextStatus);
    }
}
