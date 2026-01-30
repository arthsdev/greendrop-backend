package br.com.greendrop.backend.domain.service.product.policy;

import br.com.greendrop.backend.domain.model.Product;
import br.com.greendrop.backend.domain.model.User;
import br.com.greendrop.backend.domain.model.enums.ProductStatus;
import br.com.greendrop.backend.domain.model.enums.Role;

import java.util.UUID;

/**
 * Defines which actions the current user can perform
 * on a Product.
 *
 * Read-only policy used to expose available actions
 * to the presentation layer.
 *
 * Does not perform authorization checks, throw exceptions,
 * or modify product state.
 *
 * Usage:
 *   ProductActionPolicy.from(product, currentUser);
 */
public class ProductActionPolicy {

    private final boolean canEdit;
    private final boolean canDelete;
    private final boolean canClaim;
    private final boolean canUnclaim;

    private ProductActionPolicy(
            boolean canEdit,
            boolean canDelete,
            boolean canClaim,
            boolean canUnclaim
    ) {
        this.canEdit = canEdit;
        this.canDelete = canDelete;
        this.canClaim = canClaim;
        this.canUnclaim = canUnclaim;
    }

    // =====================================================
    // Read-only accessors (used by Presenter / Frontend)
    // =====================================================

    public boolean canEdit() {
        return canEdit;
    }

    public boolean canDelete() {
        return canDelete;
    }

    public boolean canClaim() {
        return canClaim;
    }

    public boolean canUnclaim() {
        return canUnclaim;
    }

    // =====================================================
    // Factory
    // =====================================================

    public static ProductActionPolicy from(Product product, User user) {

        if (product == null || user == null) {
            return denyAll();
        }

        UUID userId = user.getId();

        // -------------------------------------------------
        // User ↔ Product relationship
        // -------------------------------------------------
        boolean isCreator = isSameUser(product.getPostedBy(), userId);
        boolean isCollector = user.hasRole(Role.COLLECTOR);
        boolean isCurrentCollector = isSameUser(product.getClaimedBy(), userId);

        // -------------------------------------------------
        // Product state
        // -------------------------------------------------
        ProductStatus status = product.getStatus();

        boolean isPending = status == ProductStatus.PENDING;
        boolean isAssigned = status == ProductStatus.ASSIGNED;
        boolean hasCollector = product.isClaimed();
        boolean hasRoute = product.getRouteStop() != null;

        // -------------------------------------------------
        // Allowed actions
        // -------------------------------------------------
        boolean canEdit =
                isCreator &&
                        isPending &&
                        !hasRoute;

        boolean canDelete =
                isCreator &&
                        isPending &&
                        !hasRoute &&
                        !hasCollector;

        boolean canClaim =
                isCollector &&
                        !isCreator &&
                        isPending &&
                        !hasCollector &&
                        !hasRoute;

        boolean canUnclaim =
                isCollector &&
                        isCurrentCollector &&
                        isAssigned &&
                        !hasRoute;

        return new ProductActionPolicy(
                canEdit,
                canDelete,
                canClaim,
                canUnclaim
        );
    }

    // =====================================================
    // Helpers
    // =====================================================

    private static boolean isSameUser(User user, UUID userId) {
        return user != null &&
                user.getId() != null &&
                user.getId().equals(userId);
    }

    private static ProductActionPolicy denyAll() {
        return new ProductActionPolicy(false, false, false, false);
    }
}
