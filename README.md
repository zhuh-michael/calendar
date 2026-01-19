# StarQuest 假期大冒险平台

## 项目概述

StarQuest 是面向家庭的游戏化假期任务管理平台，家长负责任务分配与审核，学员通过完成任务获得“星星”，用于补给站消费或参与幸运屋抽奖。

本 README 基于 PRD（`prd.txt`）重写，明确列出已实现功能、运行与测试步骤、以及交付前的未完成项，便于验收。

## 技术栈

- 后端：Java Spring Boot 3.2.x、Spring Security、SQLite  
- 前端：Vue 3（家长端使用 Element Plus，学员端使用 Vant UI）  
- 构建工具：Maven（后端）、Vite（前端）、Node.js

## 环境要求

- 操作系统：macOS / Windows / Linux  
- Java：JDK 17+  
- Node.js：16+  
- Maven：3.6+

## 项目目录（高层）

```
starquest/
├── backend/                 # Spring Boot 后端（端口 8080）
├── frontend/                # monorepo：包含 kid/ 与 parent/ 两个子项目
│   ├── kid/                 # 学员端 Vue 应用（dev 5173）
│   └── parent/              # 家长端 Vue 应用（dev 5174）
├── database/                # SQLite 数据文件（starquest.db）
├── scripts/                 # 辅助脚本（如迁移、清理脚本）
└── prd.txt                  # 产品需求文档
```

## 快速开始（开发）

1. 环境检查
```bash
java -version
node -v && npm -v
mvn -v
```

2. 启动后端
```bash
cd backend
mvn spring-boot:run
```
后端默认监听：`http://localhost:8080`。调试时可启安全日志：
```bash
env SPRING_SECURITY_DEBUG=true mvn spring-boot:run
```

3. 启动前端（单体应用）
```bash
# 安装并启动统一前端应用（位于 frontend/app）
cd frontend/app && npm install

# 启动统一前端（dev 端口 5173）
VITE_API_BASE=http://localhost:8080 npm run dev -- --host 127.0.0.1 --port 5173
```

4. 访问应用（路由区分）
- 学员端入口：http://localhost:5173/kid/login  
- 家长端入口：http://localhost:5173/parent/login  
- 后端 API：http://localhost:8080

## 测试账号（开发）

- 管理员（家长端）：`admin` / `password`（由 `data.sql` 初始化）  
- 预设学员账号（推荐通过 API 重新创建以确保密码 hash 匹配）：
  - `testkid` / `123456`  
  - `xiaoming` / `123456`  
  - `xiaohong` / `123456`

创建测试账号（建议，确保 BCrypt 加密一致）：
```bash
sqlite3 database/starquest.db "DELETE FROM users WHERE username='testkid';"
curl -i -X POST "http://localhost:8080/api/auth/create-kid?username=testkid&password=123456&nickname=测试小朋友"
```

登录验证（查看 token）：
```bash
curl -v -H "Content-Type: application/json" \
  -d '{"username":"testkid","password":"123456"}' \
  http://localhost:8080/api/auth/login
```
响应将包含 `token` 字段（JWT，默认 12 小时），前端将其存储在 `localStorage.starquest_token`，并在后续请求中通过 `Authorization: Bearer <token>` 发送。

## 功能对照（PRD -> 实现状态）
下面按 PRD 模块列出实现情况，便于验收。

- 登录/鉴权
  - 已实现：`POST /api/auth/login` 返回 JWT（12 小时），前端自动附带 token；未授权请求返回 401。

- 家长端（Parent Portal）
  - 仪表盘（Dashboard）：已实现（今日任务完成率、孩子星星余额、待审核计数）。  
  - 任务管理：已实现 CRUD、日历/列表视图、任务模板导入（寒假模板在 `data.sql`）。  
  - 星星银行/商城：已实现商品管理、购买、订单与交易记录。  
  - 报表中心：基础实现（勤奋度曲线、财富分布）。

- 学员端（Kid App）
  - 登录与心情打卡：已实现（登录后弹出心情选择并记录）。  
  - 冒险主页（任务流）：已实现动画与音效、任务完成后记录交易与更新状态。  
  - 补给站（Reward Shop）：已实现商品展示、余额校验、购买流程（含二次确认）。  
  - 幸运屋（Lucky House）：后端盲盒抽奖逻辑已实现（权重配置、扣费、中奖记录）。

注：大多数 PRD 功能已覆盖；交付前建议完成 UI 打磨、无障碍改进与测试覆盖。

## 核心 API（速览）

- 认证：  
  - `POST /api/auth/login`  
  - `POST /api/auth/create-kid`  
  - `GET /api/auth/kids`

- 任务：  
  - `GET /api/tasks/kid/{kidId}`  
  - `POST /api/tasks/{taskId}/complete`  
  - `POST /api/tasks/from-template`

- 商城：  
  - `GET /api/rewards`  
  - `POST /api/rewards/{rewardId}/purchase`

- 幸运屋：  
  - `POST /api/lucky-draw/draw`  
  - `GET /api/lucky-draw/history/{kidId}`

## 数据管理与部署建议

- 数据脚本：`backend/src/main/resources/schema.sql` 与 `data.sql`（使用 `INSERT OR IGNORE` 保持幂等性）。  
- 生产部署前：运行 `clean-test-data.sql` 清理测试数据，并考虑迁移至 MySQL/Postgres 以提升并发能力与稳定性。

## 开发注意事项（关键）

- 并发与事务：所有写操作（扣星、更新任务、盲盒结算）需运行在事务中以保证一致性。  
- 盲盒安全：概率逻辑必须在后端实现，避免前端作弊。  
- CORS：开发阶段允许 `http://localhost:5173` 与 `http://localhost:5174`；生产环境请收紧允许的 Origin。

