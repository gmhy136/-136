# 《云记录》MVP 开发方案

> 版本：v1.0　|　日期：2026-10-13　|　定位：执行视角的开发路线图
> 承接文档：[需求分析文档](../需求文档/需求文档)、[方案设计文档](../需求文档/方案设计文档.md)
> 本文档聚焦「核心功能」的实现思路与实行路线；数据库/接口/Prompt 明细见方案设计文档 §5/§7/§8，本文不重复展开。

---

## 1. 文档定位

本方案是「照着就能写代码」的执行蓝图，回答三个问题：

1. **做什么**：MVP 核心功能的边界与清单；
2. **怎么做**：每个核心功能的实现思路（前后端 + 关键难点）；
3. **按什么顺序做**：实行路线（阶段、依赖、完成标准）。

---

## 2. 技术栈定稿

| 端 | 定稿 |
|---|---|
| 后端 | Spring Boot 3.5.x + JDK 21 + Maven + MyBatis-Plus 3.5（`mybatis-plus-spring-boot3-starter`）+ Spring Security 6 + jjwt 0.12 + Knife4j 4.5 |
| AI | Spring AI 1.1.8（`spring-ai-starter-model-deepseek` 原生接入 DeepSeek `deepseek-chat`） |
| 前端 | Next.js 16（App Router）+ React 19 + TypeScript + Tailwind CSS 4 + shadcn/ui + TanStack Query 5 + Zustand 5 |
| 数据库 | 本机 MySQL 9.5 |
| 工程 | 单仓库 monorepo：`backend/` + `frontend/` |

> 精确 patch 版本在脚手架时按 Maven Central / npm 最新锁定。

---

## 3. MVP 范围（核心功能清单）

| 模块 | 核心功能点 | 明确不做（二期） |
|---|---|---|
| 用户系统 | 邮箱验证码注册、登录、JWT 刷新、个人主页、基础设置 | 关注/粉丝、私信 |
| 记录卡 | 增删改查、心情、AI 扩写、AI 风格推荐、图片上传（1-3 张）、时间线列表 | 留言、指定送达、匿名 |
| 理想卡 | 创建理想、AI 拆解任务、每日打卡、打卡日历、可见性 | — |
| 时间胶囊 | 写信给未来自己、设定开启日期、到期提醒 | 种子信箱 |
| AI 能力 | 扩写/润色、风格推荐、理想拆解（横切，见 §5.5） | 问答游戏、RAG、AI 生图 |

---

## 4. 工程结构（monorepo）

```
e:\CloudRecord\
├── backend/                       # Spring Boot
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/cloudrecord/
│       │   ├── common/            # Result<T>、错误码、全局异常、业务异常
│       │   ├── config/            # Security、MyBatis-Plus、CORS、定时任务、Knife4j
│       │   ├── security/          # JWT 工具、登录过滤器、用户上下文
│       │   ├── controller/        # Auth/User/Record/Ideal/Capsule/Ai/File
│       │   ├── service/           # 业务接口 + impl
│       │   ├── mapper/            # MyBatis-Plus Mapper
│       │   ├── entity/ dto/ vo/
│       │   └── ai/                # AiService、prompt 模板、结构化输出转换器
│       └── resources/
│           ├── application.yml / application-dev.yml
│           └── db/schema.sql      # 建表 + mood 种子数据
└── frontend/                      # Next.js
    ├── app/                       # login/register/home/record/ideal/capsule/me
    ├── components/                # record/ ideal/ ui/ shared/
    ├── lib/                       # api.ts、auth.ts、stream.ts
    ├── store/  types/  .env.local
```

---

## 5. 核心功能实现思路

> 后端统一分层 `Controller → Service → Mapper`；统一响应 `Result<T>{code,message,data}`（`code=0` 成功）；所有 `🔒` 接口走 JWT 过滤器并做 `user_id` 归属校验（越权返回 404/403）。

### 5.1 用户系统

**功能点**：邮箱验证码注册、登录、JWT 刷新、个人主页、基础设置。

**后端实现**
- `AuthController`：`send-code` / `register` / `login` / `refresh`；`UserController`：`me`（GET/PUT）、`overview`。
- `EmailCodeService`：生成 6 位码写入 `email_code`（10 分钟过期）；发送前查最近一条做 **60s 频控**。
- `JwtService`（jjwt 0.12）：签发 `access_token`（2h）+ `refresh_token`（14d）。
- `SecurityConfig`：无状态、关 CSRF、`BCryptPasswordEncoder`；`JwtAuthenticationFilter extends OncePerRequestFilter` 解析 `Bearer` 注入 `UserContext`（ThreadLocal）。
- **白名单**：`/auth/send-code`、`/auth/register`、`/auth/login`、`/auth/refresh`、`/doc.html`、`/v3/api-docs/**`。
- **邮件抽象**：`MailService` 接口，MVP 实现用 `log/console` 打印验证码（无 SMTP 也能跑通），生产再切 SMTP。

