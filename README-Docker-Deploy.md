# 一键 Docker 部署（StarQuest）

本项目已配置 Docker 支持：后端 Spring Boot 服务 与 前端静态站点（nginx）通过 `docker-compose` 一键构建与启动。

快速开始
1. 在项目根目录运行：

```bash
docker compose build --pull
docker compose up -d
```

2. 访问：
- 前端： http://localhost/  （容器映射到 80 端口）
- 后端 API： http://localhost:8080/api/

注意事项
- 后端使用 SQLite 数据库文件，项目已将本地 `./database` 挂载到容器内的 `/app/database`，请确保 `database/starquest.db` 在本地存在并可被容器用户读取。
- 若需要调试构建过程，可以去掉 `-d` 并观察日志：`docker compose up --build`
- 如果前端需要的 API 前缀不是 `/api`，请修改 `docker-compose.yml` 中 `VITE_API_BASE` 环境变量或前端代码对应的 base URL。

清理
```bash
docker compose down --volumes --remove-orphans
```

进阶
- 你可以把构建好的后端 `target/*.jar` 或前端 `dist` 上传到服务器单独部署；也可以在 CI 中运行 `docker compose build` 并推送镜像到镜像仓库。



