package br.com.greendrop.backend.dto.pagination;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Standard wrapper for paginated API responses.
 *
 * @param <T> type of the page content
 */
@Schema(description = "Standard paginated response")
public record PaginatedResponse<T>(

        /** Pagination metadata */
        @Schema(description = "Pagination information")
        PageMetaResponse meta,

        /** Data of the current page */
        @Schema(description = "Page data")
        List<T> data


) {}
