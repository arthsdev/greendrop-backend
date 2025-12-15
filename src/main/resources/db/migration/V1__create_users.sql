-- ======================================
-- V1__create_users.sql
-- Create users table using UUID stored as BINARY(16).
--
-- Notes:
--   - UUIDs stored as BINARY(16) give better performance and less disk usage.
--   - created_at/updated_at use DATETIME(6) for sub-millisecond precision.
--   - Authentication/roles should be handled at application level.
-- ======================================

CREATE TABLE users (
    id BINARY(16) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(100) NOT NULL,

    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB;

CREATE INDEX idx_users_email ON users(email);
