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
 * <p>Contains domain-level business rules for Products that go beyond simple
 * validation and authorization. Ensures long-term consistency and prevents
 * illegal state transitions.</p>
 */
@Component
public class ProductRules {

    /**
     * Prevents updates to products already linked to a RouteStop.
     *
     * @param product Product to be updated
     * @throws BusinessException when product is already linked to routing
     */
    public void ensureNotLinkedToRouteStop(Product product) {
        if (product.getRouteStop() != null) {
            throw new BusinessException(ErrorCode.PRODUCT_CANNOT_UPDATE);
        }
    }

    /**
     * Prevents deletion of products already assigned to a RouteStop.
     *
     * @param product Product to be deleted
     * @throws BusinessException when product is tied to logistics routing
     */
    public void ensureNotLinkedForDelete(Product product) {
        if (product.getRouteStop() != null) {
            throw new BusinessException(ErrorCode.PRODUCT_CANNOT_DELETE);
        }
    }

    /**
     * Ensures a user is allowed to create or update a product with the given category.
     *
     * <p>This method is null-safe and rejects operations where the role cannot be determined.
     * Currently, Collectors cannot create or update any product, regardless of category.</p>
     *
     * @param user     the user performing the action
     * @param category the product category being used
     * @throws BusinessException if the user or role is invalid, or the category is not allowed
     */
    public void validateCategoryForRole(User user, ProductCategory category) {

        // Safety check: user must exist
        if (user == null) {
            throw new BusinessException(ErrorCode.INVALID_CATEGORY_FOR_ROLE);
        }

        // Safety check: role must exist
        if (user.getRole() == null) {
            throw new BusinessException(ErrorCode.INVALID_CATEGORY_FOR_ROLE);
        }

        // If category is null (e.g., update without category change), allow it.
        // If you prefer to forbid null, just replace this block.
        if (category == null) {
            return;
        }

        switch (user.getRole()) {

            case COLLECTOR:
                // Collectors cannot create or update products of ANY category
                throw new BusinessException(ErrorCode.INVALID_CATEGORY_FOR_ROLE);

                // Future-proofing:
                // If tomorrow ADMIN has restricted categories, add logic here.
            case ADMIN:
            case USER:
                return;

            default:
                throw new BusinessException(ErrorCode.INVALID_CATEGORY_FOR_ROLE);
        }
    }

    /**
     * Ensures that a product can be claimed by a collector.
     *
     * <p>This method validates the product state only.
     * Authorization (who is claiming) must be handled elsewhere.</p>
     *
     * @param product Product to be claimed
     * @throws BusinessException if the product cannot be claimed
     */
    public void ensureCanBeClaimed(Product product) {

        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // Product must be pending
        if (product.getStatus() != ProductStatus.PENDING) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_AVAILABLE_FOR_CLAIM);
        }

        // Product must not be already claimed
        if (product.getClaimedBy() != null) {
            throw new BusinessException(ErrorCode.PRODUCT_ALREADY_CLAIMED);
        }

        // Product must not be linked to a route
        if (product.getRouteStop() != null) {
            throw new BusinessException(ErrorCode.PRODUCT_ALREADY_ASSIGNED_TO_ROUTE);
        }

        // 🚀 Future rules:
        // - time window validation
        // - distance (km radius)
    }


}