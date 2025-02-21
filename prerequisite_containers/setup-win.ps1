# PowerShell script for setting up Kafka, Zookeeper, and PostgreSQL with pgvector on Windows 11
# Run this script using: `powershell -ExecutionPolicy Bypass -File .\setup.ps1`

$ErrorActionPreference = "Stop"

Write-Host "🚀 Starting CyberCore environment setup..."

# Ensure Docker is installed
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Host "❌ Docker is not installed. Please install it and retry."
    exit 1
}

# Ensure Docker Desktop is running
$dockerStatus = docker info --format "{{.ServerErrors}}" 2>&1
if ($dockerStatus -match "error during connect") {
    Write-Host "❌ Docker Desktop is not running. Please start it and retry."
    exit 1
}

# Define Docker Compose Configuration
$dockerComposeContent = @"
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
"@

# Save Docker Compose Configuration to a file
$composeFilePath = "$PSScriptRoot\docker-compose.yml"
$dockerComposeContent | Set-Content -Path $composeFilePath -Encoding UTF8

# Start Docker Compose
Write-Host "🛠️ Spinning up services..."
Start-Process -NoNewWindow -Wait -FilePath "docker-compose" -ArgumentList "up -d"

# Wait for PostgreSQL to be ready
Write-Host "⏳ Waiting for PostgreSQL to be ready..."
do {
    Start-Sleep -Seconds 2
    $pgStatus = docker exec $(docker ps -qf "ancestor=ankane/pgvector") pg_isready -U postgres -d cybercore_db 2>&1
} until ($pgStatus -match "accepting connections")

Write-Host "✅ PostgreSQL is ready."

# Enable pgvector extension
Write-Host "📌 Enabling pgvector extension in PostgreSQL..."
$pgvectorSQL = "CREATE EXTENSION IF NOT EXISTS pgvector;"
docker exec -i $(docker ps -qf "ancestor=ankane/pgvector") psql -U postgres -d cybercore_db -c $pgvectorSQL

Write-Host "✅ pgvector extension enabled."

Write-Host "🎉 All services are up and running! Use your Spring Boot app normally."
