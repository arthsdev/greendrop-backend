package br.com.greendrop.backend.domain.model;

import br.com.greendrop.backend.domain.model.enums.RouteStopStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Column(name = "collection_request_id", nullable = false)
    private UUID collectionRequestId;

    @Column(name = "stop_order", nullable = false)
    private Integer stopOrder;

    @Enumerated(EnumType.STRING)
    private RouteStopStatus status;

    private Instant completedAt;

    private String failureType;

    private String failureReason;

    private String notes;

    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) this.status = RouteStopStatus.PENDING;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    // helper to get route id safely
    public UUID getRouteId() {
        return (this.route != null) ? this.route.getId() : null;
    }
}
