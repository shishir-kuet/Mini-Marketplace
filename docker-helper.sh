#!/bin/bash

# Mini Marketplace Docker Helper Script

COMPOSE_FILE="docker-compose.yml"

show_usage() {
    echo "Mini Marketplace Docker Helper"
    echo ""
    echo "Usage: $0 [command]"
    echo ""
    echo "Commands:"
    echo "  start       - Start database only (recommended for development)"
    echo "  full        - Start database + application"
    echo "  stop        - Stop all containers"
    echo "  restart     - Restart database"
    echo "  logs        - Show database logs"
    echo "  reset       - Reset database (⚠️  DELETES ALL DATA!)"
    echo "  status      - Show container status"
    echo "  connect     - Connect to database via psql"
    echo "  backup      - Create database backup"
    echo "  restore     - Restore database from backup"
    echo ""
}

start_db_only() {
    echo "🚀 Starting PostgreSQL database..."
    docker-compose up postgres -d
    echo "✅ Database started on localhost:5432"
    echo "💡 Run your app with: mvn spring-boot:run -Dspring-boot.run.profiles=local"
}

start_full_stack() {
    echo "🚀 Starting full application stack..."
    docker-compose --profile full-stack up -d
    echo "✅ Full stack started"
    echo "📱 Application: http://localhost:8082"
    echo "🗄️  Database: localhost:5432"
}

stop_containers() {
    echo "🛑 Stopping all containers..."
    docker-compose down
    echo "✅ All containers stopped"
}

restart_db() {
    echo "🔄 Restarting database..."
    docker-compose restart postgres
    echo "✅ Database restarted"
}

show_logs() {
    echo "📋 Showing database logs (Ctrl+C to exit)..."
    docker-compose logs -f postgres
}

reset_database() {
    read -p "⚠️  This will DELETE ALL DATABASE DATA! Are you sure? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "🗑️  Resetting database..."
        docker-compose down -v
        docker-compose up postgres -d
        echo "✅ Database reset complete"
    else
        echo "❌ Reset cancelled"
    fi
}

show_status() {
    echo "📊 Container Status:"
    docker-compose ps
    echo ""
    echo "📊 Docker Volume Status:"
    docker volume ls | grep mini-marketplace
}

connect_db() {
    echo "🔗 Connecting to database..."
    echo "💡 Use \\q to exit psql"
    docker-compose exec postgres psql -U postgres -d mini_marketplace
}

backup_db() {
    BACKUP_FILE="backup_$(date +%Y%m%d_%H%M%S).sql"
    echo "💾 Creating database backup: $BACKUP_FILE"
    docker-compose exec postgres pg_dump -U postgres mini_marketplace > "$BACKUP_FILE"
    echo "✅ Backup created: $BACKUP_FILE"
}

restore_db() {
    if [ -z "$2" ]; then
        echo "❌ Please specify backup file: $0 restore backup.sql"
        exit 1
    fi
    
    BACKUP_FILE="$2"
    if [ ! -f "$BACKUP_FILE" ]; then
        echo "❌ Backup file not found: $BACKUP_FILE"
        exit 1
    fi
    
    echo "📥 Restoring database from: $BACKUP_FILE"
    docker-compose exec -T postgres psql -U postgres -d mini_marketplace < "$BACKUP_FILE"
    echo "✅ Database restored"
}

# Main script logic
case "$1" in
    "start")
        start_db_only
        ;;
    "full")
        start_full_stack
        ;;
    "stop")
        stop_containers
        ;;
    "restart")
        restart_db
        ;;
    "logs")
        show_logs
        ;;
    "reset")
        reset_database
        ;;
    "status")
        show_status
        ;;
    "connect")
        connect_db
        ;;
    "backup")
        backup_db
        ;;
    "restore")
        restore_db "$@"
        ;;
    *)
        show_usage
        ;;
esac