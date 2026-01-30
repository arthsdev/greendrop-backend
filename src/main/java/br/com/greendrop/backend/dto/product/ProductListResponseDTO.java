package br.com.greendrop.backend.dto.product;

import br.com.greendrop.backend.domain.model.enums.ProductCategory;
import br.com.greendrop.backend.domain.model.enums.ProductStatus;

import java.util.UUID;


public record ProductListResponseDTO(
        UUID id,
        String title,
        ProductCategory category,
        ProductStatus status,
        UUID postedBy,
        ProductActionPolicyResponse actions
) {
    public ProductListResponseDTO withActions(ProductActionPolicyResponse actions) {
        return new ProductListResponseDTO(
                id, title, category, status, postedBy, actions
        );
    }
}
