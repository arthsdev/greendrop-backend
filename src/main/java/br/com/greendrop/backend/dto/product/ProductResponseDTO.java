package br.com.greendrop.backend.dto.product;

import br.com.greendrop.backend.domain.model.enums.ProductStatus;
import br.com.greendrop.backend.domain.model.enums.ProductCategory;

import java.util.List;
import java.util.UUID;

//**
// * Data Transfer Object (DTO) representing the product information returned by the API.
// * This record is typically used in responses sent to clients, containing all relevant data
// * about a product, including its identifiers, descriptive fields, classification details,
// * images, and ownership information.
// */

/**
 * @deprecated Use ProductListResponseDTO or ProductDetailResponseDTO
 * depending on the use case.
 */
@Deprecated
public record ProductResponseDTO(
        UUID id,
        String title,
        String description,
        Double weightKg,
        Double quantity,
        ProductCategory category,
        ProductStatus status,
        List<String> images,
        UUID postedBy,
        ProductActionPolicyResponse actions
) {}
