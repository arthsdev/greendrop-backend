package br.com.greendrop.backend.domain.repository;

import br.com.greendrop.backend.domain.model.Product;
import br.com.greendrop.backend.domain.model.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Product.
 * Extends JpaSpecificationExecutor to enable dynamic filtering in the service layer.
 */
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    List<Product> findByPostedById(UUID userId);

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    Page<Product> findByPostedByIdAndStatusNot(UUID postedById, ProductStatus status, Pageable pageable);

    List<Product> findByClaimedById(UUID userId);

    Page<Product> findByClaimedByIdAndStatus(UUID userId, ProductStatus status, Pageable pageable);
}