# TAF Local Testing - Quick Start Script
# This script helps you build and start the TAF system locally
#
# Usage examples:
#   .\start-taf-local.ps1 -Mode team2            # Start team2 (use existing images)
#   .\start-taf-local.ps1 -Mode team2 -Build     # Build + start team2
#   .\start-taf-local.ps1 -Mode team2 -Restart   # Quick restart team2 containers
#   .\start-taf-local.ps1 -Mode team2 -Stop      # Stop team2 containers (keep data)
#   .\start-taf-local.ps1 -Status                 # Show all container status
#   .\start-taf-local.ps1 -Clean                  # Remove containers + volumes (destructive!)

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet("full", "minimal", "core", "team1", "team2", "team3")]
    [string]$Mode = "full",
    
    [switch]$Build,
    [switch]$Clean,
    [switch]$Stop,
    [switch]$Restart,
    [switch]$Status,
    [switch]$Logs
)

$composeFile = "docker-compose-local-test.yml"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  TAF Local Testing - Quick Start" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Auto-create .env from .env.example if missing
if (-not (Test-Path ".env")) {
    if (Test-Path ".env.example") {
        Copy-Item ".env.example" ".env"
        Write-Host "INFO: .env file created from .env.example. Please review and update the values if needed." -ForegroundColor Yellow
    } else {
        Write-Host "ERROR: .env file not found and .env.example is missing!" -ForegroundColor Red
        exit 1
    }
}

# Check if Docker is running
try {
    docker ps | Out-Null
} catch {
    Write-Host "ERROR: Docker is not running!" -ForegroundColor Red
    Write-Host "Please start Docker Desktop and try again." -ForegroundColor Yellow
    exit 1
}

# Status mode — just show container status and exit
if ($Status) {
    Write-Host "Service Status:" -ForegroundColor Cyan
    docker compose -f $composeFile ps
    exit 0
}

# Clean mode — remove containers AND volumes (MongoDB data lost!)
if ($Clean) {
    Write-Host "Cleaning up (removing containers + volumes)..." -ForegroundColor Yellow
    docker compose -f $composeFile down -v
    Write-Host "Cleanup complete! MongoDB data has been removed." -ForegroundColor Green
    exit 0
}

# Define service groups
$services = @{
    "full" = @("mongodb", "auth", "user", 
               "backend-team1", "testapi-team2", "backend-team2", "backend-team3",
               "selenium-team1", "selenium-team2", "selenium-team3",
               "frontend-team1", "frontend-team2", "frontend-team3")
    "minimal" = @("mongodb", "auth")
    "core" = @("mongodb", "auth", "user")
    "team1" = @("mongodb", "auth", "user", "backend-team1", "selenium-team1", "frontend-team1")
    "team2" = @("mongodb", "auth", "user", "testapi-team2", "backend-team2", "selenium-team2", "frontend-team2")
    "team3" = @("mongodb", "auth", "user", "backend-team3", "selenium-team3", "frontend-team3")
}

$selectedServices = $services[$Mode]

Write-Host "Mode: $Mode" -ForegroundColor Cyan
Write-Host "Services: $($selectedServices -join ', ')`n" -ForegroundColor Gray

# Stop mode — stop containers but keep images/volumes/data
if ($Stop) {
    Write-Host "Stopping $Mode services..." -ForegroundColor Yellow
    docker compose -f $composeFile stop $selectedServices
    Write-Host "Services stopped. Data and images preserved." -ForegroundColor Green
    Write-Host "To start again: .\start-taf-local.ps1 -Mode $Mode" -ForegroundColor Gray
    exit 0
}

# Restart mode — quick restart without rebuilding or phased startup
if ($Restart) {
    Write-Host "Restarting $Mode services..." -ForegroundColor Yellow
    docker compose -f $composeFile restart $selectedServices
    Write-Host "`nRestart complete!" -ForegroundColor Green
    docker compose -f $composeFile ps --format "table {{.Name}}\t{{.Status}}" | Select-Object -First 20
    exit 0
}

# Build if requested
if ($Build) {
    Write-Host "Building services..." -ForegroundColor Yellow
    Write-Host "This may take 15-30 minutes on first run...`n" -ForegroundColor Gray
    
    docker compose -f $composeFile build $selectedServices
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "`nBuild failed! Check errors above." -ForegroundColor Red
        exit 1
    }
    
    Write-Host "`nBuild complete!" -ForegroundColor Green
}

# Start services
Write-Host "`nStarting services..." -ForegroundColor Yellow

# Start in phases for better reliability
Write-Host "  Phase 1: Starting database..." -ForegroundColor Gray
docker compose -f $composeFile up -d mongodb
Start-Sleep -Seconds 10

Write-Host "  Phase 2: Starting core services..." -ForegroundColor Gray
docker compose -f $composeFile up -d auth user
Start-Sleep -Seconds 20

