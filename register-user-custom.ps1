# PowerShell Script to Register a New User Account with Interactive Input
# Usage: .\register-user-custom.ps1

param(
    [Parameter(Mandatory=$false)]
    [string]$BaseUrl = "http://localhost:8082"
)

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  User Registration" -ForegroundColor Cyan
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

Write-Host "Registering user..." -ForegroundColor Cyan
Write-Host "Username: $Username" -ForegroundColor Gray
Write-Host "Email:    $Email" -ForegroundColor Gray
Write-Host "Endpoint: $BaseUrl$registerEndpoint" -ForegroundColor Gray
Write-Host ""

try {
    # Send POST request to register endpoint
    $response = Invoke-RestMethod -Uri "$BaseUrl$registerEndpoint" `
                                   -Method Post `
                                   -Headers $headers `
                                   -Body $userData `
                                   -ErrorAction Stop
    
    Write-Host "[SUCCESS] Registration successful!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Response:" -ForegroundColor Yellow
    $response | ConvertTo-Json -Depth 10 | Write-Host
    Write-Host ""
    
    # Clean up sensitive data
    $PasswordPlain = $null
    
} catch {
    Write-Host "[ERROR] Registration failed!" -ForegroundColor Red
    Write-Host ""
    
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "Status Code: $statusCode" -ForegroundColor Red
        
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $errorBody = $reader.ReadToEnd()
            $reader.Close()
            
            Write-Host "Error Details:" -ForegroundColor Yellow
            $errorBody | Write-Host
        } catch {
            Write-Host "Could not read error details" -ForegroundColor Yellow
        }
    } else {
        Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    }
    
    Write-Host ""
    
    # Clean up sensitive data
    $PasswordPlain = $null
    
    exit 1
}
