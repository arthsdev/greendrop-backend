-- ======================================
-- V8__create_user_location.sql
-- Create user_location table
--
-- Notes:
--   - One-to-one relationship with users
--   - Each user can have only one current location
--   - UUID stored as BINARY(16) for consistency
--   - created_at / updated_at for auditing
-- ======================================

CREATE TABLE user_location (
    id BINARY(16) NOT NULL PRIMARY KEY,

    user_id BINARY(16) NOT NULL,

    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,

    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_user_location_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_user_location_user
        UNIQUE (user_id)
) ENGINE=InnoDB;
