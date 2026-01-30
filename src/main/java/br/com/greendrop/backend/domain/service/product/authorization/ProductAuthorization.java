package br.com.greendrop.backend.domain.service.product.authorization;

import br.com.greendrop.backend.domain.model.Product;
import br.com.greendrop.backend.domain.model.User;
import br.com.greendrop.backend.domain.model.enums.Role;
import br.com.greendrop.backend.exception.generic.BusinessException;
import br.com.greendrop.backend.exception.global.ErrorCode;
import org.springframework.stereotype.Component;

/**
 * ProductAuthorization
 *
 * Handles authorization decisions for product-related actions,
 * based on user identity, role and ownership.
 * This class:
 * Enforces WHO can perform an action</li>
 * Throws authorization-related exceptions
 *
 * This class does NOT:
 * Validate product state
 * Modify domain entities
 */
@Component
public class ProductAuthorization {

    // =====================================================
    // CREATE
    // =====================================================

    public void checkCanCreateProduct(User user) {
        requireAuthenticated(user);

        if (user.getRole() != Role.USER && user.getRole() != Role.ADMIN) {
            throw new BusinessException(ErrorCode.PRODUCT_CREATE_FORBIDDEN);
        }
    }

    // =====================================================
    // UPDATE / DELETE
    // =====================================================

    public void checkCanModifyProduct(Product product, User user) {
        requireAuthenticated(user);

        boolean isOwner =
                product.getPostedBy() != null &&
                        product.getPostedBy().getId().equals(user.getId());

        if (!isOwner && user.getRole() != Role.ADMIN) {
            throw new BusinessException(ErrorCode.PRODUCT_MODIFY_FORBIDDEN);
        }
    }

    // =====================================================
    // CLAIM
    // =====================================================

    public void checkCanClaimProduct(Product product, User user) {
        requireAuthenticated(user);

        if (!user.isCollector()) {
            throw new BusinessException(ErrorCode.PRODUCT_CLAIM_ONLY_COLLECTOR);
        }

        boolean isOwner =
                product.getPostedBy() != null &&
                        product.getPostedBy().getId().equals(user.getId());

        if (isOwner) {
            throw new BusinessException(ErrorCode.PRODUCT_OWNER_CANNOT_CLAIM);
        }
    }

    // =====================================================
    // UNCLAIM
    // =====================================================

    public void checkCanUnclaimProduct(Product product, User user) {
        requireAuthenticated(user);

        boolean isCurrentCollector =
                product.getClaimedBy() != null &&
                        product.getClaimedBy().getId().equals(user.getId());

        if (!isCurrentCollector) {
            throw new BusinessException(ErrorCode.PRODUCT_UNCLAIM_FORBIDDEN);
        }
    }

    // =====================================================
    // INTERNAL
    // =====================================================

    private void requireAuthenticated(User user) {
        if (user == null || user.getRole() == null) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_REQUIRED);
        }
    }
}
