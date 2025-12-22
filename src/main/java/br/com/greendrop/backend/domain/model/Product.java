package br.com.greendrop.backend.domain.model;

import br.com.greendrop.backend.domain.model.enums.ProductCategory;
import br.com.greendrop.backend.domain.model.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a product listed by a user.
 */
@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

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
    // Claim
    // =====================================================

    /** Collector who claimed this product */
    @ManyToOne
    @JoinColumn(name = "claimed_by_id", columnDefinition = "BINARY(16)")
    private User claimedBy;

    /** Timestamp when product was claimed */
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

    /** Images belonging to this product */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    /** Route stop once product is assigned to logistics */
    @OneToOne(mappedBy = "product")
    private RouteStop routeStop;

    // =====================================================
    // Audit
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
     */
    public void claimBy(User collector) {
        this.claimedBy = collector;
        this.claimedAt = LocalDateTime.now();
        this.status = ProductStatus.ASSIGNED;
    }

    /**
     * Releases the claim and returns product to PENDING state.
     */
    public void unclaim() {
        this.claimedBy = null;
        this.claimedAt = null;
        this.status = ProductStatus.PENDING;
    }

    /**
     * Marks product as posted by a user.
     * Initial state is always PENDING.
     */
    public void postBy(User user) {
        this.postedBy = user;
        this.status = ProductStatus.PENDING;
    }

    /**
     * Replaces all product images.
     */
    public void replaceImages(List<ProductImage> images) {
        this.images.clear();

        if (images != null) {
            images.forEach(image -> image.setProduct(this));
            this.images.addAll(images);
        }
    }

    /**
     * Soft delete.
     */
    public void markAsDeleted() {
        this.status = ProductStatus.DELETED;
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
    // JPA lifecycle
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
