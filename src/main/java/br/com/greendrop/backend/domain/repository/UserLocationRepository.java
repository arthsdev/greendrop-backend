package br.com.greendrop.backend.domain.repository;

import br.com.greendrop.backend.domain.model.User;
import br.com.greendrop.backend.domain.model.UserLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserLocationRepository extends JpaRepository<UserLocation, UUID> {

    Optional<UserLocation> findByUser(User user);

}
