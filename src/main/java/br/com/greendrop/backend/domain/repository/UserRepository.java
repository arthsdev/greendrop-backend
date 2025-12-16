package br.com.greendrop.backend.domain.repository;

import br.com.greendrop.backend.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for User entity.
 * Provides basic CRUD operations and custom query methods.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Finds a user by their email.
     * Commonly used during login and registration validation.
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if an email is already registered.
     */
    boolean existsByEmail(String email);
}