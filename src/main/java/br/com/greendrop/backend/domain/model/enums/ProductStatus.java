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
        PENDING.allowedTransitions = EnumSet.of(ASSIGNED, CANCELED, DELETED);
        ASSIGNED.allowedTransitions = EnumSet.of(IN_TRANSIT);
        IN_TRANSIT.allowedTransitions = EnumSet.of(COLLECTED);
        COLLECTED.allowedTransitions = EnumSet.noneOf(ProductStatus.class);
        CANCELED.allowedTransitions = EnumSet.noneOf(ProductStatus.class);
        DELETED.allowedTransitions = EnumSet.noneOf(ProductStatus.class);
    }

    public boolean canTransitionTo(ProductStatus nextStatus) {
        if (nextStatus == null) {
            return false;
        }
        return allowedTransitions.contains(nextStatus);
    }
}
