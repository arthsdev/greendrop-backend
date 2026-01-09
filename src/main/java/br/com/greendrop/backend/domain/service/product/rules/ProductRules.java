package br.com.greendrop.backend.domain.service.product.rules;

import br.com.greendrop.backend.domain.model.Product;
import br.com.greendrop.backend.domain.model.User;
import br.com.greendrop.backend.domain.model.enums.ProductCategory;
import br.com.greendrop.backend.domain.model.enums.ProductStatus;
import br.com.greendrop.backend.domain.model.enums.Role;
import br.com.greendrop.backend.exception.generic.BusinessException;
import br.com.greendrop.backend.exception.global.ErrorCode;
import org.springframework.stereotype.Component;

/**
 * ProductRules
 *
 * <p>
 * Domain-level business rules for Products.
 *
 * This class validates:
 * - product STATE
 * - structural CONSISTENCY
 *
 * It does NOT:
 * - perform authorization checks
 * - mutate entities
 * - change status directly
 * </p>
 */
@Component
public class ProductRules {

    // =====================================================
    // Structural invariants
    // =====================================================

    /**
     * A product linked to a route becomes immutable.
     *
     * Once linked, it cannot be:
     * - updated
     * - deleted
     * - claimed
     * - unclaimed
     */
    public void ensureNotLinkedToRoute(Product product) {
        if (product.getRouteStop() != null) {
            throw new BusinessException(ErrorCode.PRODUCT_LINKED_TO_ROUTE);
        }
    }

    // =====================================================
    // Update / Delete STATE rules
    // =====================================================

    /**
     * Ensures that a product can be updated.
     */
    public void ensureCanBeUpdated(Product product) {
        ensureNotLinkedToRoute(product);

        if (product.getStatus() == ProductStatus.DELETED) {
            throw new BusinessException(ErrorCode.PRODUCT_ALREADY_DELETED);
        }
    }

    /**
     * Ensures that a product can be soft-deleted.
     */
    public void ensureCanBeDeleted(Product product) {
        ensureNotLinkedToRoute(product);

        if (product.getStatus() == ProductStatus.DELETED) {
            throw new BusinessException(ErrorCode.PRODUCT_ALREADY_DELETED);
        }
    }

    // =====================================================
    // Category x Role rules
    // =====================================================

    /**
     * Ensures a user can create or update a product
     * with the given category.
     *
     * Rules:
     * - user and role must exist
     * - collectors cannot create or update products
     * - null category is allowed (PATCH / partial update)
     */
    public void validateCategoryForRole(User user, ProductCategory category) {

        if (user == null || user.getRole() == null) {
            throw new BusinessException(ErrorCode.INVALID_CATEGORY_FOR_ROLE);
        }

        if (category == null) {
            return;
        }

        if (user.getRole() == Role.COLLECTOR) {
            throw new BusinessException(ErrorCode.INVALID_CATEGORY_FOR_ROLE);
        }
    }

    // =====================================================
    // Claim / Unclaim STATE rules
    // =====================================================

    /**
     * Ensures that a product can be claimed.
     *
     * Validates STATE only.
     */
    public void ensureCanBeClaimed(Product product) {

        if (product.getStatus() == ProductStatus.ASSIGNED) {
            throw new BusinessException(ErrorCode.PRODUCT_ALREADY_CLAIMED);
        }

        if (product.getStatus() != ProductStatus.PENDING) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_AVAILABLE_FOR_CLAIM);
        }
    }

    /**
     * Ensures that a product can be unclaimed.
     *
     * Validates STATE only.
     */
    public void ensureCanBeUnclaimed(Product product) {

        if (!product.isClaimed()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_CLAIMED);
        }

        if (product.getStatus() != ProductStatus.ASSIGNED) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_ASSIGNED);
        }
    }
}
