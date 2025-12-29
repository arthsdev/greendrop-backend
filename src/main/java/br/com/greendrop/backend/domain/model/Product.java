package br.com.greendrop.backend.domain.model;

import br.com.greendrop.backend.domain.model.enums.ProductCategory;
import br.com.greendrop.backend.domain.model.enums.ProductStatus;
import br.com.greendrop.backend.exception.generic.BusinessException;
import br.com.greendrop.backend.exception.global.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a product listed by a user for collection.
 *
 * This entity is responsible for enforcing domain rules related to
 * product lifecycle, ownership and status transitions.
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

    /** Collector who claimed this product */
    @ManyToOne
    @JoinColumn(name = "claimed_by_id", columnDefinition = "BINARY(16)")
    private User claimedBy;

    /** Timestamp when the product was claimed */
    @Column(name = "claimed_at")
    private LocalDateTime claimedAt;

    // =====================================================
    // Ownership
    // =====================================================

    /** User who posted the product */
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", columnDefinition = "BINARY(16)")
    private User postedBy;

    // =====================================================
    // Relations
    // =====================================================

    /** Images associated with the product */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    /** Route stop created after assignment to logistics */
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
    // Domain behavior
    // =====================================================

    /**
     * Claims this product for collection.
     *
     * @param collector user who is claiming the product
     * @throws IllegalStateException if status transition is not allowed
     */
    public void claimBy(User collector) {
        changeStatus(ProductStatus.ASSIGNED);

        this.claimedBy = collector;
        this.claimedAt = LocalDateTime.now();
    }

    /**
     * Releases the product claim and returns it to PENDING state.
     *
     * @throws IllegalStateException if status transition is not allowed
     */
    public void unclaim() {
        changeStatus(ProductStatus.PENDING);
        this.claimedBy = null;
        this.claimedAt = null;
    }

    /**
     * Marks this product as posted by a user.
     * Initial status is always PENDING.
     *
     * @param user product owner
     */
    public void postBy(User user) {
        this.postedBy = user;
        changeStatus(ProductStatus.PENDING);
    }

    /**
     * Replaces all images associated with the product.
     *
     * @param images new list of images
     */
    public void replaceImages(List<ProductImage> images) {
        this.images.clear();

        if (images != null) {
            images.forEach(image -> image.setProduct(this));
            this.images.addAll(images);
        }
    }

    /**
     * Soft deletes the product.
     *
     * @throws IllegalStateException if status transition is not allowed
     */
    public void markAsDeleted() {
        changeStatus(ProductStatus.DELETED);
    }

    // =====================================================
    // Status transition control
    // =====================================================

    /**
     * Centralized status transition method.
     * All status changes must go through this method.
     *
     * @param nextStatus desired next status
     * @throws IllegalStateException if transition is not allowed
     */
    private void changeStatus(ProductStatus nextStatus) {
        if (!this.status.canTransitionTo(nextStatus)) {
            throw new BusinessException(ErrorCode.INVALID_PRODUCT_STATUS_TRANSITION);
        }
        this.status = nextStatus;
    }

    // =====================================================
    // Derived state helpers
    // =====================================================

    /**
     * @return true if the product is currently claimed
     */
    public boolean isClaimed() {
        return claimedBy != null;
    }

    /**
     * @return true if the product is soft deleted
     */
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