## 已做的工程改动（索引）

- 后端：添加 JJWT 依赖、`JwtUtil`、`JwtAuthenticationFilter`、更新 `AuthController` 以返回 token、调整 `SecurityConfig`（CORS 与过滤链）。  
- 前端：统一 `utils/api.js`（token 管理 + 401 自动处理）、合并为 monorepo、添加迁移脚本与 wrapper。  
- 文档：新增 `README_PRD.md`（审阅用），并将根 README 更新为 PRD 对照版本。

## 未完与后续改进（建议优先级）

1. 完善 UI 与无障碍（高优先级）  
2. 统一错误响应（`@ControllerAdvice`）与日志标准化（中）  
3. 单元/集成测试与 e2e 脚本（高）  
4. Docker 化、CI/CD（中）  
5. 生产数据库迁移与性能优化（中）

## 下一步（由我代劳）

我可以继续自动化收尾工作（删除临时代码、添加 `ControllerAdvice`、补充测试脚本并提交）。若你同意，请回复“继续收尾”，我会在仓库中提交这些改动；否则按当前说明在本地验证并把任何问题/日志贴过来，我会跟进修复。  

## 家长使用说明（产品/运营视角）
下面为家长端的快速使用手册，面向产品与运营人员，帮助完成日常操作与验收流程。

1) 登录与入口
 - 访问：`http://<host>:5173/parent/login`，使用管理员账号（如 `admin/password`）登录后进入家长仪表盘。

2) 仪表盘（快速概览）
 - 核心指标：今日任务完成率、孩子星星余额总览、待审核任务数量。顶部直接展示快捷操作入口（发布任务、上架商品、使用模板）。
 - 使用场景：先查看“今日完成率”与“待审核”以决定是否需要审核或调整任务/奖励。

3) 任务管理（发布与审核）
 - 路由：`/parent/tasks`，选择孩子与日期后加载该日任务。
 - 创建任务：点击“新建任务”，填写标题、时间、奖励星星、是否需要审核与描述。保存后任务会出现在孩子端相应日期。
 - 审核流：若任务设置“需要审核”，学员完成后进入“待审核”状态，家长在任务列表中可“通过/拒绝”。通过会发放星星并生成交易记录。

4) 任务模板（批量导入）
 - 路由：`/parent/templates`，模板库列出可用模板（如寒假作息）。
 - 一键导入：选择模板、孩子、起止日期后点击“导入”，系统会基于模板批量生成每天的任务（便于快速安排日程）。

5) 星星与小朋友管理
 - 小朋友管理：`/parent/kids`，查看所有孩子账户与余额。
 - 人工调账：点击某个孩子“调整星星”，输入正/负数量并填写备注（例：奖励做家务 +10，惩罚顶嘴 -5）。系统会立即更新余额并写入交易流水（可导出 CSV）。
 - 创建/编辑子账号：家长可直接创建孩子账号（用户名/密码/昵称），或编辑孩子昵称、密码与手动余额。

6) 商城与兑换审批
 - 商品管理：`/parent/rewards`，上架/编辑商品（图片 URL、价格、库存、类型：实物/虚拟/盲盒券）。
 - 兑换申请：学员在商店购买后生成订单，由家长在 `/parent/rewards/approvals` 审核并“确认发货”。确认发货会把订单标记为已兑换（Delivered），并保留发货记录；库存在购买时已扣减（后端处理）。

7) 幸运屋（盲盒）
 - 系统设定：每次抽奖扣除固定星星（默认 20），奖池由后端配置权重（当前 PRD 建议：大吉 5%、中吉 20%、小吉 45%、鼓励 30%）。
 - 运营注意：权重与奖项内容应由产品确认并在后端配置；前端仅负责展示动画与结果。

8) 报表（验收重点）
 - 报表入口：`/parent/analytics`，可查看勤奋度曲线（近7天）、财富分布、今日完成率等，用于评估孩子行为与奖励经济。
 - 验收项：确保“今日完成率”与数据库中任务完成状态一致；检查交易流水是否完整记录所有加/减星操作与兑换订单。

## 学员使用说明（简要）
1) 登录：学员在 `http://<host>:5173/kid/login` 选择头像并输入密码登录。登录后会弹出“今日心情”打卡弹窗，完成打卡会记录到家长端。
2) 冒险主页：展示今日任务流（待办 / 已完成），完成任务会播放音效与烟花动画，若任务无需审核会直接发放星星并记录交易；若需审核则进入家长审核流程。
3) 补给站：学员使用星星兑换商品（余额不足时显示进度），兑换后生成订单，需家长在家长端确认发货后领取实物/权益。
4) 幸运屋：学员可用星星参与盲盒抽奖（动画 + 结果），系统会记录抽奖流水与奖项发放。

## 验收清单（产品验收建议）
- 注册/登录：家长与学员能正确登录并获得 JWT token；token 传递正确并能访问受保护接口。  
- 任务流：家长能创建/编辑/删除任务；学员能完成任务并获得星星；需审核任务走审核流程且家长能通过/拒绝。  
- 星星账务：所有星星变动均在 `transactions` 表有记录（含人工调整、任务奖励、消费、抽奖）。  
- 商城与兑换：商品 CRUD 正常，购买生成订单并扣减库存；家长审批后订单状态变为已兑换（Delivered）。  
- 模板导入：模板能成功导入并在指定日期范围生成任务。  
- 报表：勤奋度/财富/完成率数据与实际任务/交易一致。

我会继续把 README 的 “部署/维护” 章节补充完善（例如备份、数据清理脚本、生产注意事项）。如果没有其他补充，我会把这些内容保存并关闭本项任务。  


