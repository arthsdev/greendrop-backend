package br.com.greendrop.backend.domain.service.product.policy;

import br.com.greendrop.backend.domain.model.Product;
import br.com.greendrop.backend.domain.model.User;
import br.com.greendrop.backend.domain.model.enums.ProductStatus;
import br.com.greendrop.backend.domain.model.enums.Role;

/**
 * ProductActionPolicy
 *
 * <p>
 * Represents the set of actions that the current authenticated user
 * is allowed to perform on a given Product, based on:
 * <ul>
 *   <li>Product state</li>
 *   <li>User role</li>
 *   <li>User relationship to the product (creator / collector)</li>
 * </ul>
 *
 * This class:
 * <ul>
 *   <li>Does NOT throw exceptions</li>
 *   <li>Does NOT modify product state</li>
 *   <li>Does NOT perform authorization checks</li>
 * </ul>
 *
 * It is intended to be used as a read-only policy object
 * to inform frontend behavior.
 * </p>
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

     /* ===============================
        Domain questions
        =============================== */

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


    public static ProductActionPolicy from(Product product, User user) {

        // -------------------------------------------------
        // User ↔ Product relationship
        // -------------------------------------------------
        boolean isCreator = product.getPostedBy().getId().equals(user.getId());
        boolean isCollector = user.hasRole(Role.COLLECTOR);
        boolean isCurrentCollector =
                product.getClaimedBy() != null &&
                        product.getClaimedBy().getId().equals(user.getId());


        // -------------------------------------------------
        // Product state
        // -------------------------------------------------
        boolean isPending = product.getStatus() == ProductStatus.PENDING;
        boolean isAssigned = product.getStatus() == ProductStatus.ASSIGNED;
        boolean hasCollector = product.isClaimed();
        boolean hasRoute = product.getRouteStop() != null;

        // -------------------------------------------------
        // Allowed actions
        // -------------------------------------------------
        boolean canEdit = isCreator && isPending;

        boolean canDelete = isCreator && isPending && !hasRoute && !hasCollector;

        boolean canClaim = isCollector && !isCreator && isPending && !hasCollector;

        boolean canUnclaim = isCollector && isCurrentCollector && isAssigned && !hasRoute;

        // -------------------------------------------------
        // Build policy
        // -------------------------------------------------
        return new ProductActionPolicy(
                canEdit,
                canDelete,
                canClaim,
                canUnclaim
        );
    }
}
