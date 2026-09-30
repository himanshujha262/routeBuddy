@echo off
REM ==============================================================================
REM RoutBuddy Smart Mobility Platform - Local Development Launcher
REM ==============================================================================

echo ===============================================================================
echo   Starting RoutBuddy Platform Development Environment...
echo ===============================================================================

REM 1. Check Docker for PostgreSQL + Redis
echo [1/3] Checking Docker Compose infrastructure (PostgreSQL PostGIS + Redis)...
cd infrastructure\docker
docker-compose up -d postgres redis
cd ..\..

REM 2. Start AI Microservice in separate window
echo [2/3] Starting Python AI Microservice on port 8000...
start "RoutBuddy AI Service" cmd /k "cd ai-service && py -3.11 -m uvicorn app.main:app --reload --port 8000"

REM 3. Information Banner
echo ===============================================================================
echo   Infrastructure and AI Service launched!
echo   - PostgreSQL PostGIS: localhost:5432 (routbuddy_db)
echo   - Redis:              localhost:6379
echo   - AI Microservice:    http://localhost:8000/docs
echo ===============================================================================
pause
