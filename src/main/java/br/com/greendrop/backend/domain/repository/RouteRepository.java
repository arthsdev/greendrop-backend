package br.com.greendrop.backend.domain.repository;

import br.com.greendrop.backend.domain.model.Route;
import br.com.greendrop.backend.domain.model.enums.RouteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RouteRepository extends JpaRepository<Route, UUID> {

    Page<Route> findByRouteDateAndCollectorId(LocalDate date, UUID collectorId, Pageable pageable);

    @EntityGraph(attributePaths = "stops")
    Page<Route> findWithStopsByRouteDateAndCollectorId(LocalDate date, UUID collectorId, Pageable pageable);

    List<Route> findByCollectorIdAndStatus(UUID collectorId, RouteStatus status);

    @EntityGraph(attributePaths = "stops")
    Optional<Route> findWithStopsById(UUID id);
}