package br.com.greendrop.backend.domain.service.product;

import br.com.greendrop.backend.domain.model.Product;
import br.com.greendrop.backend.domain.model.ProductImage;
import br.com.greendrop.backend.domain.model.User;
import br.com.greendrop.backend.domain.model.enums.ProductCategory;
import br.com.greendrop.backend.domain.model.enums.ProductStatus;
import br.com.greendrop.backend.domain.repository.ProductRepository;
import br.com.greendrop.backend.domain.service.product.authorization.ProductAuthorization;
import br.com.greendrop.backend.domain.service.product.policy.ProductActionPolicy;
import br.com.greendrop.backend.domain.service.product.rules.ProductRules;
import br.com.greendrop.backend.domain.service.product.specification.ProductSpecification;
import br.com.greendrop.backend.domain.service.product.validation.ProductValidation;
import br.com.greendrop.backend.dto.pagination.PaginatedResponse;
import br.com.greendrop.backend.dto.product.*;
import br.com.greendrop.backend.exception.generic.BusinessException;
import br.com.greendrop.backend.exception.global.ErrorCode;
import br.com.greendrop.backend.infrastructure.security.service.CurrentUserService;
import br.com.greendrop.backend.mapper.product.ProductMapper;
import br.com.greendrop.backend.presentation.product.ProductPresenter;
import br.com.greendrop.backend.shared.PaginatedResponseFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * ProductService - domain orchestration for Product lifecycle.
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
    private final ProductStatusHistoryService productStatusHistoryService;
    private final ProductPresenter productPresenter;

    // ========================================================================
    // CREATE
    // ========================================================================

    public ProductResponseDTO create(ProductCreateDTO dto) {
        if (dto == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        User currentUser = currentUserService.getCurrentUser();

        authorization.checkCanCreateProduct(currentUser);
        rules.validateCategoryForRole(currentUser, dto.category());
        validation.validateImageUrls(dto.imageUrls());

        Product product = productMapper.toEntity(dto);
        product.postBy(currentUser);

        applyImages(product, dto.imageUrls());

        productRepository.save(product);

        log.info("Product created (id={}, postedBy={})",
                product.getId(), currentUser.getId());

        return toResponseWithPolicy(product, currentUser);
    }


    // ========================================================================
    // READ
    // ========================================================================

    @Transactional(Transactional.TxType.SUPPORTS)
    public ProductResponseDTO getById(UUID id) {
        User currentUser = currentUserService.getCurrentUser();
        Product product = findOrThrow(id);

        return toResponseWithPolicy(product, currentUser);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<ProductResponseDTO> getProductsByUser(UUID userId) {
        User currentUser = currentUserService.getCurrentUser();

        return productRepository.findByPostedById(userId).stream()
                .map(product -> toResponseWithPolicy(product, currentUser))
                .toList();
    }


    // ========================================================================
    // UPDATE
    // ========================================================================
    public ProductResponseDTO update(UUID id, ProductUpdateDTO dto) {
        if (dto == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        Product product = findOrThrow(id);
        User currentUser = currentUserService.getCurrentUser();

        boolean isOwner =
                product.getPostedBy() != null &&
                        product.getPostedBy().getId().equals(currentUser.getId());

        authorization.checkOwnershipOrAdmin(isOwner, currentUser);
        rules.ensureNotLinkedToRoute(product);
        rules.validateCategoryForRole(currentUser, dto.category());
        validation.validateImageUrls(dto.imageUrls());

        productMapper.updateEntityFromDTO(dto, product);
        applyImages(product, dto.imageUrls());

        productRepository.save(product);

        log.info("Product updated (id={}, by={})",
                product.getId(), currentUser.getId());

        return toResponseWithPolicy(product, currentUser);
    }

    // ========================================================================
    // DELETE (soft)
    // ========================================================================

    public void softDelete(UUID id) {
        Product product = findOrThrow(id);
        User currentUser = currentUserService.getCurrentUser();

        boolean isOwner =
                product.getPostedBy() != null &&
                        product.getPostedBy().getId().equals(currentUser.getId());

        authorization.checkOwnershipOrAdmin(isOwner, currentUser);
        rules.ensureNotLinkedToRoute(product);

        changeStatus(product, ProductStatus.DELETED, currentUser);

        log.info("Product soft-deleted (id={}, by={})",
                product.getId(), currentUser.getId());
    }

    // ========================================================================
    // LIST / FILTERING (Page)
    // ========================================================================

    @Transactional(Transactional.TxType.SUPPORTS)
    public PaginatedResponse<ProductResponseDTO> listProducts(
            ProductStatus status,
            ProductCategory category,
            UUID postedBy,
            UUID routeStopId,
            Double weightMin,
            Double weightMax,
            LocalDateTime createdFrom,
            LocalDateTime createdTo,
            Pageable pageable
    ) {

        ProductStatus effectiveStatus =
                status != null ? status : ProductStatus.PENDING;

        Specification<Product> spec = Specification
                .where(ProductSpecification.hasStatus(effectiveStatus))
                .and(ProductSpecification.hasCategory(category))
                .and(ProductSpecification.postedBy(postedBy))
                .and(ProductSpecification.hasRouteStop(routeStopId))
                .and(ProductSpecification.hasWeightBetween(weightMin, weightMax))
                .and(ProductSpecification.createdBetween(createdFrom, createdTo));

        User currentUser = currentUserService.getCurrentUser();

        Page<Product> page = productRepository.findAll(spec, pageable);

        List<ProductResponseDTO> data = page.stream()
                .map(product -> toResponseWithPolicy(product, currentUser))
                .toList();

        return PaginatedResponseFactory.from(page, data);
    }

    // ========================================================================
    // LIST CREATED PRODUCTS
    // ========================================================================

    @Transactional(Transactional.TxType.SUPPORTS)
    public PaginatedResponse<ProductResponseDTO> getMyProducts(Pageable pageable) {
        User currentUser = currentUserService.getCurrentUser();

        Page<Product> page = productRepository.findByPostedByIdAndStatusNot(
                currentUser.getId(),
                ProductStatus.DELETED,
                pageable
        );

        List<ProductResponseDTO> data = page.stream()
                .map(product -> toResponseWithPolicy(product, currentUser))
                .toList();

        return PaginatedResponseFactory.from(page, data);
    }

    // ========================================================================
    // LIST CLAIMED PRODUCTS
    // ========================================================================

    @Transactional(Transactional.TxType.SUPPORTS)
    public PaginatedResponse<ProductResponseDTO> getMyClaimedProducts(Pageable pageable) {
        User currentUser = currentUserService.getCurrentUser();

        Page<Product> page = productRepository.findByClaimedByIdAndStatus(
                currentUser.getId(),
                ProductStatus.ASSIGNED,
                pageable
        );

        List<ProductResponseDTO> data = page.stream()
                .map(product ->  toResponseWithPolicy(product, currentUser))
                .toList();

        return  PaginatedResponseFactory.from(page, data);
    }

    // ========================================================================
    // Helpers
    // ========================================================================

    private Product findOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private ProductResponseDTO toResponseWithPolicy(Product product, User currentUser) {
        ProductActionPolicy policy =
                ProductActionPolicy.from(product, currentUser);

        return productPresenter.present(product, policy);
    }




    /**
     * Applies image URLs to product respecting JPA orphanRemoval.
     * - null  → do nothing (partial update)
     * - empty → remove all images
     */
    private void applyImages(Product product, List<String> imageUrls) {

        if (imageUrls == null) {
            return;
        }

        product.getImages().clear();

        if (imageUrls.isEmpty()) {
            return;
        }

        List<ProductImage> images = imageUrls.stream()
                .filter(Objects::nonNull)
                .map(url -> ProductImage.builder()
                        .url(url)
                        .product(product)
                        .build())
                .toList();

        product.getImages().addAll(images);
    }

    public void changeStatus(Product product,
                             ProductStatus newStatus,
                             User actor) {

        ProductStatus currentStatus = product.getStatus();

        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new BusinessException(
                    ErrorCode.INVALID_PRODUCT_STATUS_TRANSITION
            );
        }

        product.changeStatus(newStatus);
        productRepository.save(product);

        productStatusHistoryService.recordStatusChange(
                product,
                currentStatus,
                newStatus,
                actor
        );
    }

    // TODO: Consider extracting status transition orchestration
    //       into a dedicated ProductStatusService if lifecycle grows.
}
