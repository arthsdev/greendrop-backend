package br.com.greendrop.backend.domain.model;

import br.com.greendrop.backend.domain.model.enums.RouteStopStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a single stop within a Route.
 * Operational and workflow-oriented entity.
 *
 * This entity owns the one-to-one relationship with Product,
 * meaning the foreign key (product_id) is stored here.
 */
@Entity
@Table(name = "route_stop")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteStop {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    /** Parent route for this stop */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false, columnDefinition = "BINARY(16)")
    private Route route;

    /** Identifier of the collection request linked to this stop */
    @Column(name = "collection_request_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID collectionRequestId;

    /** Position of this stop in the route execution order */
    @Column(name = "stop_order", nullable = false)
    private Integer stopOrder;

    /** Current execution status */
    @Enumerated(EnumType.STRING)
    private RouteStopStatus status;

    private Instant completedAt;

    private String failureType;
    private String failureReason;
    private String notes;

    /** Optional product associated with this stop */
    @OneToOne
    @JoinColumn(name = "product_id", columnDefinition = "BINARY(16)")
    private Product product;

    /* Timestamps */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = RouteStopStatus.PENDING;
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = Instant.now();
    }

    /** Helper to safely retrieve the Route ID without triggering lazy loading */
    public UUID getRouteId() {
        return route != null ? route.getId() : null;
    }
}
