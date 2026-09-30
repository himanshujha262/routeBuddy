# ==============================================================================
# RoutBuddy Smart Mobility Platform - PowerShell Dev Launcher
# ==============================================================================

Write-Host "===============================================================================" -ForegroundColor Cyan
Write-Host "   Starting RoutBuddy Platform Development Environment (PowerShell)..." -ForegroundColor Cyan
Write-Host "===============================================================================" -ForegroundColor Cyan

# 1. Start Infrastructure
Write-Host "`n[1/3] Bringing up PostgreSQL PostGIS and Redis..." -ForegroundColor Yellow
Push-Location infrastructure\docker
docker-compose up -d postgres redis
Pop-Location

# 2. Check Python AI Service
Write-Host "`n[2/3] Checking Python 3.11 environment..." -ForegroundColor Yellow
$pythonExe = "C:\Users\Shubhi\Python311\python.exe"
if (Test-Path $pythonExe) {
    Write-Host "Python 3.11 detected. Launching AI Service in background..." -ForegroundColor Green
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd ai-service; & '$pythonExe' -m uvicorn app.main:app --reload --port 8000"
} else {
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd ai-service; py -3.11 -m uvicorn app.main:app --reload --port 8000"
}

# 3. Status Summary
Write-Host "`n===============================================================================" -ForegroundColor Cyan
Write-Host "   ROUTBUDDY DEV STACK STATUS" -ForegroundColor Cyan
Write-Host "===============================================================================" -ForegroundColor Cyan
Write-Host "  * PostgreSQL PostGIS   : localhost:5432 (Database: routbuddy_db)" -ForegroundColor White
Write-Host "  * Redis Cache & Geo    : localhost:6379" -ForegroundColor White
Write-Host "  * AI Microservice Docs : http://localhost:8000/docs" -ForegroundColor White
Write-Host "  * Backend API Docs     : http://localhost:8080/swagger-ui.html" -ForegroundColor White
Write-Host "  * Next.js Admin Portal : http://localhost:3000" -ForegroundColor White
Write-Host "===============================================================================`n" -ForegroundColor Cyan
