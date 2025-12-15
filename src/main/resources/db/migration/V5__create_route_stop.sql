-- ======================================
-- V5__create_route_stop.sql
-- Create route_stop table linking route, product and collection_request.
--
-- Notes:
--   - product_id is optional and can be NULL.
--   - ON DELETE behavior:
--       route → CASCADE (delete stops when deleting route)
--       collection_request → RESTRICT (can't delete a request in use)
--       product → SET NULL (product removed but stop remains)
--   - stop_order indexed for route execution ordering.
-- ======================================

CREATE TABLE route_stop (
    id BINARY(16) NOT NULL PRIMARY KEY,

    route_id BINARY(16) NOT NULL,
    collection_request_id BINARY(16) NOT NULL,
    product_id BINARY(16) NULL,

    stop_order INT NOT NULL,

    status VARCHAR(50) NOT NULL,
    completed_at DATETIME(6) NULL,

    failure_type VARCHAR(255) NULL,
    failure_reason TEXT NULL,
    notes TEXT NULL,

    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB;

-- Indexes
CREATE INDEX idx_route_stop_route_id ON route_stop(route_id);
CREATE INDEX idx_route_stop_request_id ON route_stop(collection_request_id);
CREATE INDEX idx_route_stop_product_id ON route_stop(product_id);
CREATE INDEX idx_route_stop_status ON route_stop(status);
CREATE INDEX idx_route_stop_order ON route_stop(stop_order);

-- Foreign keys
ALTER TABLE route_stop
ADD CONSTRAINT fk_route_stop_route
    FOREIGN KEY (route_id) REFERENCES route(id)
    ON DELETE CASCADE;

ALTER TABLE route_stop
ADD CONSTRAINT fk_route_stop_collection_request
    FOREIGN KEY (collection_request_id) REFERENCES collection_request(id)
    ON DELETE RESTRICT;

ALTER TABLE route_stop
ADD CONSTRAINT fk_route_stop_product
    FOREIGN KEY (product_id) REFERENCES product(id)
    ON DELETE SET NULL;
