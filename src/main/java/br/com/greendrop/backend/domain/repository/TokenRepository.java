package br.com.greendrop.backend.domain.repository;

import br.com.greendrop.backend.domain.model.Token;

import java.util.Optional;

/**
 * Domain-level repository abstraction for managing refresh tokens.
 * Keeps the interface independent of any persistence implementation (e.g. Redis).
 */
public interface TokenRepository {

    void save(Token token);

    Optional<Token> findByToken(String token);

    void deleteByUserId(String userId);
}
