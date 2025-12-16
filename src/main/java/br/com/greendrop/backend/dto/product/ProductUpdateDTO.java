package br.com.greendrop.backend.dto.product;

import br.com.greendrop.backend.domain.model.enums.ProductCategory;
import jakarta.validation.constraints.*;

import java.util.List;

/**
 * DTO used for updating an existing Product.
 * All fields are optional. If a field is null, it will not be updated.
 */
public record ProductUpdateDTO(

        @Size(min = 1, message = "Title must have at least 1 character")
        String title,

        @Size(min = 1, message = "Description must have at least 1 character")
        String description,

        @Positive(message = "Weight must be greater than zero")
        Double weightKg,

        @Positive(message = "Quantity must be greater than zero")
        Double quantity,

        ProductCategory category,

        @Size(min = 1, message = "At least one image URL must be provided")
        List<@NotBlank(message = "Image URL cannot be blank") String> imageUrls

) {}
