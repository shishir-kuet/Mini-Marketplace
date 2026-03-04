# Quick test script for registration
$body = @{
    username = "quicktest"
    email = "quicktest@example.com"
    password = "password123"
} | ConvertTo-Json

Write-Host "Testing registration endpoint..." -ForegroundColor Cyan
Write-Host ""

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8082/api/auth/register" `
                                   -Method Post `
                                   -ContentType "application/json" `
                                   -Body $body

    Write-Host "[SUCCESS] Registration worked!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Response:" -ForegroundColor Yellow
    $response | ConvertTo-Json -Depth 10
    
} catch {
    Write-Host "[FAILED] Registration failed" -ForegroundColor Red
    Write-Host "Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $errorBody = $reader.ReadToEnd()
            $reader.Close()
            Write-Host "Error: $errorBody" -ForegroundColor Red
        } catch {
            Write-Host "Could not read error details" -ForegroundColor Yellow
        }
    }
}
