#!/bin/bash

set -e  # Exit immediately if a command fails
set -u  # Treat unset variables as an error
set -o pipefail  # Fail on first error in a pipeline

echo "🚀 Starting CyberCore environment setup..."

# Ensure Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install it and retry."
    exit 1
fi

# Ensure Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose is not installed. Please install it and retry."
    exit 1
fi

# Define the Docker Compose configuration
COMPOSE_FILE="docker-compose.yml"

cat > "$COMPOSE_FILE" <<EOF
version: '2'
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.4.4
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - 22181:2181
    volumes:
      - zookeeper-data:/var/lib/zookeeper

  kafka:
    image: confluentinc/cp-kafka:7.4.4
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
    environment:
      POSTGRES_DB: cybercore_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - 5432:5432
    volumes:
      - pgdata:/var/lib/postgresql/data
    command: >
      postgres -c shared_preload_libraries='pgvector'

volumes:
  zookeeper-data:
  kafka-data:
  pgdata:
EOF

# Start Docker Compose
echo "🛠️ Spinning up services..."
docker-compose up -d

# Wait for PostgreSQL to be ready
echo "⏳ Waiting for PostgreSQL to be ready..."
until docker exec "$(docker ps -qf 'ancestor=ankane/pgvector')" pg_isready -U postgres -d cybercore_db > /dev/null 2>&1; do
    sleep 2
done
echo "✅ PostgreSQL is ready."

# Enable pgvector extension
echo "📌 Enabling pgvector extension in PostgreSQL..."
docker exec -i "$(docker ps -qf 'ancestor=ankane/pgvector')" psql -U postgres -d cybercore_db <<EOSQL
CREATE EXTENSION IF NOT EXISTS pgvector;
EOSQL
echo "✅ pgvector extension enabled."

echo "🎉 All services are up and running! Use your Spring Boot app normally."
