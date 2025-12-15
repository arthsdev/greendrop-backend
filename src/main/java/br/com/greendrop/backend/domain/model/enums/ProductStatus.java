package br.com.greendrop.backend.domain.model.enums;

public enum ProductStatus {
    PENDING,         // Product created by user
    ASSIGNED,        // Assigned to a route
    IN_COLLECTION,   // Collector is on the way
    COLLECTED,       // Pickup complete
    CANCELED,        // User canceled
    DELETED,         // Soft delete
}
