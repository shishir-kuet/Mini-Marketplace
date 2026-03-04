-- Connect to your PostgreSQL database and run these commands:
-- psql -U postgres -d mini_marketplace -p 5433

-- Drop the old constraint
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;

-- Optionally, add a new constraint with correct values
ALTER TABLE users ADD CONSTRAINT users_role_check CHECK (role IN ('USER', 'ADMIN'));

-- Verify the fix
SELECT constraint_name, check_clause 
FROM information_schema.check_constraints 
WHERE constraint_name = 'users_role_check';
