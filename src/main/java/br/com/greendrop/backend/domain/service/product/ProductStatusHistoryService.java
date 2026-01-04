package br.com.greendrop.backend.domain.service.product;

import br.com.greendrop.backend.domain.model.Product;
import br.com.greendrop.backend.domain.model.ProductStatusHistory;
import br.com.greendrop.backend.domain.model.User;
import br.com.greendrop.backend.domain.model.enums.ProductStatus;
import br.com.greendrop.backend.domain.repository.ProductStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductStatusHistoryService {

    private final ProductStatusHistoryRepository productStatusHistoryRepository;


    public void recordStatusChange(Product product,
                                   ProductStatus from,
                                   ProductStatus to,
                                   User changedBy
                                   ){
        ProductStatusHistory history = ProductStatusHistory.builder()

                .product(product)

                .fromStatus(from)

                .toStatus(to)

                .changedBy(changedBy)

                .build();

        productStatusHistoryRepository.save(history);

    }
}
