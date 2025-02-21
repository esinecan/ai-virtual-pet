#!/bin/bash

set -e  # Exit on error
set -o pipefail  # Prevent errors in a pipeline from being masked

echo "Starting CyberCore environment setup..."

# Ensure Docker is installed
if ! command -v docker &> /dev/null; then
    echo "Docker is not installed. Please install it and retry."
    exit 1
fi

# Ensure Docker is running
if ! docker info &> /dev/null; then
    echo "Docker is not running. Please start it and retry."
    exit 1
fi

# Define the adjacent directory for persistent storage
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
VOLUME_PATH="$SCRIPT_DIR/pgdata"

if [ ! -d "$VOLUME_PATH" ]; then
    mkdir -p "$VOLUME_PATH"
fi

# Define Docker Compose Configuration
DOCKER_COMPOSE_YML="$SCRIPT_DIR/docker-compose.yml"
cat <<EOF > "$DOCKER_COMPOSE_YML"
version: '3.8'
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.4.4
    container_name: cybercore_zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - 22181:2181
    volumes:
      - zookeeper-data:/var/lib/zookeeper

  kafka:
    image: confluentinc/cp-kafka:7.4.4
    container_name: cybercore_kafka
    depends_on:
      - zookeeper
    ports:
      - 29092:29092
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092,PLAINTEXT_HOST://localhost:29092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    volumes:
      - kafka-data:/var/lib/kafka/data

  postgres:
    image: ankane/pgvector:latest
    container_name: cybercore_postgres
    environment:
      POSTGRES_DB: cybercore_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - 5432:5432
    volumes:
      - "$VOLUME_PATH:/var/lib/postgresql/data"

volumes:
  zookeeper-data:
  kafka-data:
EOF

# Stop and remove old PostgreSQL container only if it exists
PG_CONTAINER_NAME="cybercore_postgres"
if docker ps -a --format "{{.Names}}" | grep -q "$PG_CONTAINER_NAME"; then
    echo "Stopping and removing old PostgreSQL container..."
    docker stop "$PG_CONTAINER_NAME"
    docker rm "$PG_CONTAINER_NAME"
else
    echo "No old PostgreSQL container found, continuing..."
fi

echo "Starting Docker services..."
docker-compose -f "$DOCKER_COMPOSE_YML" up -d

# Wait for PostgreSQL to actually be running
echo "Waiting for PostgreSQL container to be fully up..."
MAX_WAIT_TIME=60
ELAPSED_TIME=0
while [[ "$(docker inspect --format '{{.State.Status}}' $PG_CONTAINER_NAME 2>/dev/null)" != "running" ]]; do
    sleep 2
    ELAPSED_TIME=$((ELAPSED_TIME + 2))
    if [[ $ELAPSED_TIME -ge $MAX_WAIT_TIME ]]; then
        echo "Error: PostgreSQL container failed to start within $MAX_WAIT_TIME seconds."
        exit 1
    fi
done

echo "PostgreSQL container is now running."

# Wait for PostgreSQL to be ready
echo "Waiting for PostgreSQL service to be ready inside the container..."
until docker exec "$PG_CONTAINER_NAME" pg_isready -U postgres -d cybercore_db &>/dev/null; do
    sleep 2
done

echo "PostgreSQL is ready."

# Enable vector extension
echo "Enabling vector extension in PostgreSQL..."
docker exec -i "$PG_CONTAINER_NAME" psql -U postgres -d cybercore_db -c "CREATE EXTENSION IF NOT EXISTS vector;"

echo "Vector extension enabled."

echo "All services are up and running. You can now start your Spring Boot application."