**前端实现**
- `/login` `/register` 页；`lib/auth.ts` 存取 token；`lib/api.ts` 统一注入 `Bearer`、401 自动用 refresh 换新、失败跳登录。
- Zustand 存当前用户；TanStack Query 管 `useLogin/useRegister/useMe`。

**关键边界**：验证码错误/过期明确提示；邮箱已注册引导登录；token 失效跳登录。

**完成标准**：注册（验证码）→ 登录 → 刷新 → `GET /user/me` 全流程走通；未登录访问受保护接口返回 401。

---

### 5.2 记录卡

**功能点**：CRUD、心情、AI 扩写、AI 风格推荐、图片上传（1-3 张）、时间线列表。

**后端实现**
- `RecordController`：`GET/POST/PUT/DELETE /records`、`GET /records/{id}`（分页）；`FileController`：`POST /files/upload`。
- `RecordService`：创建用**事务**同时写 `record_card` + `card_image`；删除为逻辑删除。
- `FileService`：校验类型（jpg/png/webp）、单张 ≤5MB、最多 3 张；本地存 `uploads/` 随机文件名，返回访问 url。
- **存储抽象**：`StorageService` 接口（本地 / OSS 两实现），生产切换零改动业务代码。
- 列表走 MyBatis-Plus `Page` 分页，按 `user_id` 过滤。

**前端实现**
- `/record` 时间线（`useInfiniteQuery` 分页）；`/record/new`、`/record/[id]/edit`。
- 组件：正文编辑器、心情选择器、AI 面板、风格选择、图片上传（预览/删除/排序）。
- **AI 扩写交互**：`fetch-event-source` 调 `/ai/expand` 流式逐字渲染到预览框 → 用户「采纳」覆盖正文 / 「丢弃」保留原文（不落中间态）。

**关键边界**：空内容前后端双重校验；AI 失败降级保留原文不阻塞保存；图片超限即时提示。

**完成标准**：建卡（心情 + 1-3 图）→ 列表 → 编辑 → 删除走通；空提交、图片超限被拦截。

---

### 5.3 理想卡

**功能点**：创建理想、AI 拆解任务、每日打卡、打卡日历、可见性设置。

**后端实现**
- `IdealController`：`/ideals` CRUD、`/ideals/{id}/tasks`、`/ideals/{id}/checkin`、`/ideals/{id}/calendar`。
- `IdealService`：创建时写 `ideal`；AI 拆解（场景 `IDEAL_SPLIT`）返回任务列表写 `ideal_task`，**失败降级为手动加任务**。
- `CheckInService`：打卡幂等——`check_in` 联合唯一键 `(ideal_id, check_date)`，用「唯一键兜底 + 先查后插」保证同一天重复点击只更新不新增。
- `CalendarService`：按 `check_in.check_date` 按月聚合打卡分布。

**前端实现**
- `/ideal` 列表（进行中/已完成分组）；`/ideal/[id]` 详情：任务列表（勾选完成）、今日打卡按钮、打卡日历。

**关键边界**：AI 拆解失败理想仍可创建；重复打卡幂等；`PRIVATE/PUBLIC` 为二期社交预留字段。

**完成标准**：建理想 → AI 拆解（或手动）→ 打卡 → 日历显示；同日重复打卡不产生重复记录。

---

### 5.4 时间胶囊

**功能点**：写信给未来自己、设定开启日期、到期提醒。

**后端实现**
- `CapsuleController`：`GET/POST/DELETE /capsules`、`GET /capsules/{id}`。
- `CapsuleService`：创建校验 `open_date ≥ 明天`；详情**强制校验到期**——未到期返回锁定态（不含 `content`），到期后可见并置 `is_opened=1`。
- `CapsuleRemindJob`：`@Scheduled` 每日扫描 `open_date <= today AND remind_sent=0` → 发站内提醒（个人主页提示）+ 邮件（可选），置 `remind_sent=1`；主类 `@EnableScheduling`。

**前端实现**
- `/capsule`：写信（标题/内容/开启日期，日期选择器限制 ≥ 明天）、列表（锁定/可开启状态）、开启详情。

**关键边界**：未到期内容不可见（后端强制校验）；到期提醒兜底为站内（邮件可选）。

**完成标准**：写信 → 未到期锁定 → 到期开启 → 内容可见；定时任务能扫到到期胶囊并触发提醒。

---

### 5.5 AI 能力（横切）

**依赖与接入**：`spring-ai-bom` + `spring-ai-starter-model-deepseek`；`application.yml` 配 `spring.ai.deepseek.api-key / base-url=https://api.deepseek.com / chat.options.model=deepseek-chat`，注入 `ChatClient`。

