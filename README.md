# 云记录

> 温和、无竞争的个人记录 + 轻社交 Web 应用。用文字留住今天，用 AI 辅助润色与规划理想，把想做的事拆成一件件能打卡的小事。

**MVP 已交付能力**：邮箱验证码注册/登录（JWT）、记录卡（心情 / 图片 / AI 扩写 / AI 风格推荐）、理想卡（AI 拆解 / 每日打卡 / 打卡日历）、时间胶囊（写信给未来 / 到期开启 / 站内提醒）、个人主页（聚合 + 统计）、基础设置（头像 / 昵称 / 简介 / AI 剩余次数）。

---

## 技术栈

| 端 | 技术 |
|---|---|
| 后端 | Spring Boot 3.5 · JDK 21 · Maven · MyBatis-Plus 3.5 · Spring Security 6 · jjwt 0.12 · Spring AI 1.1.8（DeepSeek）· Knife4j |
| 前端 | Next.js 16（App Router/Turbopack）· React 19 · TypeScript · Tailwind CSS 4 · TanStack Query 5 · Zustand 5 · pnpm |
| 数据库 | MySQL 9.5 |

## 目录结构

```
├── backend/                 # Spring Boot 后端
│   ├── src/main/java/com/cloudrecord/   # common/config/security/controller/service/mapper/entity/dto/vo/ai
│   └── src/main/resources/
│       ├── application.yml              # 公共配置（已提交）
│       ├── application-dev.yml          # 敏感配置（.gitignore，不提交）
│       ├── application-dev.yml.example  # 配置模板（已提交）
│       └── db/schema.sql                # 建表 + mood 种子
├── frontend/                # Next.js 前端
│   ├── app/                 # 页面路由（login/register/record/ideal/capsule/me/settings）
│   ├── components/          # TopNav / RecordForm / IdealForm / Calendar / ImageUploader
│   ├── lib/  store/         # api/auth/record/ideal/capsule 等
│   └── .env.local           # NEXT_PUBLIC_API_BASE（.gitignore）
├── 需求文档/ 方案设计/ 核心功能/ 原型设计/
└── README.md
```

## 本地启动

**前置**：JDK 21、Maven 3.9+、Node.js 20+、pnpm、本机 MySQL 9.5。

### 1. 初始化数据库

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS cloudrecord DEFAULT CHARACTER SET utf8mb4;"
mysql -u root -p cloudrecord < backend/src/main/resources/db/schema.sql
```

### 2. 配置后端

```bash
cp backend/src/main/resources/application-dev.yml.example backend/src/main/resources/application-dev.yml
```

编辑 `application-dev.yml`，填入真实值：

- `spring.datasource.password`：本机 MySQL 密码
- `jwt.secret`：一段 ≥32 字节的随机字符串
- `spring.ai.deepseek.api-key`：DeepSeek API Key（不填则 AI 能力不可用，其余功能不受影响）

### 3. 启动后端（端口 8080）

```bash
cd backend
mvn spring-boot:run
```

API 文档（Knife4j）：http://localhost:8080/doc.html

### 4. 配置并启动前端（端口 3000）

```bash
cd frontend
pnpm install
# .env.local 已含默认值；如后端非本机 8080，改 NEXT_PUBLIC_API_BASE
pnpm dev
```

访问 http://localhost:3000

## 测试

```bash
# 后端（TDD，单元 + 集成，含 H2 与真实 MySQL 场景）
cd backend && mvn test

# 前端（类型检查 + 生产构建）
cd frontend && pnpm build
```

## 配置项速查

| 配置 | 位置 | 说明 |
|---|---|---|
| 数据库连接 | `application-dev.yml` | MySQL `cloudrecord` 库 |
| JWT 密钥 / 有效期 | `application-dev.yml` | access 2h / refresh 14d |
| DeepSeek | `application-dev.yml` | `deepseek-chat`，每场景每日 20 次 |
| 上传目录 | `application.yml` → `app.upload-dir` | 默认 `uploads/`（已 gitignore） |
| 前端 API 地址 | `frontend/.env.local` → `NEXT_PUBLIC_API_BASE` | 默认 `http://localhost:8080/api/v1` |

## 相关文档

- [需求文档](需求文档/需求文档)
- [方案设计文档](需求文档/方案设计文档.md)
- [MVP 开发方案](方案设计/MVP开发方案.md)
- [部署预案](方案设计/部署预案.md)
- [UI 原型](原型设计/)
