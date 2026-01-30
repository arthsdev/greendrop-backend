package br.com.greendrop.backend.domain.repository;

import br.com.greendrop.backend.domain.model.Product;
import br.com.greendrop.backend.domain.model.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link Product}.
 *
 * Uses EntityGraph to fetch relations for list views and prevent N+1 queries.
 * Supports specification-based queries with pagination.
 */
public interface ProductRepository
        extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    // -----------------------------
    // List products created by a user
    // -----------------------------
    @EntityGraph(value = "Product.list")
    @NonNull
    List<Product> findByPostedById(@NonNull UUID userId);

    // -----------------------------
    // List products claimed by a user
    // -----------------------------
    @EntityGraph(value = "Product.list")
    @NonNull
    List<Product> findByClaimedById(@NonNull UUID userId);

    // -----------------------------
    // Paginated products created by a user (excluding DELETED)
    // -----------------------------
    @EntityGraph(value = "Product.list")
    @NonNull
    Page<Product> findByPostedByIdAndStatusNot(
            @NonNull UUID postedById,
            @NonNull ProductStatus status,
            @NonNull Pageable pageable
    );

    // -----------------------------
    // Paginated products claimed by a user and filtered by status
    // -----------------------------
    @EntityGraph(value = "Product.list")
    @NonNull
    Page<Product> findByClaimedByIdAndStatus(
            @NonNull UUID userId,
            @NonNull ProductStatus status,
            @NonNull Pageable pageable
    );

    // -----------------------------
    // Specification-based paginated query
    // -----------------------------
    @Override
    @EntityGraph(value = "Product.list")
    @NonNull
    Page<Product> findAll(
            @NonNull org.springframework.data.jpa.domain.Specification<Product> spec,
            @NonNull Pageable pageable
    );
}
