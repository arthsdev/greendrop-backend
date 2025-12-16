-- ======================================
-- V6__fix_product_and_create_product_images.sql
-- Normalizes product images and aligns schema with JPA entities
-- ======================================

-- 1) Remove legacy JSON column
ALTER TABLE product
    DROP COLUMN image_urls;

-- 2) Add missing business columns
ALTER TABLE product
    ADD COLUMN weight_kg DOUBLE NOT NULL,
    ADD COLUMN quantity DOUBLE NOT NULL,
    ADD COLUMN status VARCHAR(50) NOT NULL;

-- 3) Create product_images table
CREATE TABLE product_images (
    id BINARY(16) NOT NULL PRIMARY KEY,
    product_id BINARY(16) NOT NULL,
    url VARCHAR(500) NOT NULL,

    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_product_images_product
        FOREIGN KEY (product_id)
        REFERENCES product(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- Index for faster joins
CREATE INDEX idx_product_images_product_id
    ON product_images(product_id);
