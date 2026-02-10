# 一键 Docker 部署（StarQuest）

本项目支持两种部署模式：
- **本地开发**: 前后端本地运行（不使用Docker）
- **生产环境**: 前后端Docker容器化部署

## 🌍 环境配置

### 架构说明
- **本地开发**: 前端连接 `localhost:8080`，后端使用相对路径数据库 `../database/starquest.db`
- **生产环境**: 前后端Docker容器，前端连接backend服务，后端使用容器路径 `/data/database/starquest.db`

### 前端环境变量
**开发环境** (`.env.development`):
- `VITE_API_BASE`: `http://localhost:8080/api`
- `VITE_APP_ENV`: `development`

**生产环境** (`.env.production`):
- `VITE_API_BASE`: `https://www.zhuchenyi.com:8080/api` (Docker网络)
- `VITE_APP_ENV`: `production`

### 后端环境变量
**开发环境** (`application-dev.properties`):
- 数据库: `../database/starquest.db` (相对路径)
- 日志级别: DEBUG
- CORS: 允许localhost开发端口

**生产环境** (`application-prod.properties`):
- 数据库: `/data/database/starquest.db` (容器路径)
- 日志级别: INFO
- 支持环境变量配置敏感信息

## 🚀 快速开始

### 本地开发环境
```bash
# 一键启动前后端开发服务
./dev-start.sh

# 或者分别启动:
# 后端: cd backend && mvn spring-boot:run
# 前端: cd frontend && npm run dev
```

**访问地址**:
- 前端: http://localhost:5173/
- 后端API: http://localhost:8080/api/

### 生产环境部署
```bash
# 1. 构建生产镜像
docker compose build

# 2. 启动服务
docker compose up -d

# 3. 访问应用
# 前端：http://localhost/
# 后端API：http://localhost:8080/api/
```

### 一键构建和推送
```bash
# 构建并推送到阿里云
./build-and-deploy.sh
```

## 🏗️ 架构支持

- ✅ **amd64/x86_64**: 适用于大多数 Intel/AMD 服务器
- ✅ **arm64**: Apple Silicon Mac 和部分 ARM 服务器
- ✅ **多平台镜像**: 支持同时构建多种架构

## 📦 镜像信息

- **后端镜像**: `calendar-backend:latest` (151MB)
  - 架构: linux/amd64
  - 技术栈: Java 17, Spring Boot, SQLite
- **前端镜像**: `calendar-frontend:latest` (26.5MB)
  - 架构: linux/amd64
  - 技术栈: Node.js, Nginx, Vue.js

## 🔧 高级用法

### 手动指定架构构建
```bash
# 构建 amd64 架构 (Intel/AMD 服务器)
DOCKER_DEFAULT_PLATFORM=linux/amd64 docker compose build

# 构建 arm64 架构 (Apple Silicon)
DOCKER_DEFAULT_PLATFORM=linux/arm64 docker compose build

# 构建多架构镜像
docker buildx build --platform linux/amd64,linux/arm64 -t calendar-backend:multi-arch ./backend
```

### 推送到镜像仓库
```bash
# 阿里云容器镜像服务
docker tag calendar-backend:latest your-registry/starquest-backend:v1.1-amd64
docker tag calendar-frontend:latest your-registry/starquest-frontend:v1.1-amd64

docker login --username=yourusername your-registry
docker push your-registry/starquest-backend:v1.1-amd64
docker push your-registry/starquest-frontend:v1.1-amd64
```

## 📋 注意事项

- **数据库**: SQLite 文件存储在 `./database/starquest.db`，会自动挂载到容器
- **端口映射**: 前端 80 → 容器 80，后端 8080 → 容器 8080
- **网络**: 前端自动代理 `/api/*` 请求到后端服务
- **重启策略**: 生产环境配置了 `unless-stopped` 自动重启

## 🧹 清理命令

```bash
# 停止服务
docker compose down

# 清理镜像和数据卷
docker compose down --volumes --rmi all

# 清理构建缓存
docker system prune -f
```

## 🔍 故障排除

### 架构不匹配错误
```
ERROR: image with reference xxx was found but does not match the specified platform
```
**解决方案**: 重新构建指定架构的镜像
```bash
DOCKER_DEFAULT_PLATFORM=linux/amd64 docker compose build --no-cache
```

### 端口占用
```bash
# 检查端口占用
lsof -i :80
lsof -i :8080

# 修改端口映射
# 编辑 docker-compose.yml 中的 ports 配置
```

### 数据库权限问题
```bash
# 确保 database 目录权限正确
chmod 755 database/
chmod 644 database/starquest.db
```



