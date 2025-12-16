-- ======================================
-- V2__create_product.sql
-- Create product table using UUID in BINARY(16).
--
-- Notes:
--   - Storing UUID as BINARY(16) is the most common production best practice.
--   - JSON column used for image_urls for flexibility.
--   - user_id references users table.
--   - created_at/updated_at aligned with Java Instant precision.
-- ======================================

CREATE TABLE product (
    id BINARY(16) NOT NULL PRIMARY KEY,
    user_id BINARY(16) NOT NULL,

    title VARCHAR(255) NOT NULL,
    description TEXT NULL,
    category VARCHAR(100) NOT NULL,

    image_urls JSON NOT NULL,

    is_active TINYINT(1) NOT NULL DEFAULT 1,

    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB;

-- Indexes
CREATE INDEX idx_product_user_id ON product(user_id);
CREATE INDEX idx_product_category ON product(category);
CREATE INDEX idx_product_is_active ON product(is_active);

-- Foreign key
ALTER TABLE product
ADD CONSTRAINT fk_product_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE CASCADE;