# Mini Marketplace

A Spring Boot-based mini marketplace application with JWT authentication and PostgreSQL database.

## 🚀 Quick Start

### Using Docker (Recommended for Team Development)

```bash
# 1. Start the database
docker-compose up postgres -d

# 2. Run the application  
mvn spring-boot:run

# 3. Access at http://localhost:8082
```

### Manual Setup

```bash
# 1. Set up PostgreSQL database
# 2. Configure application.yaml 
# 3. Run: mvn spring-boot:run
```

## 📁 Project Structure

```
├── src/                    # Source code
├── docs/                   # Documentation
│   ├── DOCKER_SETUP.md     # Docker setup guide
│   ├── JWT_TESTING_GUIDE.md # JWT testing guide  
│   └── README-TESTING-CICD.md # CI/CD documentation
├── scripts/                # Utility scripts
│   ├── docker-helper.sh    # Docker management script
│   └── create-develop-branch.ps1 # Git workflow script
├── sql/                    # Database scripts
│   └── fix-database-constraint.sql
├── docker-compose.yml      # Docker services
├── Dockerfile              # App containerization
└── pom.xml                 # Maven configuration
```

## 🛠 Technology Stack

- **Backend**: Spring Boot 3.3.2
- **Security**: Spring Security + JWT
- **Database**: PostgreSQL 13
- **Build Tool**: Maven
- **Containerization**: Docker & Docker Compose
- **Java Version**: 17

## 📖 Documentation

- **[Docker Setup Guide](docs/DOCKER_SETUP.md)** - Team development environment
- **[JWT Testing Guide](docs/JWT_TESTING_GUIDE.md)** - API authentication testing
- **[CI/CD Guide](docs/README-TESTING-CICD.md)** - Continuous integration setup

## 🔗 API Endpoints

- **Auth**: `/api/auth/register`, `/api/auth/login`
- **Products**: `/api/products`, `/api/products/{id}`, `/api/products/search`
- **Orders**: `/api/orders`, `/api/orders/my`, `/api/orders/seller-orders`
- **Reviews**: `/api/reviews/product/{productId}`, `/api/reviews/product/{productId}/summary`, `/api/reviews/my`
- **Health**: `/actuator/health`

## 🏗 Development Workflow

1. **Setup**: Follow [Docker Setup Guide](docs/DOCKER_SETUP.md)
2. **Development**: Create feature branches from `develop`
3. **Testing**: Use scripts in `scripts/` directory  
4. **Review**: Create PR to `develop` branch
5. **Deploy**: Merge `develop` → `main`

## 👥 Team Members

- Student ID: 2107029, 2107027

## 🚀 Getting Started for New Developers

```bash
git clone https://github.com/shishir-kuet/Mini-Marketplace.git
cd mini-marketplace
docker-compose up postgres -d
mvn spring-boot:run
```

Visit [docs/DOCKER_SETUP.md](docs/DOCKER_SETUP.md) for detailed setup instructions.