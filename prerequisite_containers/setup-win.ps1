# PowerShell script for setting up Kafka, Zookeeper, PostgreSQL with pgvector, and Ollama on Windows 11
# Run this script using: `powershell -ExecutionPolicy Bypass -File .\setup-win.ps1`

$ErrorActionPreference = "Stop"

Write-Host "Starting CyberCore environment setup..."

# Check if Ollama is installed
$ollamaPath = "$env:LOCALAPPDATA\ollama\ollama.exe"
if (-not (Test-Path $ollamaPath)) {
    Write-Host "Installing Ollama..."
    # Download Ollama installer
    $installerUrl = "https://ollama.ai/download/OllamaSetup.exe"
    $installerPath = "$env:TEMP\OllamaSetup.exe"
    Invoke-WebRequest -Uri $installerUrl -OutFile $installerPath

    # Run installer
    Start-Process -Wait -FilePath $installerPath -ArgumentList "/S" # Silent install
    
    # Wait for Ollama service to be available
    Write-Host "Waiting for Ollama service to start..."
    Start-Sleep -Seconds 10
}

# Pull the Mistral model
Write-Host "Pulling Mistral model for Ollama..."
Start-Process -NoNewWindow -Wait -FilePath $ollamaPath -ArgumentList "pull mistral"

# Ensure Docker is installed
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Host "Docker is not installed. Please install it and retry."
    exit 1
}

# Ensure Docker Desktop is running
$dockerStatus = docker info --format "{{.ServerErrors}}" 2>&1
if ($dockerStatus -match "error during connect") {
    Write-Host "Docker Desktop is not running. Please start it and retry."
    exit 1
}

# Define the adjacent directory for persistent storage
$volumePath = "$PSScriptRoot\pgdata"
if (!(Test-Path $volumePath)) {
    New-Item -ItemType Directory -Path $volumePath | Out-Null
}

# Convert Windows path to Docker-compatible format
$dockerVolumePath = $volumePath -replace "\\", "/"

# Define Docker Compose Configuration (Escaping $dockerVolumePath properly)
$dockerComposeContent = @"
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
      - '${dockerVolumePath}:/var/lib/postgresql/data'

volumes:
  zookeeper-data:
  kafka-data:
"@

# Save Docker Compose Configuration to a file
$composeFilePath = "$PSScriptRoot\docker-compose.yml"
$dockerComposeContent | Set-Content -Path $composeFilePath -Encoding UTF8

# Stop and remove old PostgreSQL container only if it exists
$pgContainerName = "cybercore_postgres"
$containerExists = docker ps -a --format "{{.Names}}" | Select-String -Pattern $pgContainerName

if ($containerExists) {
    Write-Host "Stopping and removing old PostgreSQL container..."
    docker stop $pgContainerName
    docker rm $pgContainerName
} else {
    Write-Host "No old PostgreSQL container found, continuing..."
}

Write-Host "Starting Docker services..."
Start-Process -NoNewWindow -Wait -FilePath "docker-compose" -ArgumentList "up -d"

# Wait for PostgreSQL to actually be running
Write-Host "Waiting for PostgreSQL container to be fully up..."
$maxWaitTime = 60
$elapsedTime = 0
do {
    Start-Sleep -Seconds 2
    $containerState = docker inspect --format "{{.State.Status}}" $pgContainerName 2>$null
    $elapsedTime += 2
    if ($elapsedTime -ge $maxWaitTime) {
        Write-Host "Error: PostgreSQL container failed to start within $maxWaitTime seconds."
        exit 1
    }
} until ($containerState -eq "running")

Write-Host "PostgreSQL container is now running."

# Wait for PostgreSQL to be ready
Write-Host "Waiting for PostgreSQL service to be ready inside the container..."
do {
    Start-Sleep -Seconds 2
    $pgStatus = docker exec $pgContainerName pg_isready -U postgres -d cybercore_db 2>&1
} until ($pgStatus -match "accepting connections")

Write-Host "PostgreSQL is ready."

# Enable pgvector extension
Write-Host "Enabling vector extension in PostgreSQL..."
$pgvectorSQL = "CREATE EXTENSION IF NOT EXISTS vector;"
docker exec -i $pgContainerName psql -U postgres -d cybercore_db -c $pgvectorSQL

Write-Host "Vector extension enabled."

Write-Host "All services are up and running. You can now start your Spring Boot application."
