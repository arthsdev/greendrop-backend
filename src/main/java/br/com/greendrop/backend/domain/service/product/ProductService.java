package br.com.greendrop.backend.domain.service.product;

import br.com.greendrop.backend.domain.model.Product;
import br.com.greendrop.backend.domain.model.ProductImage;
import br.com.greendrop.backend.domain.model.User;
import br.com.greendrop.backend.domain.model.enums.ProductStatus;
import br.com.greendrop.backend.domain.repository.ProductRepository;
import br.com.greendrop.backend.domain.service.product.authorization.ProductAuthorization;
import br.com.greendrop.backend.domain.service.product.policy.ProductActionPolicy;
import br.com.greendrop.backend.domain.service.product.rules.ProductRules;
import br.com.greendrop.backend.domain.service.product.specification.ProductSpecification;
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

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CurrentUserService currentUserService;

    private final ProductAuthorization authorization;
    private final ProductRules rules;
    private final ProductStatusHistoryService productStatusHistoryService;
    private final ProductPresenter productPresenter;

    // ========================================================
    // CREATE
    // ========================================================

    public ProductDetailResponseDTO create(ProductCreateDTO dto) {
        if (dto == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        User currentUser = currentUserService.getCurrentUser();

        authorization.checkCanCreateProduct(currentUser);
        rules.validateCategoryForRole(currentUser, dto.category());

        Product product = productMapper.toEntity(dto);
        product.postBy(currentUser);

        applyImages(product, dto.imageUrls());

        productRepository.save(product);

        log.info("Product created (id={}, postedBy={})", product.getId(), currentUser.getId());

        return productPresenter.toDetail(product);
    }

    // ========================================================
    // READ
    // ========================================================

    @Transactional(Transactional.TxType.SUPPORTS)
    public ProductDetailResponseDTO getById(UUID id) {
        return productPresenter.toDetail(findOrThrow(id));
    }

    // ========================================================
    // UPDATE
    // ========================================================

    public ProductDetailResponseDTO update(UUID id, ProductUpdateDTO dto) {
        if (dto == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        Product product = findOrThrow(id);
        User currentUser = currentUserService.getCurrentUser();

        authorization.checkCanModifyProduct(product, currentUser);
        rules.ensureNotLinkedToRoute(product);
        rules.validateCategoryForRole(currentUser, dto.category());

        productMapper.updateEntityFromDTO(dto, product);
        applyImages(product, dto.imageUrls());

        log.info("Product updated (id={}, by={})", product.getId(), currentUser.getId());

        return productPresenter.toDetail(product);
    }

    // ========================================================
    // DELETE (soft)
    // ========================================================

    public void softDelete(UUID id) {
        Product product = findOrThrow(id);
        User currentUser = currentUserService.getCurrentUser();

        authorization.checkCanModifyProduct(product, currentUser);
        rules.ensureNotLinkedToRoute(product);

        changeStatus(product, ProductStatus.DELETED, currentUser);

        log.info("Product soft-deleted (id={}, by={})", product.getId(), currentUser.getId());
    }

    // ========================================================
    // LIST / FILTER
    // ========================================================
    @Transactional(Transactional.TxType.SUPPORTS)
    public PaginatedResponse<ProductListResponseDTO> listProducts(
            ProductFilterDTO filter,
            Pageable pageable
    ) {
        ProductStatus status = filter.status() != null
                ? filter.status()
                : ProductStatus.PENDING;

        Specification<Product> spec = Specification
                .where(ProductSpecification.hasStatus(status))
                .and(ProductSpecification.hasCategory(filter.category()))
                .and(ProductSpecification.postedBy(filter.postedBy()))
                .and(ProductSpecification.hasRouteStop(filter.routeStopId()))
                .and(ProductSpecification.hasWeightBetween(filter.weightMin(), filter.weightMax()))
                .and(ProductSpecification.createdBetween(filter.createdFrom(), filter.createdTo()));

        User currentUser = currentUserService.getCurrentUser();

        Page<Product> page = productRepository.findAll(spec, pageable);

        List<ProductListResponseDTO> data = page.stream()
                .map(product ->
                        productPresenter.toList(
                                product,
                                ProductActionPolicy.from(product, currentUser)
                        )
                )
                .toList();

        return PaginatedResponseFactory.from(page, data);
    }

    // ========================================================================
    // LIST CREATED PRODUCTS
    // ========================================================================
    @Transactional(Transactional.TxType.SUPPORTS)
    public PaginatedResponse<ProductListResponseDTO> getMyProducts(Pageable pageable) {
        User currentUser = currentUserService.getCurrentUser();

        Page<Product> page = productRepository.findByPostedByIdAndStatusNot(
                currentUser.getId(),
                ProductStatus.DELETED,
                pageable
        );

        List<ProductListResponseDTO> data = page.stream()
                .map(product ->
                        productPresenter.toList(
                                product,
                                ProductActionPolicy.from(product, currentUser)
                        )
                )
                .toList();

        return PaginatedResponseFactory.from(page, data);
    }

    // ========================================================================
    // LIST CLAIMED PRODUCTS
    // ========================================================================
    @Transactional(Transactional.TxType.SUPPORTS)
    public PaginatedResponse<ProductListResponseDTO> getMyClaimedProducts(Pageable pageable) {
        User currentUser = currentUserService.getCurrentUser();

        Page<Product> page = productRepository.findByClaimedByIdAndStatus(
                currentUser.getId(),
                ProductStatus.ASSIGNED,
                pageable
        );

        List<ProductListResponseDTO> data = page.stream()
                .map(product ->
                        productPresenter.toList(
                                product,
                                ProductActionPolicy.from(product, currentUser)
                        )
                )
                .toList();

        return PaginatedResponseFactory.from(page, data);
    }


    // ========================================================
    // HELPERS
    // ========================================================

    private Product findOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private void applyImages(Product product, List<String> imageUrls) {
        if (imageUrls == null) return;

        List<ProductImage> images = imageUrls.stream()
                .filter(Objects::nonNull)
                .map(url -> ProductImage.builder()
                        .url(url)
                        .product(product)
                        .build())
                .toList();

        product.replaceImages(images);
    }

    // TODO: REMOVE THIS METHOD AND EXTRACT TO A DEDICATED CHANGE STATUS SERVICE
    private void changeStatus(Product product, ProductStatus newStatus, User actor) {
        ProductStatus currentStatus = product.getStatus();

        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new BusinessException(ErrorCode.INVALID_PRODUCT_STATUS_TRANSITION);
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
}
