-- Fix orphaned DB columns that are no longer in JPA entities
-- These statements are idempotent and safe to run on every startup

-- Make 'active' column nullable with default true (removed from User entity)
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'active') THEN
        ALTER TABLE users ALTER COLUMN active SET DEFAULT true;
        ALTER TABLE users ALTER COLUMN active DROP NOT NULL;
    END IF;
END $$;

-- Drop confirmed_by_admin column (no admin confirmation needed)
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'payments' AND column_name = 'confirmed_by_admin') THEN
        ALTER TABLE payments DROP COLUMN confirmed_by_admin;
    END IF;
END $$;
