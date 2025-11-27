-- ==========================================================
-- V2 - ADD ACTIVE COLUMN TO USERS
-- ==========================================================
ALTER TABLE users
ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;