**统一封装**：`AiService` 两个方法，四场景复用——
- `chatStream(prompt)`：返回 `Flux<String>`，桥接 `SseEmitter`，规范化为自有协议 `{"type":"delta","content":"..."}` / `{"type":"done"}` / `{"type":"error",...}`；
- `chatJson(prompt, type)`：`BeanOutputConverter` 结构化输出为 DTO。

**四场景**：

| 场景 | 输出 | 关键点 |
|---|---|---|
| 扩写 EXPAND | 流式 | 温度 0.7 / max_tokens 1024 |
| 风格推荐 STYLE | 非流式 JSON `{style_key, reason}` | 温度 0.2 / 失败回退默认风格 |
| 理想拆解 IDEAL_SPLIT | 非流式 JSON `[{content}]` | 失败降级手动加任务 |
| 任务拆解 | 复用场景三 | 出 2~3 个今日小动作 |

**限流与成本**：`ai_usage_log` 计次，每场景每日 20 次，超限返回 429；靠每日上限 + `max_tokens` 控成本（MVP 不做结果缓存）。

**降级铁律**：所有 AI 调用包在 try/catch，失败即降级为手动模式，**核心功能不依赖 AI**。DeepSeek 的 function calling 不稳定，统一走「提示词要求输出 JSON + 后端容错解析」，不用 tool calling。

**完成标准**：扩写流式、风格推荐、理想拆解可用；超限返回 429；DeepSeek 不可用时记录卡/理想卡/胶囊功能不受影响。

---

## 6. 实行路线

### 6.1 依赖关系

```
数据库(schema) → 用户系统(所有🔒接口前置)
                  ↓
记录卡(最小完整闭环：前后端 + 图片 + 分页)
                  ↓
AI 能力(扩写/风格，接入记录卡)
                  ↓
理想卡(复用 AI 封装) → 时间胶囊(独立 + 定时任务) → 个人主页(聚合视图，最后做)
                  ↓
UI 打磨 / 响应式 / 测试 / 部署预案
```

> 顺序原则：**先库后接口 → 先后端后前端 → 先骨架后功能 → AI 后置 → 每天可运行**。认证是所有受保护接口的前置；记录卡是最简单的完整闭环，用它打通前后端 + 图片 + AI 三件套；理想卡直接复用 AI 封装；胶囊最独立；主页是聚合视图必须等各模块就绪。

### 6.2 阶段与里程碑

| 阶段 | 天 | 内容 | 里程碑 / 完成标准 |
|---|---|---|---|
| **P0 地基** | D1 | monorepo 骨架 + `schema.sql` + 两工程可启动 | 后端/前端各自启动，库建好，`Result<T>`/异常/JWT/CORS 就位 |
| **P1 用户** | D2 | 注册/登录/JWT/验证码 | 登录注册全流程走通 |
| **P2 记录卡** | D3-4 | CRUD + 心情 + 图片上传 | 卡片可增删改查 |
| **P3 AI** | D5 | Spring AI 接入 + 扩写 + 风格推荐 | AI 扩写/风格可用 |
| **P4 理想卡** | D6-8 | CRUD + 打卡 + 日历 + AI 拆解 | 理想可打卡 |
| **P5 胶囊** | D9 | 写信/开启 + 定时提醒 | 胶囊可写可开启 |
| **P6 主页** | D10 | 个人主页聚合视图 | 主页展示完整 |
| **P7 收尾** | D11-14 | 漫画风打磨 / 响应式 / 测试 / 部署预案 | MVP 可交付 |

### 6.3 每日检查清单

① 两工程可启动　② 当日功能跑通　③ 无 P0 阻塞　④ 记录明日任务。

---

## 7. 工程规范

- **分支**：单人 `main` 直提，每完成一个功能点提交一次（`feat: 记录卡 CRUD`）。
- **联调**：后端用 Knife4j 自测；前端 TanStack Query 对接；问题用后端日志 + 浏览器 Network 定位。
- **越权**：所有资源按 `user_id` 归属校验，查/改/删他人数据返回 404/403。
- **验收清单**：注册登录、记录卡（含心情/图片）、AI 扩写、AI 风格、理想卡（拆解/打卡/日历）、时间胶囊（写信/开启/提醒）、个人主页、淡蓝漫画风与温暖文案，全部走通。

---

## 8. 裁剪与风险

- **严格 MVP**：新想法一律进 backlog，二期再评估，防止范围膨胀。
- **AI 最小可用**：失败降级为手动模式，AI 永远不阻塞核心功能。
- **部署后置**：本地先跑通，部署方案（Docker 预案）到 D14 再定。
- 详细风险清单见方案设计文档 §13。

---

> 本文档与方案设计文档配套使用：设计文档是「蓝图」（表结构/接口/Prompt 明细），本文档是「路线」（实现思路 + 顺序 + 完成标准）。开发中如遇变更，同步更新两份文档并记录变更点。
