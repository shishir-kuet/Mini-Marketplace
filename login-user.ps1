# PowerShell Script to Login User and Get JWT Token
# Usage: .\login-user.ps1

param(
    [Parameter(Mandatory=$false)]
    [string]$BaseUrl = "http://localhost:8082"
)

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  User Login" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Prompt for Username
do {
    $Username = Read-Host "Enter Username"
    if ([string]::IsNullOrWhiteSpace($Username)) {
        Write-Host "[ERROR] Username cannot be empty" -ForegroundColor Red
    }
} while ([string]::IsNullOrWhiteSpace($Username))

# Prompt for Password
do {
    $Password = Read-Host "Enter Password" -AsSecureString
    $PasswordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($Password))
    if ([string]::IsNullOrWhiteSpace($PasswordPlain)) {
        Write-Host "[ERROR] Password cannot be empty" -ForegroundColor Red
    }
} while ([string]::IsNullOrWhiteSpace($PasswordPlain))

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Authenticating..." -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$loginEndpoint = "/api/auth/login"

# User login data
$loginData = @{
    username = $Username
    password = $PasswordPlain
} | ConvertTo-Json

# Headers
$headers = @{
    "Content-Type" = "application/json"
}

Write-Host "Request Details:" -ForegroundColor Gray
Write-Host "  Endpoint: $BaseUrl$loginEndpoint" -ForegroundColor Gray
Write-Host "  Username: $Username" -ForegroundColor Gray
Write-Host ""

try {
    # Send POST request to login endpoint
    $response = Invoke-RestMethod -Uri "$BaseUrl$loginEndpoint" `
                                   -Method Post `
                                   -Headers $headers `
                                   -Body $loginData `
                                   -ErrorAction Stop
    
    Write-Host "============================================" -ForegroundColor Green
    Write-Host "  LOGIN SUCCESSFUL!" -ForegroundColor Green
    Write-Host "============================================" -ForegroundColor Green
    Write-Host ""
    
    Write-Host "Server Response:" -ForegroundColor Yellow
    $response | ConvertTo-Json -Depth 10 | Write-Host -ForegroundColor Cyan
    Write-Host ""
    
    # Check if JWT token is present and display it separately
    if ($response.token -or $response.jwt -or $response.accessToken) {
        Write-Host "============================================" -ForegroundColor Green
        Write-Host "  JWT TOKEN" -ForegroundColor Green
        Write-Host "============================================" -ForegroundColor Green
        
        $token = if ($response.token) { $response.token } 
                 elseif ($response.jwt) { $response.jwt }
                 else { $response.accessToken }
        
        Write-Host $token -ForegroundColor Yellow
        Write-Host ""
        Write-Host "Token saved to clipboard (if available)" -ForegroundColor Gray
        
        # Try to copy token to clipboard
        try {
            $token | Set-Clipboard
            Write-Host "Token copied to clipboard!" -ForegroundColor Green
        } catch {
            Write-Host "Could not copy to clipboard" -ForegroundColor Gray
        }
    }
    
    Write-Host ""
    
    # Clean up sensitive data
    $PasswordPlain = $null
    $loginData = $null
    
} catch {
    Write-Host "============================================" -ForegroundColor Red
    Write-Host "  LOGIN FAILED!" -ForegroundColor Red
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
            
            Write-Host "Server Response:" -ForegroundColor Yellow
            $errorBody | Write-Host -ForegroundColor Cyan
            Write-Host ""
            
            # Try to parse error message
            try {
                $errorJson = $errorBody | ConvertFrom-Json
                if ($errorJson.message) {
                    Write-Host "Error Message: $($errorJson.message)" -ForegroundColor Red
                }
            } catch {
                # Could not parse as JSON, already displayed raw error
            }
        } catch {
            Write-Host "Could not read error response body" -ForegroundColor Yellow
        }
    } else {
        Write-Host "Exception Message:" -ForegroundColor Yellow
        Write-Host "$($_.Exception.Message)" -ForegroundColor Cyan
    }
    
    Write-Host ""
    
    # Clean up sensitive data
    $PasswordPlain = $null
    $loginData = $null
    
    exit 1
}
