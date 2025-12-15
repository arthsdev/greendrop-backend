package br.com.greendrop.backend.dto.product;

import br.com.greendrop.backend.domain.model.enums.ProductCategory;
import jakarta.validation.constraints.*;
import java.util.List;

/**
 * DTO used when creating a new Product.
 * Contains all required fields with validation annotations
 * to ensure safe and consistent input data.
 */
public record ProductCreateDTO(

        @NotBlank(message = "Title is required")
        String title,

        @NotBlank(message = "Description is required")
        String description,

        @NotNull(message = "Weight (kg) is required")
        @Positive(message = "Weight must be greater than zero")
        Double weightKg,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than zero")
        Double quantity,

        @NotNull(message = "Category is required")
        ProductCategory category,

        @NotNull(message = "Image list cannot be null")
        @Size(min = 1, message = "At least one image URL must be provided")
        List<
                @NotBlank(message = "Image URL cannot be blank")
                        String
                > imageUrls

) {}
