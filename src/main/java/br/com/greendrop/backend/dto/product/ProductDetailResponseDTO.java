package br.com.greendrop.backend.dto.product;

import br.com.greendrop.backend.domain.model.enums.ProductCategory;
import br.com.greendrop.backend.domain.model.enums.ProductStatus;

import java.util.List;
import java.util.UUID;

public record ProductDetailResponseDTO(
        UUID id,
        String title,
        String description,
        Double weightKg,
        Double quantity,
        ProductCategory category,
        ProductStatus status,
        List<String> images,
        UUID postedBy,
        UUID claimedBy
) {}
