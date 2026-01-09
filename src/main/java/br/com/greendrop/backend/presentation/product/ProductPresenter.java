package br.com.greendrop.backend.presentation.product;

import br.com.greendrop.backend.domain.model.Product;
import br.com.greendrop.backend.domain.service.product.policy.ProductActionPolicy;
import br.com.greendrop.backend.dto.product.ProductActionPolicyResponse;
import br.com.greendrop.backend.dto.product.ProductResponseDTO;
import br.com.greendrop.backend.mapper.product.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * This presenter combines the base product data with
 * user-specific action permissions calculated by
 * {@link ProductActionPolicy}.
 * Mapping concerns are delegated to {@link ProductMapper},
 * while authorization-related flags are added here to keep
 * responsibilities well separated.
 */
@Component
@RequiredArgsConstructor
public class ProductPresenter {

    private final ProductMapper productMapper;

    /**
     * Creates a {@link ProductResponseDTO} enriched with
     * action permissions for the current user.
     *
     * @param product the product domain entity
     * @param policy  the evaluated action policy
     * @return the assembled response DTO
     */
    public ProductResponseDTO present(Product product, ProductActionPolicy policy) {

        ProductResponseDTO base = productMapper.toResponse(product);

        return new ProductResponseDTO(
                base.id(),
                base.title(),
                base.description(),
                base.weightKg(),
                base.quantity(),
                base.category(),
                base.status(),
                base.images(),
                base.postedBy(),
                new ProductActionPolicyResponse(
                        policy.canEdit(),
                        policy.canDelete(),
                        policy.canClaim(),
                        policy.canUnclaim()
                )
        );
    }
}
