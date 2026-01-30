package br.com.greendrop.backend.domain.service.product;

import br.com.greendrop.backend.domain.model.Product;
import br.com.greendrop.backend.domain.model.User;
import br.com.greendrop.backend.domain.model.enums.ProductStatus;
import br.com.greendrop.backend.domain.repository.ProductRepository;
import br.com.greendrop.backend.domain.service.product.authorization.ProductAuthorization;
import br.com.greendrop.backend.domain.service.product.rules.ProductRules;
import br.com.greendrop.backend.dto.product.ProductDetailResponseDTO;
import br.com.greendrop.backend.exception.generic.BusinessException;
import br.com.greendrop.backend.exception.global.ErrorCode;
import br.com.greendrop.backend.infrastructure.security.service.CurrentUserService;
import br.com.greendrop.backend.presentation.product.ProductPresenter;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductClaimService {

    private final ProductRepository productRepository;
    private final CurrentUserService currentUserService;
    private final ProductAuthorization authorization;
    private final ProductRules rules;
    private final ProductPresenter presenter;

    /**
     * Claims a product for the current collector.
     */
    public ProductDetailResponseDTO claim(UUID productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        User collector = currentUserService.getCurrentUser();

        // -------------------------------------------------
        // Authorization (WHO)
        // -------------------------------------------------
        authorization.checkCanClaimProduct(product, collector);

        // -------------------------------------------------
        // Domain rules (STATE)
        // -------------------------------------------------
        rules.ensureCanBeClaimed(product);
        rules.ensureNotLinkedToRoute(product);

        // -------------------------------------------------
        // State mutation
        // -------------------------------------------------
        product.assignCollector(collector);
        product.changeStatus(ProductStatus.ASSIGNED);

        productRepository.save(product);

        log.info(
                "Product claimed (productId={}, collectorId={})",
                product.getId(),
                collector.getId()
        );

        return presenter.toDetail(product);
    }
}
