# Software Engineering Lab Project Rubric

## Full Stack Web Application with DevOps Pipeline

## Objective
Build a small but complete web application using Spring Boot, Thymeleaf, PostgreSQL, Spring Security (role-based access), Docker, GitHub CI/CD, and Render deployment. The goal is not feature richness, but to demonstrate a complete professional software development workflow.

## Team Requirements
- Mandatory 2-person project
- Each team must consist of exactly two members

## Project Theme (Choose One)
- Mini Marketplace
- Book Exchange Platform
- Simple Inventory Management

The system must include Admin, Seller, and Buyer roles (or equivalent role separation).

## Mandatory Functional Requirements

### 1. Authentication and Authorization
Use Spring Security with:
- User Registration
- Login and Logout
- Password encryption (BCrypt)
- Role-based authorization (ADMIN, SELLER, BUYER)

Access must be restricted using method-level security or URL-based configuration.

### 2. REST API Design
Follow REST principles with proper HTTP methods, status codes, and global exception handling.
Minimum:
- 3 Controllers
- CRUD for at least 2 main entities

### 3. Database
Use PostgreSQL with:
- At least 4 tables (example: User, Role, Product, Order)
- Proper relationships (1:M, M:1, M:M)
- JPA

### 4. Testing
Include:
- Unit tests (Service layer)
- Integration tests (Controller layer)

Using:
- JUnit
- Mockito
- SpringBootTest
- MockMvc

Minimum:
- 15 unit tests
- 3 integration tests

Tests must run successfully in CI.

### 5. Dockerization
Include Dockerfile and docker-compose.yml with application and PostgreSQL containers.
Use environment variables (no hardcoded credentials).
The app must run using:
- docker compose up --build

### 6. GitHub Requirements
Use branch strategy:
- main (protected)
- develop
- feature branches

Rules:
- No direct push to main
- Pull request required with at least one review approval

### 7. CI and CD Pipeline
Use GitHub Actions to:
- Build the project
- Run tests
- Deploy to Render automatically from the main branch

### 8. Deployment
Deploy on Render.
Application must be publicly accessible.
Submit:
- Live URL
- GitHub repository link

## Required Deliverables
- GitHub Repository
- README with:
  - project description
  - architecture diagram
  - ER diagram
  - API endpoints
  - run instructions
  - CI and CD explanation
- Deployed URL
- 5-minute demo presentation

## Evaluation Rubrics (100 Marks)

### Architecture and Code Quality (20 marks)
- Layered architecture (5)
- Clean code and naming (5)
- DTO usage (5)
- Exception handling (5)

### Security and Role Management (15 marks)
- Spring Security implemented (5)
- Password encryption (3)
- Role-based access enforced (7)

### Testing (15 marks)
- Unit tests quality (7)
- Integration tests (5)
- Tests run in CI (3)

### Dockerization (10 marks)
- Proper Dockerfile (4)
- docker-compose works (4)
- Environment config handled correctly (2)

### CI and CD and Git Workflow (15 marks)
- Branch protection configured (5)
- GitHub Actions workflow correct (5)
- Automatic deployment working (5)

### Database Design (10 marks)
- Proper entity relationships (10)

### Deployment and Demo (10 marks)
- App runs without error (5)
- Proper demonstration (5)

### Documentation (5 marks)
- Clear README (5)

## Automatic Failure Conditions
- No role-based access control
- Direct push to main branch
- No Dockerization
- Tests not implemented
- App not deployed

## What You Are Being Evaluated On
- Designing a small system
- Clean architecture
- Applying security
- Writing tests
- Using Git professionally
- Configuring CI and CD
- Dockerizing applications
- Deploying production-ready software
