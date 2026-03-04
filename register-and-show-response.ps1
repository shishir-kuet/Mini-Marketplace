# PowerShell Script to Register a User and Display Server Response
# Usage: .\register-and-show-response.ps1

param(
    [Parameter(Mandatory=$false)]
    [string]$BaseUrl = "http://localhost:8082"
)

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  User Registration & Response Viewer" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Prompt for Username
do {
    $Username = Read-Host "Enter Username (3-50 characters)"
    if ($Username.Length -lt 3 -or $Username.Length -gt 50) {
        Write-Host "[ERROR] Username must be between 3 and 50 characters" -ForegroundColor Red
    }
} while ($Username.Length -lt 3 -or $Username.Length -gt 50)

# Prompt for Email
do {
    $Email = Read-Host "Enter Email"
    if ($Email -notmatch "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$") {
        Write-Host "[ERROR] Please enter a valid email address" -ForegroundColor Yellow
    }
} while ($Email -notmatch "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$")

# Prompt for Password
do {
    $Password = Read-Host "Enter Password (minimum 6 characters)" -AsSecureString
    $PasswordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($Password))
    if ($PasswordPlain.Length -lt 6) {
        Write-Host "[ERROR] Password must be at least 6 characters" -ForegroundColor Red
    }
} while ($PasswordPlain.Length -lt 6)

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Sending Registration Request" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$registerEndpoint = "/api/auth/register"

# User registration data
$userData = @{
    username = $Username
    email = $Email
    password = $PasswordPlain
} | ConvertTo-Json

# Headers
$headers = @{
    "Content-Type" = "application/json"
}

Write-Host "Request Details:" -ForegroundColor Gray
Write-Host "  Endpoint: $BaseUrl$registerEndpoint" -ForegroundColor Gray
Write-Host "  Username: $Username" -ForegroundColor Gray
Write-Host "  Email:    $Email" -ForegroundColor Gray
Write-Host ""

try {
    # Send POST request to register endpoint
    $response = Invoke-RestMethod -Uri "$BaseUrl$registerEndpoint" `
                                   -Method Post `
                                   -Headers $headers `
                                   -Body $userData `
                                   -ErrorAction Stop
    
    Write-Host "============================================" -ForegroundColor Green
    Write-Host "  SERVER RESPONSE (Success)" -ForegroundColor Green
    Write-Host "============================================" -ForegroundColor Green
    Write-Host ""
    
    # Print the complete server response
    $response | ConvertTo-Json -Depth 10 | Write-Host -ForegroundColor Cyan
    
    Write-Host ""
    Write-Host "============================================" -ForegroundColor Green
    Write-Host "  Registration completed successfully!" -ForegroundColor Green
    Write-Host "============================================" -ForegroundColor Green
    
    # Clean up sensitive data
    $PasswordPlain = $null
    
} catch {
    Write-Host "============================================" -ForegroundColor Red
    Write-Host "  SERVER RESPONSE (Error)" -ForegroundColor Red
    Write-Host "============================================" -ForegroundColor Red
    Write-Host ""
    
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "HTTP Status Code: $statusCode" -ForegroundColor Yellow
        Write-Host ""
        
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $errorBody = $reader.ReadToEnd()
            $reader.Close()
            
            Write-Host "Server Response Body:" -ForegroundColor Yellow
            $errorBody | Write-Host -ForegroundColor Cyan
        } catch {
            Write-Host "Could not read error response body" -ForegroundColor Yellow
        }
    } else {
        Write-Host "Exception Message:" -ForegroundColor Yellow
        Write-Host "$($_.Exception.Message)" -ForegroundColor Cyan
    }
    
    Write-Host ""
    Write-Host "============================================" -ForegroundColor Red
    Write-Host "  Registration failed!" -ForegroundColor Red
    Write-Host "============================================" -ForegroundColor Red
    
    # Clean up sensitive data
    $PasswordPlain = $null
    
    exit 1
}
