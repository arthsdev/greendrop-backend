package br.com.greendrop.backend.domain.service.product;

import br.com.greendrop.backend.domain.model.Product;
import br.com.greendrop.backend.domain.model.User;
import br.com.greendrop.backend.domain.repository.ProductRepository;
import br.com.greendrop.backend.domain.service.product.authorization.ProductAuthorization;
import br.com.greendrop.backend.domain.service.product.rules.ProductRules;
import br.com.greendrop.backend.dto.product.ProductResponseDTO;
import br.com.greendrop.backend.exception.generic.BusinessException;
import br.com.greendrop.backend.exception.global.ErrorCode;
import br.com.greendrop.backend.infrastructure.security.service.CurrentUserService;
import br.com.greendrop.backend.mapper.product.ProductMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductUnclaimService {

    private final ProductRepository productRepository;
    private final CurrentUserService currentUserService;
    private final ProductAuthorization authorization;
    private final ProductRules rules;
    private final ProductMapper productMapper;

    /**
     * Unclaims a product previously claimed by the current collector.
     *
     *   Authorization: only the same collector can unclaim
     *   Domain rules: product must be claimed and assigned
     *   Domain behavior: unclaim
     *   Persist and return response
     */
    public ProductResponseDTO unclaim(UUID productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        User collector = currentUserService.getCurrentUser();

        // Authorization (WHO can do)
        authorization.checkCanUnclaim(product, collector);

        // Domain rules (STATE)
        rules.ensureCanBeUnclaimed(product);
        rules.ensureNotLinkedToRouteStop(product);

        // Domain behavior
        product.unclaim();

        productRepository.save(product);

        log.info(
                "Product unclaimed (productId={}, collectorId={})",
                product.getId(),
                collector.getId()
        );

        return productMapper.toResponse(product);
    }
}
