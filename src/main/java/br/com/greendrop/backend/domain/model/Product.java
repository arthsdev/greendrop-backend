package br.com.greendrop.backend.domain.model;

import br.com.greendrop.backend.domain.model.enums.ProductCategory;
import br.com.greendrop.backend.domain.model.enums.ProductStatus;
import br.com.greendrop.backend.exception.global.ErrorCode;
import br.com.greendrop.backend.exception.generic.BusinessException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a product listed by a user for collection.
 *
 * Domain rules:
 * - Product does NOT decide business flows
 * - Product does NOT choose next status
 * - Status changes are validated here, but orchestrated by services
 */
@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    // =====================================================
    // Identifiers
    // =====================================================

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    // =====================================================
    // Basic attributes
    // =====================================================

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "weight_kg", nullable = false)
    private Double weightKg;

    @Column(nullable = false)
    private Double quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    // =====================================================
    // Claim information
    // =====================================================

    @ManyToOne
    @JoinColumn(name = "claimed_by_id", columnDefinition = "BINARY(16)")
    private User claimedBy;

    @Column(name = "claimed_at")
    private LocalDateTime claimedAt;

    // =====================================================
    // Ownership
    // =====================================================

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", columnDefinition = "BINARY(16)")
    private User postedBy;

    // =====================================================
    // Relations
    // =====================================================

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    @OneToOne(mappedBy = "product")
    private RouteStop routeStop;

    // =====================================================
    // Audit fields
    // =====================================================

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // =====================================================
    // State mutation (NO business flow here)
    // =====================================================

    /**
     * Assigns a collector to this product.
     * Does NOT change status.
     */
    public void assignCollector(User collector) {
        if (collector == null) {
            throw new BusinessException(ErrorCode.INVALID_COLLECTOR);
        }

        this.claimedBy = collector;
        this.claimedAt = LocalDateTime.now();
    }

    /**
     * Removes collector from this product.
     * Does NOT change status.
     */
    public void removeCollector() {
        if (this.claimedBy == null) {
            throw new BusinessException(ErrorCode.COLLECTOR_NOT_ASSIGNED);
        }

        this.claimedBy = null;
        this.claimedAt = null;
    }

    /**
     * Replaces all images associated with the product.
     */
    public void replaceImages(List<ProductImage> images) {
        this.images.clear();

        if (images != null) {
            images.forEach(image -> image.setProduct(this));
            this.images.addAll(images);
        }
    }

    /**
     * Sets product owner.
     * Initial status is always PENDING.
     */
    public void postBy(User user) {
        this.postedBy = user;

        if (this.status == null) {
            this.status = ProductStatus.PENDING;
        }
    }

    // =====================================================
    // Status transition control (validated, not orchestrated)
    // =====================================================

    /**
     * Applies a validated status transition.
     * This method MUST be called only by domain services.
     */
    public void changeStatus(ProductStatus nextStatus) {
        if (this.status == null) {
            throw new BusinessException(ErrorCode.INVALID_PRODUCT_STATUS_TRANSITION);
        }

        if (!this.status.canTransitionTo(nextStatus)) {
            throw new BusinessException(ErrorCode.INVALID_PRODUCT_STATUS_TRANSITION);
        }

        this.status = nextStatus;
    }

    // =====================================================
    // Derived state helpers
    // =====================================================

    public boolean isClaimed() {
        return claimedBy != null;
    }

    public boolean isDeleted() {
        return status == ProductStatus.DELETED;
    }

    // =====================================================
    // JPA lifecycle hooks
    // =====================================================

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }

        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (status == null) {
            status = ProductStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
