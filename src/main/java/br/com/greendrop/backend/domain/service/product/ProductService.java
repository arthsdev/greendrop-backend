package br.com.greendrop.backend.domain.service.product;

import br.com.greendrop.backend.domain.model.Product;
import br.com.greendrop.backend.domain.model.ProductImage;
import br.com.greendrop.backend.domain.model.User;
import br.com.greendrop.backend.domain.model.enums.ProductCategory;
import br.com.greendrop.backend.domain.model.enums.ProductStatus;
import br.com.greendrop.backend.domain.model.enums.Role;
import br.com.greendrop.backend.domain.repository.ProductRepository;
import br.com.greendrop.backend.domain.service.product.authorization.ProductAuthorization;
import br.com.greendrop.backend.domain.service.product.rules.ProductRules;
import br.com.greendrop.backend.domain.service.product.validation.ProductValidation;
import br.com.greendrop.backend.domain.service.product.specification.ProductSpecification;
import br.com.greendrop.backend.dto.product.*;
import br.com.greendrop.backend.exception.generic.BusinessException;
import br.com.greendrop.backend.exception.global.ErrorCode;
import br.com.greendrop.backend.infrastructure.security.service.CurrentUserService;
import br.com.greendrop.backend.mapper.product.ProductMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ProductService - domain orchestration for Product lifecycle.
 *
 * Responsibilities:
 *  - enforce authorization & domain rules
 *  - validate input
 *  - map DTOs ↔ entities using ProductMapper
 *  - persist via ProductRepository
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CurrentUserService currentUserService;

    private final ProductAuthorization authorization;
    private final ProductValidation validation;
    private final ProductRules rules;

    // ========================================================================
    // CREATE
    // ========================================================================

    /**
     * Create a new product owned by current authenticated user.
     */
    public ProductResponseDTO create(ProductCreateDTO dto) {
        if (dto == null) throw new BusinessException(ErrorCode.BAD_REQUEST);

        User user = currentUserService.getCurrentUser();

        // Authorization: role-based decision (throws Forbidden)
        authorization.checkCanCreateProduct(user);

        // Business rules: validate category/role constraints
        rules.validateCategoryForRole(user, dto.category());

        // Input validation (image urls etc)
        validation.validateImageUrls(dto.imageUrls());

        // Map and enrich
        Product product = productMapper.toEntity(dto);
        product.postBy(user);

        if (dto.imageUrls() != null && !dto.imageUrls().isEmpty()) {
            List<ProductImage> images = dto.imageUrls().stream()
                    .filter(Objects::nonNull)
                    .map(url -> ProductImage.builder().url(url).build())
                    .toList();

            product.replaceImages(images);
        }

        productRepository.save(product);

        log.info("Product created (id={} postedBy={})", product.getId(), user.getId());

        return productMapper.toResponse(product);
    }

    // ========================================================================
    // READ
    // ========================================================================

    /**
     * Read-only transactional read for single product.
     */
    @Transactional(Transactional.TxType.SUPPORTS)
    public ProductResponseDTO getById(UUID id) {
        return productMapper.toResponse(findOrThrow(id));
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<ProductResponseDTO> getProductsByUser(UUID userId) {
        return productRepository.findByPostedById(userId).stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ========================================================================
    // UPDATE
    // ========================================================================

    public ProductResponseDTO update(UUID id, ProductUpdateDTO dto) {
        if (dto == null) throw new BusinessException(ErrorCode.BAD_REQUEST);

        Product product = findOrThrow(id);
        User current = currentUserService.getCurrentUser();

        // Owner or admin
        authorization.checkOwnershipOrAdmin(product, current);

        // Domain rule: can't update if linked to route stop
        rules.ensureNotLinkedToRouteStop(product);

        // Validate images (if present)
        validation.validateImageUrls(dto.imageUrls());

        // If DTO includes a category, check role constraints (collector, etc.)
        rules.validateCategoryForRole(current, dto.category());

        // Apply partial update (MapStruct decorator configured to IGNORE nulls)
        productMapper.updateEntityFromDTO(dto, product);

        if (dto.imageUrls() != null) {
            List<ProductImage> images = dto.imageUrls().stream()
                    .filter(Objects::nonNull)
                    .map(url -> ProductImage.builder().url(url).build())
                    .toList();

            product.replaceImages(images);
        }

        productRepository.save(product);

        log.info("Product updated (id={} by={})", product.getId(), current.getId());

        return productMapper.toResponse(product);
    }

    // ========================================================================
    // DELETE (soft)
    // ========================================================================

    public void softDelete(UUID id) {
        Product product = findOrThrow(id);
        User current = currentUserService.getCurrentUser();

        authorization.checkOwnershipOrAdmin(product, current);
        rules.ensureNotLinkedForDelete(product);

        product.markAsDeleted();
        productRepository.save(product);

        log.info("Product soft-deleted (id={} by={})", product.getId(), current.getId());
    }

    // ========================================================================
    // LIST / FILTERING
    // ========================================================================

    public Page<ProductResponseDTO> listProducts(
            ProductStatus status,
            ProductCategory category,
            UUID postedBy,
            UUID routeStopId,
            Double weightMin,
            Double weightMax,
            LocalDateTime createdFrom,
            LocalDateTime createdTo,
            Pageable pageable) {

        // List Active products if status is null as default
        if (status == null) {
            status = ProductStatus.PENDING;
        }

        Specification<Product> spec = Specification.where(ProductSpecification.hasStatus(status))
                .and(ProductSpecification.hasCategory(category))
                .and(ProductSpecification.postedBy(postedBy))
                .and(ProductSpecification.hasRouteStop(routeStopId))
                .and(ProductSpecification.hasWeightBetween(weightMin, weightMax))
                .and(ProductSpecification.createdBetween(createdFrom, createdTo));

        return productRepository.findAll(spec, pageable).map(productMapper::toResponse);
    }

    // ========================================================================
    // LIST CREATED PRODUCTS
    // ========================================================================

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<ProductResponseDTO> getMyProducts() {
        UUID userId = currentUserService.getCurrentUserId();

        return productRepository
                .findByPostedByIdAndStatusNot(userId, ProductStatus.DELETED)
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    // ========================================================================
// LIST CLAIMED PRODUCTS
// ========================================================================
    public List<ProductResponseDTO> getMyClaimedProducts() {

        UUID collectorId = currentUserService.getCurrentUserId();

        return productRepository
                .findByClaimedByIdAndStatus(
                        collectorId,
                        ProductStatus.ASSIGNED
                )
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }


    // ========================================================================
    // Helpers
    // ========================================================================

    private Product findOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }
}