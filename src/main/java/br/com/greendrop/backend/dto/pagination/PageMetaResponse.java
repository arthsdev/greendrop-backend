package br.com.greendrop.backend.dto.pagination;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Metadata describing the state of a paginated result.
 */
@Schema(description = "Pagination metadata")
public record PageMetaResponse(

        @Schema(example = "0", description = "Current page number (0-based)")
        int page,

        @Schema(example = "10", description = "Page size")
        int size,

        @Schema(example = "120", description = "Total number of elements")
        long totalElements,

        @Schema(example = "12", description = "Total number of pages")
        int totalPages,

        @Schema(example = "true", description = "Whether there is a next page")
        boolean hasNext,

        @Schema(example = "false", description = "Whether there is a previous page")
        boolean hasPrevious
) {}
