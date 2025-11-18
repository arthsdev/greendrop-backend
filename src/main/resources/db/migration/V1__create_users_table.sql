-- ==========================================================
-- V1 - CREATE USERS TABLE
-- ==========================================================
-- This table stores all application users.
-- UUIDs are stored as BINARY(16) for performance and proper indexing.
-- The 'role' field is an ENUM (ADMIN, USER, COLLECTOR) as defined in the Java enum.
-- Geo-location fields are optional.
-- Timestamps are handled automatically by MySQL.
-- ==========================================================

CREATE TABLE users (
    id            BINARY(16) NOT NULL,
    name          VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password      VARCHAR(255) NOT NULL,

    role          VARCHAR(50) NOT NULL,

    cep           VARCHAR(255),
    latitude      DOUBLE,
    longitude     DOUBLE,
    points        INT DEFAULT 0,

    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;
