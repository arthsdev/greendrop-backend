package br.com.greendrop.backend.domain.repository;

import br.com.greendrop.backend.domain.model.RouteStop;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface RouteStopRepository extends JpaRepository<RouteStop, UUID> {
    List<RouteStop> findByRouteIdOrderByStopOrderAsc(UUID routeId);
}
