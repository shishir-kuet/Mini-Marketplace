# PowerShell script to fix database constraint

# Set the PostgreSQL password (from application.yaml)
$env:PGPASSWORD = "C"

Write-Host "Fixing database constraint..." -ForegroundColor Yellow

# Drop the old constraint
Write-Host "`nStep 1: Dropping old constraint..." -ForegroundColor Cyan
$result1 = psql -U postgres -d mini_marketplace -p 5433 -c "ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;" 2>&1
Write-Host $result1

# Add the new constraint
Write-Host "`nStep 2: Adding new constraint..." -ForegroundColor Cyan
$result2 = psql -U postgres -d mini_marketplace -p 5433 -c "ALTER TABLE users ADD CONSTRAINT users_role_check CHECK (role IN ('USER', 'ADMIN'));" 2>&1
Write-Host $result2

# Verify the fix
Write-Host "`nStep 3: Verifying constraint..." -ForegroundColor Cyan
$result3 = psql -U postgres -d mini_marketplace -p 5433 -c "SELECT constraint_name, check_clause FROM information_schema.check_constraints WHERE constraint_name = 'users_role_check';" 2>&1
Write-Host $result3

Write-Host "`n[SUCCESS] Database constraint fixed!" -ForegroundColor Green
Write-Host "You can now run the registration script." -ForegroundColor Green
