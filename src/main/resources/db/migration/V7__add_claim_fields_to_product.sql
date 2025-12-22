-- =====================================================
--  V7__add_claim_fields_to_product.sql
--  Add claim support to product
-- =====================================================

ALTER TABLE product
    ADD COLUMN claimed_by_id BINARY(16) NULL,
    ADD COLUMN claimed_at DATETIME NULL;

-- =====================================================
-- Foreign key
-- =====================================================

ALTER TABLE product
    ADD CONSTRAINT fk_product_claimed_by
        FOREIGN KEY (claimed_by_id)
        REFERENCES `users` (id);

-- =====================================================
-- Indexes for performance
-- =====================================================

CREATE INDEX idx_product_claimed_by_status
    ON product (claimed_by_id, status);
