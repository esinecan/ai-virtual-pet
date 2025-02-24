# PowerShell script for setting up the CyberCore environment on Windows
# Run this script using: `powershell -ExecutionPolicy Bypass -File .\setup-win.ps1`

$ErrorActionPreference = "Stop"

Write-Host "Starting CyberCore environment setup..."
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

Write-Host "Starting Docker services..."
docker-compose up -d

Write-Host "Services are starting up. You can monitor their health status with: docker-compose ps"
Write-Host "Once all services are healthy, the environment will be ready to use."
