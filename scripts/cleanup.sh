#!/bin/bash
set -e

# Define container name
CONTAINER_NAME="cbs-postgres"
IT_CONTAINER_NAME="cbs-it-postgres"

echo "🧹 Starting cleanup of development/test environment..."

# 1. Restart database container to clear connection pool
if docker ps -q -f name=$CONTAINER_NAME | grep -q .; then
    echo "🔄 Restarting $CONTAINER_NAME to clear database connections..."
    docker restart $CONTAINER_NAME
    
    echo "⏳ Waiting for PostgreSQL to be ready..."
    until docker exec $CONTAINER_NAME pg_isready -U cbs -d cbs_config > /dev/null 2>&1; do
        sleep 1
    done
    echo "✅ Database is ready."
else
    echo "⚠️  Container $CONTAINER_NAME is not running. Skipping restart."
fi

# 2. Check for and remove the dedicated IT container if it exists
if docker ps -a -q -f name=$IT_CONTAINER_NAME | grep -q .; then
    echo "🗑️  Removing leftover integration test container ($IT_CONTAINER_NAME)..."
    docker rm -f $IT_CONTAINER_NAME
    echo "✅ Removed $IT_CONTAINER_NAME."
fi

# 3. Clean Maven build artifacts
echo "🧹 Cleaning Maven target directories..."
if command -v mvn &> /dev/null; then
    # Run maven clean in quiet mode
    mvn clean -q -T 1C
    echo "✅ Maven clean complete."
else
    echo "⚠️  Maven not found. Skipping 'mvn clean'."
fi

# 4. Prune dangling images (optional, safe cleanup)
echo "🧹 Pruning dangling docker images (if any)..."
docker image prune -f > /dev/null
echo "✅ Docker images pruned."

echo "✨ Cleanup finished successfully!"
