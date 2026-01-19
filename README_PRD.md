# StarQuest — PRD 对照版 README

此文档为基于 `prd.txt` 的重写 README，明确列出已实现的功能、运行步骤、测试账号与未完成项，便于验收与交付。

项目目录（高层）
- `backend/` — Spring Boot 后端（端口 8080）
- `frontend/kid/` — 学员端 Vue 应用（dev 端口 5173）
- `frontend/parent/` — 家长端 Vue 应用（dev 端口 5174）
- `database/` — SQLite 数据文件（`starquest.db`）
- `prd.txt` — 产品需求说明

一、PRD 功能对照（概要）
- 登录/鉴权
  - 管理员账号 `admin/password`（初始化）。家长可创建孩子账号或使用 `POST /api/auth/create-kid` 创建。
  - 已实现 JWT（12 小时）并由后端验证。前端将 token 存于 `localStorage.starquest_token` 并在每次请求附带 `Authorization: Bearer <token>`。

- 家长端（Parent）
  - 仪表盘：今日任务完成率、孩子星星余额、待审核任务（基础实现）。
  - 任务管理：CRUD、日历/列表视图、任务模板（寒假模板已在 `data.sql`）。
  - 星星银行/商城：商品管理、购买流程、订单与交易记录、盲盒券类型。
  - 报表：勤奋度曲线与财富分布（基础实现）。

- 学员端（Kid）
  - 登录 + 心情打卡：登录后弹出心情记录（已实现）。
  - 任务流：待办/已完成、完成任务触发动画与音效；需审核任务进入审核中状态。
  - 补给站：商品网格、余额判断、购买确认、扣星逻辑。
  - 幸运屋：后端盲盒抽奖（权重配置、扣费、中奖记录）。

二、运行说明（开发）

1. 启动后端
```bash
cd backend
mvn spring-boot:run
```
后端监听 `8080`。如需安全调试：
```bash
env SPRING_SECURITY_DEBUG=true mvn spring-boot:run
```

2. 启动前端
```bash
# 安装依赖
cd frontend/kid && npm install
cd ../parent && npm install

# 启动学员端 (5173)
cd frontend/kid && VITE_API_BASE=http://localhost:8080 npm run dev -- --host 127.0.0.1 --port 5173

# 启动家长端 (5174)
cd frontend/parent && VITE_API_BASE=http://localhost:8080 npm run dev -- --host 127.0.0.1 --port 5174

# 或从项目根并发启动（workspace 支持）
npm --prefix frontend run dev
```

3. 创建测试账号（推荐通过后端接口，确保密码用 BCrypt 加密）
```bash
sqlite3 database/starquest.db "DELETE FROM users WHERE username='testkid';"
curl -i -X POST "http://localhost:8080/api/auth/create-kid?username=testkid&password=123456&nickname=测试小朋友"
```

4. 登录验证（查看 token）
```bash
curl -v -H "Content-Type: application/json" \
  -d '{"username":"testkid","password":"123456"}' \
  http://localhost:8080/api/auth/login
```
响应包含 `token` 字段。

三、API 概览（重要）
- `POST /api/auth/login` — 登录（返回 token）
- `POST /api/auth/create-kid` — 创建孩子账号
- `GET /api/auth/kids` — 列出孩子账号
- 任务相关：`GET /api/tasks/kid/{kidId}`, `POST /api/tasks/{taskId}/complete`, `POST /api/tasks/from-template` 等
- 商城相关：`GET /api/rewards`, `POST /api/rewards/{id}/purchase`
- 幸运屋：`POST /api/lucky-draw/draw`, `GET /api/lucky-draw/history/{kidId}`

四、已知问题与建议（交付前）
- UI 细节与可访问性需进一步打磨（颜色对比、触控目标、动画性能优化）。
- 增加统一错误响应（`@ControllerAdvice`）与完整的单元/集成测试。
- 考虑将 SQLite 在生产迁移至 MySQL/Postgres 以改进并发与持久化表现。

五、我已完成的工程更改（索引）
- 后端：JWT + 认证过滤器、CORS、AuthController 修改、data.sql/test data 管理
- 前端：统一 `utils/api.js` 添加 token 管理、合并 frontend 工作区、添加迁移脚本
- 文档：新增 `frontend/README.md`、更新 root README（指向本文件）

如需我将本文件替换为仓库根 `README.md`（覆盖原 README），我可以直接执行；或者你也可先审阅并确认无误后让我替换。若同意直接替换，请回复“替换”，我会立即将 `README.md` 用本文件内容替换并提交。


