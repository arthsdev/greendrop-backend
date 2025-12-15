package br.com.greendrop.backend.domain.repository;

import br.com.greendrop.backend.domain.model.CollectionRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CollectionRequestRepository extends JpaRepository<CollectionRequest, UUID> {
    // simple existsById(UUID) and findAllById(Iterable<UUID>) are inherited
}