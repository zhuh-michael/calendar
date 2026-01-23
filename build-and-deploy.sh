#!/bin/bash

# Production build and deploy script for backend + frontend
# Usage: ./build-and-deploy.sh

set -e

echo "🚀 Building StarQuest Production Environment (Backend + Frontend)..."

# Build the production images
echo "📦 Building Docker images..."
DOCKER_DEFAULT_PLATFORM=linux/amd64 docker compose build

# Tag the images
REGISTRY="crpi-ymugfdq7imhhoek8.cn-shanghai.personal.cr.aliyuncs.com/myspace_zhuh"
VERSION="v1.4-prod"

BACKEND_IMAGE="${REGISTRY}/starquest-backend:${VERSION}"
# FRONTEND_IMAGE="${REGISTRY}/starquest-frontend:${VERSION}"

echo "🏷️ Tagging images:"
echo "  Backend: $BACKEND_IMAGE"
# echo "  Frontend: $FRONTEND_IMAGE"

docker tag calendar-backend:latest $BACKEND_IMAGE
# docker tag calendar-frontend:latest $FRONTEND_IMAGE

# Login and push
echo "🔐 Please login to Aliyun Container Registry:"
docker login --username=朱浩michael $REGISTRY

echo "📤 Pushing images..."
docker push $BACKEND_IMAGE
# docker push $FRONTEND_IMAGE

echo "✅ Production environment deployed!"
echo "🌐 Images:"
echo "  Backend: $BACKEND_IMAGE"
# echo "  Frontend: $FRONTEND_IMAGE"
echo ""
echo "🚀 To run on server:"
echo "  docker compose pull && docker compose up -d"
