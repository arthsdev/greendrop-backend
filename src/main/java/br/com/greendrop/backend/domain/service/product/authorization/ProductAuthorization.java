package br.com.greendrop.backend.domain.service.product.authorization;

import br.com.greendrop.backend.domain.model.Product;
import br.com.greendrop.backend.domain.model.User;
import br.com.greendrop.backend.domain.model.enums.Role;
import br.com.greendrop.backend.exception.auth.ForbiddenException;
import br.com.greendrop.backend.exception.generic.BusinessException;
import br.com.greendrop.backend.exception.global.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class ProductAuthorization {

    /**
     * Only USER and ADMIN can create products.
     */
    public void checkCanCreateProduct(User user) {
        if (user == null || user.getRole() == null) {
            throw new ForbiddenException(ErrorCode.UNAUTHORIZED_ACTION);
        }

        Role role = user.getRole();

        boolean allowed =
                role == Role.USER ||
                        role == Role.ADMIN;

        if (!allowed) {
            throw new ForbiddenException(ErrorCode.UNAUTHORIZED_ACTION);
        }
    }

    /**
     * Product can be updated/deleted only by owner or admin.
     */
    public void checkOwnershipOrAdmin(Product product, User currentUser) {
        if (product == null || currentUser == null) {
            throw new ForbiddenException(ErrorCode.UNAUTHORIZED_ACTION);
        }

        boolean isOwner = product.getPostedBy().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new ForbiddenException(ErrorCode.UNAUTHORIZED_ACTION);
        }
    }

    /**
     * Validates whether a user is authorized to claim a product.
     * This method performs authorization checks related to the user identity
     * and role. It does NOT validate the product state (status, routing, time
     * or distance rules)
     */
    public void checkCanClaimProduct(User user, Product product) {

        if (!user.isCollector()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (product.getPostedBy().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }


    //TODO Javadoc with properly description
    public void checkCanUnclaim(Product product, User user) {
        if (!user.equals(product.getClaimedBy())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

}