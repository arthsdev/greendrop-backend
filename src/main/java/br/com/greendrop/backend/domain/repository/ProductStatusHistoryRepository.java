package br.com.greendrop.backend.domain.repository;


import br.com.greendrop.backend.domain.model.ProductStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductStatusHistoryRepository extends JpaRepository<ProductStatusHistory, UUID> {
}
