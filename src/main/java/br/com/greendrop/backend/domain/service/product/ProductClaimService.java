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
public class ProductClaimService {

    private final ProductRepository productRepository;
    private final CurrentUserService currentUserService;
    private final ProductAuthorization authorization;
    private final ProductRules rules;
    private final ProductMapper productMapper;

    public ProductResponseDTO claim(UUID productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        User collector = currentUserService.getCurrentUser();

        authorization.checkCanClaimProduct(collector, product);
        rules.ensureCanBeClaimed(product);

        product.claimBy(collector);

        productRepository.save(product);

        log.info(
                "Product claimed (productId={}, collectorId={})",
                product.getId(),
                collector.getId()
        );

        return productMapper.toResponse(product);
    }
}
