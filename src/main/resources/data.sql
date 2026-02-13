-- Fix orphaned DB columns that are no longer in JPA entities
-- These run on every startup and are idempotent

-- Drop confirmed_by_admin column (no admin confirmation needed)
ALTER TABLE payments DROP COLUMN IF EXISTS confirmed_by_admin;

-- Make 'active' column nullable with default (removed from User entity)
ALTER TABLE users ALTER COLUMN active SET DEFAULT true;
ALTER TABLE users ALTER COLUMN active DROP NOT NULL;
