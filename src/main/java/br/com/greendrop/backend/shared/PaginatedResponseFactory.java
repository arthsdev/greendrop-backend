package br.com.greendrop.backend.shared;

import br.com.greendrop.backend.dto.pagination.PageMetaResponse;
import br.com.greendrop.backend.dto.pagination.PaginatedResponse;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Factory responsible for building standardized paginated API responses.
 * Translates Spring Data {@link Page} pagination details into the API
 * pagination contract, keeping controllers and services agnostic.
 */
public final class PaginatedResponseFactory {

    private PaginatedResponseFactory() {
        // prevents instantiation
    }

    /**
     * Creates a {@link PaginatedResponse} using pagination metadata from
     * a Spring Data {@link Page} and a list of already mapped DTOs.
     *
     * @param page Spring Data page containing pagination metadata
     * @param data DTOs to be returned in the response
     * @param <T>  DTO type
     * @return standardized paginated response
     */
    public static <T> PaginatedResponse<T> from(Page<?> page, List<T> data) {

        PageMetaResponse meta = new PageMetaResponse(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious()
        );

        return new PaginatedResponse<>(meta, data);
    }
}
