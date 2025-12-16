-- ======================================
-- V3__create_collection_request.sql
-- Minimal collection_request table.
--
-- Notes:
--   - Contains only IDs + timestamps until the business rules mature.
--   - Referenced by route_stop (required).
-- ======================================

CREATE TABLE collection_request (
    id BINARY(16) NOT NULL PRIMARY KEY,

    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB;