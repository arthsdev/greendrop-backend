-- ======================================
-- V4__create_route.sql
-- Create route table supporting logistics workflow.
--
-- Notes:
--   - UUID as BINARY(16) (best practice).
--   - collector_id references users table.
--   - status stored as VARCHAR for enum flexibility.
-- ======================================

CREATE TABLE route (
    id BINARY(16) NOT NULL PRIMARY KEY,

    name VARCHAR(255) NOT NULL,
    route_date DATE NOT NULL,

    collector_id BINARY(16) NULL,
    status VARCHAR(50) NOT NULL,

    expected_start_time DATETIME(6) NULL,
    expected_end_time DATETIME(6) NULL,

    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB;

-- Indexes
CREATE INDEX idx_route_date ON route(route_date);
CREATE INDEX idx_route_status ON route(status);
CREATE INDEX idx_route_collector_id ON route(collector_id);

-- Foreign key
ALTER TABLE route
ADD CONSTRAINT fk_route_collector
    FOREIGN KEY (collector_id) REFERENCES users(id)
    ON DELETE SET NULL;