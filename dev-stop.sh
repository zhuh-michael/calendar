#!/bin/bash

# Local Development Stop Script
# Stops all local development services

echo "🛑 Stopping StarQuest Local Development Environment..."

# Stop backend
if [ -f backend.pid ]; then
    PID=$(cat backend.pid)
    if kill -0 $PID 2>/dev/null; then
        echo "Stopping backend (PID: $PID)..."
        kill $PID
        sleep 2
        if kill -0 $PID 2>/dev/null; then
            echo "Force killing backend..."
            kill -9 $PID
        fi
    fi
    rm -f backend.pid
    echo "✅ Backend stopped"
fi

# Stop frontend
if [ -f frontend.pid ]; then
    PID=$(cat frontend.pid)
    if kill -0 $PID 2>/dev/null; then
        echo "Stopping frontend (PID: $PID)..."
        kill $PID
        sleep 2
        if kill -0 $PID 2>/dev/null; then
            echo "Force killing frontend..."
            kill -9 $PID
        fi
    fi
    rm -f frontend.pid
    echo "✅ Frontend stopped"
fi

echo "🎉 All services stopped"
