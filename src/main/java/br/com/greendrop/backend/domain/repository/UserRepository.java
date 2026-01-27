package br.com.greendrop.backend.domain.repository;

import br.com.greendrop.backend.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for User entity.
 * Handles CRUD operations and custom queries with N+1 safe options.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // ====================== FIND BY EMAIL ======================

    /**
     * Finds a user by their email.
     */
    Optional<User> findByEmail(String email);

    // ====================== CHECK IF EMAIL EXISTS ======================

    /**
     * Checks whether the email is already registered.
     * Simple query, no relationships loaded.
     */
    boolean existsByEmail(String email);

    // ====================== FIND ALL USERS PAGINATED ======================

    /**
     * Fetches all users in a paginated way.
     * Loads roles and profile eagerly to prevent N+1 issues.
     * Future relationships can be added in attributePaths without changing service.
     */
    @EntityGraph(attributePaths = {"roles", "profile"})
    @Query("SELECT u FROM User u") // explicit JPQL avoids query derivation error
    Page<User> findAllWithRelations(org.springframework.data.domain.Pageable pageable);

    // ====================== FIND BY ID WITH RELATIONS ======================

    /**
     * Fetches a single user by ID with roles, profile, and location.
     * N+1 safe: everything is loaded in a single query.
     */
    @EntityGraph(attributePaths = {"roles", "profile", "location"})
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithRelations(@Param("id") UUID id);
}