if ($Mode -ne "minimal" -and $Mode -ne "core") {
    Write-Host "  Phase 4: Starting testing backends..." -ForegroundColor Gray
    
    switch ($Mode) {
        "full" {
            docker compose -f $composeFile up -d backend-team1 testapi-team2 backend-team2 backend-team3
        }
        "team1" {
            docker compose -f $composeFile up -d backend-team1
        }
        "team2" {
            docker compose -f $composeFile up -d testapi-team2 backend-team2
        }
        "team3" {
            docker compose -f $composeFile up -d backend-team3
        }
    }
    
    Start-Sleep -Seconds 20
    
    Write-Host "  Phase 5: Starting Selenium grids..." -ForegroundColor Gray
    
    switch ($Mode) {
        "full" {
            docker compose -f $composeFile up -d selenium-team1 selenium-team2 selenium-team3
        }
        "team1" {
            docker compose -f $composeFile up -d selenium-team1
        }
        "team2" {
            docker compose -f $composeFile up -d selenium-team2
        }
        "team3" {
            docker compose -f $composeFile up -d selenium-team3
        }
    }
    
    Start-Sleep -Seconds 10
    
    Write-Host "  Phase 6: Starting frontend applications..." -ForegroundColor Gray
    
    switch ($Mode) {
        "full" {
            docker compose -f $composeFile up -d frontend-team1 frontend-team2 frontend-team3
        }
        "team1" {
            docker compose -f $composeFile up -d frontend-team1
        }
        "team2" {
            docker compose -f $composeFile up -d frontend-team2
        }
        "team3" {
            docker compose -f $composeFile up -d frontend-team3
        }
    }
}

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "  Services Started!" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Green

# Show status
Write-Host "Service Status:" -ForegroundColor Cyan
docker compose -f $composeFile ps

Write-Host "`nAccess Points:" -ForegroundColor Cyan
Write-Host "  MongoDB:         http://localhost:27017" -ForegroundColor White
Write-Host "  Mongo Express:   http://localhost:8881" -ForegroundColor White
Write-Host "  Auth Service:    http://localhost:8081" -ForegroundColor White
Write-Host "  User Service:    http://localhost:8082" -ForegroundColor White

if ($Mode -ne "minimal" -and $Mode -ne "core") {
    switch ($Mode) {
        "full" {
            Write-Host "  Backend Team 1:  http://localhost:8083" -ForegroundColor White
            Write-Host "  Backend Team 2:  http://localhost:8084" -ForegroundColor White
            Write-Host "  Backend Team 3:  http://localhost:8085" -ForegroundColor White
            Write-Host "  TestAPI Team 2:  http://localhost:8086" -ForegroundColor White
            Write-Host "  Selenium Team 1: http://localhost:4444" -ForegroundColor White
            Write-Host "  Selenium Team 2: http://localhost:4445" -ForegroundColor White
            Write-Host "  Selenium Team 3: http://localhost:4446" -ForegroundColor White
            Write-Host "  Frontend Team 1: http://localhost:4200" -ForegroundColor Yellow
            Write-Host "  Frontend Team 2: http://localhost:4300" -ForegroundColor Yellow
            Write-Host "  Frontend Team 3: http://localhost:4400" -ForegroundColor Yellow
        }
        "team1" {
            Write-Host "  Backend Team 1:  http://localhost:8083" -ForegroundColor White
            Write-Host "  Selenium Team 1: http://localhost:4444" -ForegroundColor White
            Write-Host "  Frontend Team 1: http://localhost:4200" -ForegroundColor Yellow
        }
        "team2" {
            Write-Host "  TestAPI Team 2:  http://localhost:8086" -ForegroundColor White
            Write-Host "  Backend Team 2:  http://localhost:8084" -ForegroundColor White
            Write-Host "  Selenium Team 2: http://localhost:4445" -ForegroundColor White
            Write-Host "  Frontend Team 2: http://localhost:4300" -ForegroundColor Yellow
        }
        "team3" {
            Write-Host "  Backend Team 3:  http://localhost:8085" -ForegroundColor White
            Write-Host "  Selenium Team 3: http://localhost:4446" -ForegroundColor White
            Write-Host "  Frontend Team 3: http://localhost:4400" -ForegroundColor Yellow
        }
    }
}

Write-Host "`nUseful Commands:" -ForegroundColor Cyan
Write-Host "  Quick restart:   .\start-taf-local.ps1 -Mode $Mode -Restart" -ForegroundColor Gray
Write-Host "  Stop services:   .\start-taf-local.ps1 -Mode $Mode -Stop" -ForegroundColor Gray
Write-Host "  Check status:    .\start-taf-local.ps1 -Status" -ForegroundColor Gray
Write-Host "  View logs:       docker compose -f $composeFile logs -f" -ForegroundColor Gray
Write-Host "  Rebuild:         .\start-taf-local.ps1 -Mode $Mode -Build" -ForegroundColor Gray
Write-Host "  Full cleanup:    .\start-taf-local.ps1 -Clean" -ForegroundColor Gray

if ($Logs) {
    Write-Host "`nShowing logs (Ctrl+C to exit)..." -ForegroundColor Yellow
    docker compose -f $composeFile logs -f
}

Write-Host "`nDone! Services are starting up..." -ForegroundColor Green
Write-Host "Wait 2-3 minutes for all services to be fully ready.`n" -ForegroundColor Yellow


