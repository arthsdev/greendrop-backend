package br.com.greendrop.backend.dto.product.swagger;

import br.com.greendrop.backend.dto.pagination.PageMetaResponse;
import br.com.greendrop.backend.dto.product.ProductResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Swagger-only DTO used to document paginated product responses.
 * Not used at runtime.
 */
@Schema(name = "PaginatedProductResponse")
public class PaginatedProductResponse {

    /** Pagination metadata */
    @Schema(description = "Pagination metadata")
    public PageMetaResponse meta;

    /** List of products in the current page */
    @Schema(description = "List of products")
    public List<ProductResponseDTO> data;
}
