# PowerShell Script to Create and Push Develop Branch
# Usage: .\create-develop-branch.ps1

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Create Develop Branch" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Check if we're in a git repository
if (-not (Test-Path .git)) {
    Write-Host "[ERROR] Not a git repository!" -ForegroundColor Red
    Write-Host "Please run this script from the root of your git repository." -ForegroundColor Yellow
    exit 1
}

# Check if there are uncommitted changes
$status = git status --porcelain
if ($status) {
    Write-Host "[WARNING] You have uncommitted changes:" -ForegroundColor Yellow
    Write-Host $status -ForegroundColor Gray
    Write-Host ""
    $response = Read-Host "Do you want to commit these changes first? (y/n)"
    
    if ($response -eq 'y' -or $response -eq 'Y') {
        git add .
        $commitMsg = Read-Host "Enter commit message"
        git commit -m "$commitMsg"
        Write-Host "[SUCCESS] Changes committed" -ForegroundColor Green
    } else {
        Write-Host "[INFO] Proceeding without committing changes..." -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "Creating develop branch..." -ForegroundColor Cyan

# Check if develop branch already exists locally
$localBranches = git branch --list develop
if ($localBranches) {
    Write-Host "[INFO] Develop branch already exists locally" -ForegroundColor Yellow
    git checkout develop
} else {
    # Create develop branch from current branch
    git checkout -b develop
    Write-Host "[SUCCESS] Develop branch created locally" -ForegroundColor Green
}

Write-Host ""
Write-Host "Pushing develop branch to remote..." -ForegroundColor Cyan

# Check if remote exists
$remotes = git remote
if (-not $remotes) {
    Write-Host "[ERROR] No remote repository configured!" -ForegroundColor Red
    Write-Host "Please add a remote repository first using:" -ForegroundColor Yellow
    Write-Host "  git remote add origin <your-repo-url>" -ForegroundColor Gray
    exit 1
}

# Push develop branch to remote
try {
    git push -u origin develop
    Write-Host ""
    Write-Host "============================================" -ForegroundColor Green
    Write-Host "  SUCCESS!" -ForegroundColor Green
    Write-Host "============================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "✅ Develop branch created and pushed to remote" -ForegroundColor Green
    Write-Host ""
    Write-Host "Next steps:" -ForegroundColor Yellow
    Write-Host "1. Go to your GitHub repository settings" -ForegroundColor White
    Write-Host "2. Navigate to Branches -> Add branch protection rule" -ForegroundColor White
    Write-Host "3. Add protection for 'develop' branch" -ForegroundColor White
    Write-Host "4. Enable 'Require status checks to pass before merging'" -ForegroundColor White
    Write-Host "5. Select the CI/CD workflow as required check" -ForegroundColor White
    Write-Host "6. Enable 'Require pull request reviews before merging'" -ForegroundColor White
    Write-Host ""
} catch {
    Write-Host ""
    Write-Host "[ERROR] Failed to push develop branch" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "Common issues:" -ForegroundColor Yellow
    Write-Host "- Make sure you have push access to the repository" -ForegroundColor Gray
    Write-Host "- Verify your remote URL: git remote -v" -ForegroundColor Gray
    Write-Host "- Check your authentication credentials" -ForegroundColor Gray
    exit 1
}
