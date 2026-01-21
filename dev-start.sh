#!/bin/bash

# Local Development Startup Script
# Starts backend and frontend locally (not in Docker)

echo "🚀 Starting StarQuest Local Development Environment..."

# Function to start backend
start_backend() {
    echo "📦 Starting backend server..."
    cd backend
    
    # Set development profile
    export SPRING_PROFILES_ACTIVE=dev
    
    # Start Spring Boot in background
    nohup mvn spring-boot:run > ../logs/backend.log 2>&1 &
    echo $! > ../backend.pid
    
    cd ..
    echo "✅ Backend started (PID: $(cat backend.pid))"
}

# Function to start frontend  
start_frontend() {
    echo "🎨 Starting frontend dev server..."
    cd frontend
    
    # Start Vite dev server in background
    nohup npm run dev > ../logs/frontend.log 2>&1 &
    echo $! > ../frontend.pid
    
    cd ..
    echo "✅ Frontend started (PID: $(cat frontend.pid))"
}

# Create logs directory
mkdir -p logs

# Start services
start_backend
sleep 5  # Wait for backend to start
start_frontend

echo ""
echo "🌐 Access URLs:"
echo "Frontend: http://localhost:5173/"
echo "Backend API: http://localhost:8080/api/"
echo ""
echo "📝 Logs:"
echo "Backend: tail -f logs/backend.log"  
echo "Frontend: tail -f logs/frontend.log"
echo ""
echo "🛑 To stop: ./dev-stop.sh"
