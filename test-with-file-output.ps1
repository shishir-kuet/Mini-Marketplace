# Test registration and save result to file
$timestamp = (Get-Date).Ticks
$username = "user$timestamp"

$body = @{
    username = $username
    email = "$username@test.com"
    password = "password123"
} | ConvertTo-Json

"Testing registration at $(Get-Date)" | Out-File -FilePath "test-result.txt"
"Username: $username" | Out-File -FilePath "test-result.txt" -Append

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8082/api/auth/register" `
        -Method Post `
        -Body $body `
        -ContentType "application/json" `
        -ErrorAction Stop
    
    "SUCCESS! User registered" | Out-File -FilePath "test-result.txt" -Append
    $response | ConvertTo-Json | Out-File -FilePath "test-result.txt" -Append
    Write-Host "SUCCESS! Check test-result.txt" -ForegroundColor Green
    
} catch {
    "FAILED: $($_.Exception.Message)" | Out-File -FilePath "test-result.txt" -Append
    "Status: $($_.Exception.Response.StatusCode.value__)" | Out-File -FilePath "test-result.txt" -Append
    Write-Host "FAILED! Check test-result.txt" -ForegroundColor Red
}
