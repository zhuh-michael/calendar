# Build stage
FROM node:18-alpine AS build
WORKDIR /app

# Copy package files
COPY package*.json ./
RUN npm ci

# Copy source code
COPY . .

# Set production environment variables explicitly
ENV VITE_API_BASE=http://47.100.5.220
ENV VITE_APP_ENV=production
ENV VITE_ENABLE_DEVTOOLS=false
ENV VITE_ENABLE_MOCK=false
ENV VITE_LOG_LEVEL=warn
ENV VITE_APP_TITLE=StarQuest
ENV VITE_APP_VERSION=1.0.0

RUN npm run build

# Production stage
FROM nginx:stable-alpine

# Copy built application
COPY --from=build /app/dist /usr/share/nginx/html

# Copy nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx","-s", "-g", "daemon off;"]



