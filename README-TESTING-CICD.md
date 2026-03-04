# Testing & CI/CD Setup Guide

## 📋 Table of Contents
- [Unit Tests Overview](#unit-tests-overview)
- [Running Tests Locally](#running-tests-locally)
- [CI/CD Pipeline](#cicd-pipeline)
- [Branch Protection Setup](#branch-protection-setup)
- [Development Workflow](#development-workflow)

## 🧪 Unit Tests Overview

### Test Coverage
The project includes comprehensive unit tests for authentication:

**AuthControllerTest** covers:
- ✅ User Registration
  - Successful registration
  - Duplicate username validation
  - Duplicate email validation
  - Username length validation (min 3, max 50)
  - Email format validation
  - Password length validation (min 6 chars)
  - Blank field validation

- ✅ User Login
  - Successful login with JWT token generation
  - Invalid credentials handling
  - Blank/null field validation
  - User not found scenarios

### Test Technologies
- **JUnit 5**: Testing framework
- **MockMvc**: Spring MVC testing
- **Mockito**: Mocking dependencies
- **Spring Boot Test**: Integration testing support

## 🚀 Running Tests Locally

### Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL 13+ (for integration tests)

### Run All Tests
```bash
# Windows
.\mvnw.cmd test

# Linux/Mac
./mvnw test
```

### Run Specific Test Class
```bash
.\mvnw.cmd test -Dtest=AuthControllerTest
```

### Run with Coverage Report
```bash
.\mvnw.cmd test jacoco:report
# Report will be in: target/site/jacoco/index.html
```

### View Test Results
After running tests, check:
- Console output for summary
- `target/surefire-reports/` for detailed reports
- `target/site/surefire-report.html` for HTML report

## 🔄 CI/CD Pipeline

### Overview
The CI/CD pipeline automatically runs on:
- ✅ Push to any branch
- ✅ Pull request to any branch
- ✅ Before merging (when branch protection is enabled)

### Pipeline Stages

#### 1. **Test Stage**
- Sets up PostgreSQL test database
- Runs all unit and integration tests
- Generates test reports
- Uploads test results as artifacts

#### 2. **Build Stage**
- Runs only if tests pass
- Builds the application JAR
- Uploads build artifacts

#### 3. **Status Check Stage**
- Verifies all previous stages passed
- Provides final CI/CD status

### Workflow Configuration
Location: `.github/workflows/ci-cd.yml`

**Key Features:**
- Automated PostgreSQL setup for tests
- Maven caching for faster builds
- Test result artifacts
- Detailed GitHub Actions summaries
- Fail-fast on test failures

## 🛡️ Branch Protection Setup

### Create Develop Branch
Run the provided script:
```powershell
.\create-develop-branch.ps1
```

This will:
1. Create `develop` branch from current branch
2. Push it to GitHub
3. Provide instructions for branch protection

### Configure Branch Protection on GitHub

#### For `develop` branch:
1. Go to **Settings** → **Branches** → **Add branch protection rule**
2. Branch name pattern: `develop`
3. Enable:
   - ✅ **Require a pull request before merging**
     - Required approvals: 1
   - ✅ **Require status checks to pass before merging**
     - Required checks: `Run Tests`, `Build Application`
   - ✅ **Require branches to be up to date before merging**
   - ✅ **Do not allow bypassing the above settings**

#### For `main`/`master` branch:
Follow the same steps as above but with stricter rules:
- Required approvals: 2
- Include administrators in restrictions

### Testing Branch Protection
1. Create a feature branch: `git checkout -b feature/test`
2. Make changes and push: `git push -u origin feature/test`
3. Create PR on GitHub → develop
4. CI/CD will automatically run
5. PR can only be merged if tests pass ✅

## 💻 Development Workflow

### Recommended Git Flow

```bash
# 1. Start from develop branch
git checkout develop
git pull origin develop

# 2. Create feature branch
git checkout -b feature/your-feature-name

# 3. Make changes and commit
git add .
git commit -m "feat: your feature description"

# 4. Push to remote (triggers CI/CD)
git push -u origin feature/your-feature-name

# 5. Create Pull Request on GitHub
# - Target: develop
# - Wait for CI/CD checks to pass
# - Request review
# - Merge after approval + passing tests

# 6. After merge, delete feature branch
git checkout develop
git pull origin develop
git branch -d feature/your-feature-name
```

### Commit Message Convention
Follow [Conventional Commits](https://www.conventionalcommits.org/):
- `feat:` New feature
- `fix:` Bug fix
- `test:` Adding/updating tests
- `docs:` Documentation changes
- `refactor:` Code refactoring
- `chore:` Maintenance tasks

## 🔍 Viewing CI/CD Results

### On GitHub Actions
1. Go to **Actions** tab in your repository
2. Click on any workflow run
3. View job details, logs, and artifacts

### Test Artifacts
After each CI/CD run, download:
- **test-results**: Surefire test reports
- **test-coverage**: Coverage reports
- **mini-marketplace-jar**: Built application

## 🐛 Troubleshooting

### Tests Failing Locally
```bash
# Clean and rebuild
.\mvnw.cmd clean test

# Check PostgreSQL is running
# Verify application.yaml test configuration
```

### CI/CD Failing on GitHub
1. Check Actions tab for error logs
2. Common issues:
   - Database connection (check service config)
   - Maven dependencies (check pom.xml)
   - Test configuration (check test resources)

### Branch Protection Not Working
1. Verify you've enabled status checks in branch protection
2. Make sure CI/CD workflow has run at least once
3. Check that workflow name matches in branch protection settings

## 📚 Additional Resources

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Spring Boot Testing](https://spring.io/guides/gs/testing-web/)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Branch Protection Rules](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/defining-the-mergeability-of-pull-requests/about-protected-branches)

---

## 🎯 Quick Start Checklist

- [ ] Run tests locally: `.\mvnw.cmd test`
- [ ] Create develop branch: `.\create-develop-branch.ps1`
- [ ] Push CI/CD workflow: `git add .github/workflows/ci-cd.yml`
- [ ] Set up branch protection on GitHub
- [ ] Create test PR to verify CI/CD works
- [ ] ✅ Start developing with confidence!

---

**Last Updated:** March 2026  
**Project:** Mini Marketplace  
**Version:** 1.0.0
