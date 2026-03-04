# PowerShell Script to Register a New User Account
# Usage: .\register-user.ps1

# Configuration
$baseUrl = "http://localhost:8082"
$registerEndpoint = "/api/auth/register"

# User registration data
$userData = @{
    username = "testuser"
    email = "testuser@example.com"
    password = "password123"
} | ConvertTo-Json

# Headers
$headers = @{
    "Content-Type" = "application/json"
}

Write-Host "Registering new user account..." -ForegroundColor Cyan
Write-Host "Endpoint: $baseUrl$registerEndpoint" -ForegroundColor Gray
Write-Host ""

try {
    # Send POST request to register endpoint
    $response = Invoke-RestMethod -Uri "$baseUrl$registerEndpoint" `
                                   -Method Post `
                                   -Headers $headers `
                                   -Body $userData `
                                   -ErrorAction Stop
    
    Write-Host "[SUCCESS] Registration successful!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Response:" -ForegroundColor Yellow
    $response | ConvertTo-Json -Depth 10 | Write-Host
    
} catch {
    Write-Host "[ERROR] Registration failed!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Error Details:" -ForegroundColor Yellow
    
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "Status Code: $statusCode" -ForegroundColor Red
        
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $errorBody = $reader.ReadToEnd()
        $reader.Close()
        
        Write-Host "Error Message:" -ForegroundColor Red
        $errorBody | Write-Host
    } else {
        Write-Host $_.Exception.Message -ForegroundColor Red
    }
}
