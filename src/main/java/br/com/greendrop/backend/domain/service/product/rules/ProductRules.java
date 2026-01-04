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
 * Contains domain-level business rules for Products.
 * This class validates STATE and CONSISTENCY only.
 *
 * Responsibilities:
 * - validate if an operation is allowed given the current product state
 * - prevent illegal transitions or actions
 *
 * Does NOT:
 * - change product status
 * - choose next status
 * - check permissions (authorization)
 * </p>
 */
@Component
public class ProductRules {

    // =====================================================
    // RouteStop constraints (STRUCTURAL rules)
    // =====================================================

    /**
     * Ensures that a product is not linked to a route.
     *
     * This is a structural invariant:
     * once linked to a route, the product cannot be
     * updated, deleted, claimed or unclaimed.
     */
    public void ensureNotLinkedToRoute(Product product) {
        if (product.getRouteStop() != null) {
            throw new BusinessException(ErrorCode.PRODUCT_LINKED_TO_ROUTE);
        }
    }

    // =====================================================
    // Category x Role constraints
    // =====================================================

    /**
     * Ensures a user can create or update a product with the given category.
     *
     * Rules:
     * - user and role must exist
     * - collectors cannot create or update products
     * - null category is allowed (partial update)
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

        // USER and ADMIN are allowed
    }

    // =====================================================
    // Claim / Unclaim STATE rules
    // =====================================================

    /**
     * Ensures that a product can be claimed.
     *
     * Validates product STATE only.
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
     * Validates product STATE only.
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
