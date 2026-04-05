# Mini Marketplace - Team Development Setup

## 🐳 Docker Setup for Team Development

This setup ensures all team members work with the same database configuration and can easily share data.

### Quick Start

1. **Start only the database (Recommended for development)**:
   ```bash
   docker compose up postgres -d
   ```

2. **Run your Spring Boot app locally**:
   ```bash
   # Use the local profile
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   
   # Or with your IDE (set active profile to 'local')
   ```

3. **Stop the database**:
   ```bash
   docker compose down
   ```

### Advanced Setup

1. **Full containerized setup** (app + database):
   ```bash
   docker compose up -d
   ```

2. **View logs**:
   ```bash
   docker compose logs -f postgres  # Database logs
   docker compose logs -f app       # Application logs
   ```

### Team Workflow

#### For New Team Members:
```bash
# 1. Clone the repository
git clone https://github.com/shishir-kuet/Mini-Marketplace.git
cd mini-marketplace

# 2. Start the database
docker compose up postgres -d

# 3. Run the application
mvn spring-boot:run -Dspring-boot.run.profiles=local

# 4. Access at http://localhost:8083
```

#### Daily Development:
```bash
# Start working
docker compose up postgres -d
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Stop working
docker compose down
```

### Database Connection Details

- **Host**: localhost 
- **Port**: 5433
- **Database**: mini_marketplace
- **Username**: postgres  
- **Password**: postgres

### Configuration Profiles

We support these common database targets:

- Local host to Docker PostgreSQL: `localhost:5433`
- Docker internal network (when app runs in container): `postgres:5432`
- Legacy teammate setup (if still needed): `localhost:5433`

### Data Persistence

- Database data is persisted in a Docker volume (`postgres_data`)
- Data survives container restarts
- To reset database: `docker compose down -v` (⚠️ This deletes all data!)

### Troubleshooting

1. **Port conflicts**:
   ```bash
    # If port 5433 is busy, change in docker-compose.yml:
   ports:
       - "5434:5432"  # Example alternative host port
   ```

2. **Database connection issues**:
   ```bash
   # Check if container is running
   docker compose ps
   
   # Check logs
   docker compose logs postgres
   ```

3. **Reset everything**:
   ```bash
   docker compose down -v
   docker compose up postgres -d
   ```

### Migration from Individual Setups

**Option 1: Export/Import Data**
```bash
# Export from old database
pg_dump -h localhost -p 5433 -U postgres mini_marketplace > backup.sql

# Import to Docker database
docker compose exec postgres psql -U postgres -d mini_marketplace < backup.sql
```

**Option 2: Start Fresh**
- Let Hibernate recreate tables with `ddl-auto: update`
- Re-register test users

### Why This Approach?

✅ **Benefits over Render/Cloud DB**:
- Faster development (local network)
- Works offline
- No API rate limits
- Free unlimited usage
- Complete control over database

✅ **Benefits over Individual Local DBs**:
- Consistent environment for all team members
- Easy to share test data
- No "works on my machine" issues
- Simple onboarding for new developers