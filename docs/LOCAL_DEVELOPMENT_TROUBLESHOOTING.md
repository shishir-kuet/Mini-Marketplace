# Troubleshooting Local Development Setup

## Issue: Local App Cannot Connect to Docker Database

### Quick Fix Options:

#### Option 1: Use Legacy Profile (Your Teammate's Setup)
```bash
# If you have PostgreSQL installed locally on port 5433
mvn spring-boot:run -Dspring-boot.run.profiles=legacy
```

#### Option 2: Fix Docker Database Connection
```bash
# 1. Restart Docker database clean
docker-compose down postgres
docker-compose up postgres -d

# 2. Wait 10 seconds, then verify database
docker-compose exec postgres psql -U postgres -c "\l"

# 3. Create database if needed
docker-compose exec postgres createdb -U postgres mini_marketplace

# 4. Start app with explicit config
mvn spring-boot:run \
  -Dspring.datasource.url=jdbc:postgresql://localhost:5432/mini_marketplace \
  -Dspring.datasource.username=postgres \
  -Dspring.datasource.password=postgres
```

#### Option 3: Use Application Properties Override
```bash
# Create application-local.yml with explicit settings
# Then run:
mvn spring-boot:run -Dspring.profiles.active=local
```

### Possible Causes:
1. **Port Conflict**: Port 5432 might be used by local PostgreSQL
2. **Docker Network**: Connection issue between host and container  
3. **Database Initialization**: Database not properly initialized
4. **Maven Configuration**: System properties not passed correctly

### Verification Steps:
1. Check port availability: `Test-NetConnection localhost 5432`
2. Test direct database access: Local tools can connect to `localhost:5432`
3. Check Docker logs: `docker-compose logs postgres`

## Recommended Workflow:
- **Development**: Use local app + Docker database (once fixed)
- **Testing**: Use full Docker stack  
- **CI/CD**: Use full Docker stack
- **Production**: Use full containerized deployment