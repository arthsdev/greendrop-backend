package br.com.greendrop.backend.domain.repository;

import br.com.greendrop.backend.domain.model.Route;
import br.com.greendrop.backend.domain.model.enums.RouteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface RouteRepository extends JpaRepository<Route, UUID> {
    List<Route> findByRouteDateAndCollectorId(LocalDate date, UUID collectorId);
    List<Route> findByCollectorIdAndStatus(UUID collectorId, RouteStatus status);
}