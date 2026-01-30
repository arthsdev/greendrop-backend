package br.com.greendrop.backend.presentation.product;

import br.com.greendrop.backend.domain.model.Product;
import br.com.greendrop.backend.domain.service.product.policy.ProductActionPolicy;
import br.com.greendrop.backend.dto.product.ProductActionPolicyResponse;
import br.com.greendrop.backend.dto.product.ProductDetailResponseDTO;
import br.com.greendrop.backend.dto.product.ProductListResponseDTO;
import br.com.greendrop.backend.mapper.product.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Presenter responsible for assembling Product responses.
 *
 * Responsibilities:
 * - List / dashboard views (with action permissions)
 * - Detail views (pure data, no policy)
 *
 * This class does NOT contain business logic.
 * It only prepares API-facing DTOs.
 */
@Component
@RequiredArgsConstructor
public class ProductPresenter {

    private final ProductMapper productMapper;

    /**
     * Builds a ProductListResponseDTO enriched with
     * user-specific action permissions.
     *
     * Used by:
     * - product listings
     * - dashboards
     */
    public ProductListResponseDTO toList(
            Product product,
            ProductActionPolicy policy
    ) {
        return productMapper
                .toListResponse(product)
                .withActions(
                        new ProductActionPolicyResponse(
                                policy.canEdit(),
                                policy.canDelete(),
                                policy.canClaim(),
                                policy.canUnclaim()
                        )
                );
    }

    /**
     * Builds a ProductDetailResponseDTO.
     *
     * Used by:
     * - product detail screens
     *
     * No action policy is applied here.
     */
    public ProductDetailResponseDTO toDetail(Product product) {
        return productMapper.toDetailResponse(product);
    }
}
