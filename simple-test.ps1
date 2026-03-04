# Simple registration test
$timestamp = Get-Date -Format "yyyyMMddHHmmss"
$username = "user$timestamp"
$body = @{
    username = $username
    email = "$username@example.com"
    password = "password123"
} | ConvertTo-Json

Write-Host "Attempting to register user: $username" -ForegroundColor Cyan

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8082/api/auth/register" `
        -Method Post `
        -Body $body `
        -ContentType "application/json"
    
    Write-Host "`n=== SUCCESS ===" -ForegroundColor Green
    Write-Host "User registered successfully!" -ForegroundColor Green
    Write-Host "`nResponse:" -ForegroundColor Yellow
    $response | ConvertTo-Json
    
} catch {
    Write-Host "`n=== FAILED ===" -ForegroundColor Red
    Write-Host "Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}
