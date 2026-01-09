package br.com.greendrop.backend.domain.service.product.authorization;

import br.com.greendrop.backend.domain.model.User;
import br.com.greendrop.backend.domain.model.enums.Role;
import br.com.greendrop.backend.exception.generic.BusinessException;
import br.com.greendrop.backend.exception.global.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class ProductAuthorization {

    /**
     * Only USER and ADMIN can create products.
     */
    public void checkCanCreateProduct(User user) {
        requireAuthenticated(user);

        if (user.getRole() != Role.USER && user.getRole() != Role.ADMIN) {
            throw new BusinessException(ErrorCode.PRODUCT_CREATE_FORBIDDEN);
        }
    }

    /**
     * Only OWNER or ADMIN can modify a product.
     */
    public void checkOwnershipOrAdmin(boolean isOwner, User user) {
        requireAuthenticated(user);

        if (!isOwner && user.getRole() != Role.ADMIN) {
            throw new BusinessException(ErrorCode.PRODUCT_MODIFY_FORBIDDEN);
        }
    }

    /**
     * Only collectors can claim products,
     * and they cannot claim their own products.
     */
    public void checkCanClaimProduct(boolean isOwner, User user) {
        requireAuthenticated(user);

        if (isOwner) {
            throw new BusinessException(ErrorCode.PRODUCT_OWNER_CANNOT_CLAIM);
        }

        if (!user.isCollector()) {
            throw new BusinessException(ErrorCode.PRODUCT_CLAIM_ONLY_COLLECTOR);
        }
    }

    /**
     * Only the collector who claimed can unclaim.
     */
    public void checkCanUnclaim(boolean isClaimer, User user) {
        requireAuthenticated(user);

        if (!isClaimer) {
            throw new BusinessException(ErrorCode.PRODUCT_UNCLAIM_FORBIDDEN);
        }
    }

    private void requireAuthenticated(User user) {
        if (user == null || user.getRole() == null) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_REQUIRED);
        }
    }
}

