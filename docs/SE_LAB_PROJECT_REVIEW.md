# SE Lab Rubric-Based Project Review

Date: 2026-04-05
Project: Mini Marketplace
Rubric Source: docs/SE_LAB_PROJECT_RUBRIC.md

## Executive Summary
Estimated score: 67 / 100

Strong points:
- Clear layered backend structure (controller, service, repository)
- Good DTO usage and global exception handling
- Unit test volume is high
- Docker artifacts and CI workflow exist

Major gaps against rubric:
- Required role model is not fully implemented (ADMIN, SELLER, BUYER)
- CI has no automatic Render deployment job from main
- README does not contain all required deliverables
- Database relationship modeling is partly ID-based instead of explicit JPA relationships
- Local test run fails without running Postgres

## Critical Findings (High Priority)

1) Role requirement mismatch with rubric
- Rubric requires ADMIN, SELLER, BUYER (or equivalent clear separation).
- Current code persists only user/admin roles and validates only these two values.
- Evidence:
  - src/main/java/com/__2107027/mini_marketplace/model/User.java (role default comment: user/admin)
  - src/main/java/com/__2107027/mini_marketplace/controller/AuthController.java (default role is user)
  - src/main/java/com/__2107027/mini_marketplace/service/UserService.java (role validation: user/admin only)

2) CI/CD pipeline lacks automatic Render deployment from main
- Rubric requires build + test + deploy automatically from main branch.
- Current ci.yml builds and tests only; no deploy step.
- Evidence:
  - .github/workflows/ci.yml

3) README deliverables are incomplete
- Rubric requires README with architecture diagram, ER diagram, endpoint list, run instructions, CI/CD explanation, plus deployed URL and repository link.
- Current README has basic overview and endpoint summary, but no architecture diagram, no ER diagram, and no explicit deployed URL section.
- Evidence:
  - README.md

## Medium Findings

4) Database relationships are only partially modeled with JPA relations
- Review entity uses @ManyToOne, but core commerce entities rely mostly on scalar foreign key IDs.
- This partially satisfies relationship expectations but weakens the design score.
- Evidence:
  - src/main/java/com/__2107027/mini_marketplace/model/Review.java
  - src/main/java/com/__2107027/mini_marketplace/model/Order.java
  - src/main/java/com/__2107027/mini_marketplace/model/OrderItem.java
  - src/main/java/com/__2107027/mini_marketplace/model/Product.java

5) Docker compose default behavior may not fully match required run command expectation
- Rubric says the app should run with docker compose up --build.
- App service is placed behind profile full-stack, so default command may not start app container.
- Evidence:
  - docker-compose.yml

6) Sensitive defaults are present in Docker profile config
- application-docker.yml contains literal passwords/secrets.
- Rubric expects environment-variable based config with no hardcoded credentials.
- Evidence:
  - src/main/resources/application-docker.yml

## Testing Assessment

7) Test volume requirement is met
- Service-layer test count is well above 15.
- Controller integration tests are above 3 test cases.
- Evidence:
  - src/test/java/com/__2107027/mini_marketplace/service/*.java
  - src/test/java/com/__2107027/mini_marketplace/controller/AuthControllerTest.java

8) Current local run status is not fully green unless DB is available
- Local full suite run observed failures due DB connection refused at localhost:5433 for SpringBootTest classes.
- Summary observed: passed=50, failed=20.
- Evidence:
  - src/test/java/com/__2107027/mini_marketplace/controller/AuthControllerTest.java
  - src/test/java/com/__2107027/mini_marketplace/MiniMarketplaceApplicationTests.java

## Rubric Scoring (Estimated)

1) Architecture and Code Quality (20)
- Layered architecture: 5/5
- Clean code and naming: 4/5
- DTO usage: 5/5
- Exception handling: 4/5
Subtotal: 18/20

2) Security and Role Management (15)
- Spring Security implemented: 5/5
- Password encryption: 3/3
- Role-based access enforced: 3/7 (role model mismatch)
Subtotal: 11/15

3) Testing (15)
- Unit tests quality: 6/7
- Integration tests: 4/5
- Tests run in CI: 2/3 (workflow exists; local run shows env dependency)
Subtotal: 12/15

4) Dockerization (10)
- Proper Dockerfile: 4/4
- docker-compose works: 2/4 (default command behavior concern)
- Environment config handling: 1/2 (hardcoded defaults present)
Subtotal: 7/10

5) CI/CD and Git Workflow (15)
- Branch protection configured: 0/5 (not verifiable from repository files)
- GitHub Actions workflow: 5/5 (build/test present)
- Automatic deployment working: 0/5 (no deploy job in ci.yml)
Subtotal: 5/15

6) Database Design (10)
- Proper entity relationships: 6/10
Subtotal: 6/10

7) Deployment and Demo (10)
- App runs without error: 3/5 (public endpoint appears configured, but not fully revalidated here)
- Proper demonstration: 0/5 (cannot verify from repository)
Subtotal: 3/10

8) Documentation (5)
- Clear README: 5/5 for basic readability, but missing required sections for full marks
Adjusted subtotal: 5/5

Total Estimated: 67/100

## Automatic Failure Conditions Check
- No role-based access control: PASS (role checks exist)
- Direct push to main branch: NOT VERIFIABLE from codebase
- No Dockerization: PASS
- Tests not implemented: PASS
- App not deployed: LIKELY PASS (keep-alive references Render URL)

## Prioritized Action Plan

1) Implement explicit role model for ADMIN, SELLER, BUYER (or document exact equivalent mapping).
2) Add deploy job to .github/workflows/ci.yml for Render auto-deploy from main.
3) Update README to include architecture diagram, ER diagram, full CI/CD explanation, live URL, repo link.
4) Remove hardcoded credentials/secrets from application-docker.yml; rely on env vars.
5) Make docker compose up --build start both app and DB by default.
6) Stabilize integration tests to run without external local DB assumptions (test profile/Testcontainers or CI-style test config).
7) Improve entity relationship modeling with explicit JPA associations where appropriate.
