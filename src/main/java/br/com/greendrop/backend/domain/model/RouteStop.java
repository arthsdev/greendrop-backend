package br.com.greendrop.backend.domain.model;

import br.com.greendrop.backend.domain.model.enums.RouteStopStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a single stop within a Route.
 * Each RouteStop may optionally hold a reference to a Product to be collected.
 *
 * <p>This entity is operational and workflow-oriented.
 * It owns the one-to-one relationship with Product,
 * meaning the foreign key (product_id) is stored in this table.</p>
 */
@Entity
@Table(name = "route_stop")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RouteStop {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Parent route for this stop */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    /** Identifier of the collection request linked to this stop */
    @Column(name = "collection_request_id", nullable = false)
    private UUID collectionRequestId;

    /** Position of this stop in the route order */
    @Column(name = "stop_order", nullable = false)
    private Integer stopOrder;

    /** Current execution status of the stop */
    @Enumerated(EnumType.STRING)
    private RouteStopStatus status;

    private Instant completedAt;

    private String failureType;

    private String failureReason;

    private String notes;

    /** Product associated with this stop (optional) */
    @OneToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = RouteStopStatus.PENDING;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    /** Helper to safely retrieve the Route ID without triggering lazy-loading */
    public UUID getRouteId() {
        return (this.route != null) ? this.route.getId() : null;
    }
}
