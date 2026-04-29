# Java ChatRoom 实时聊天室

基于 **Spring Boot 3.2 + WebSocket + JWT + Redis** 的即时通讯系统，部署在腾讯云服务器，公网可访问。

---

## 技术栈

| 类别 | 技术 | 用途 |
|------|------|------|
| 核心框架 | Spring Boot 3.2 | 快速构建 Web 应用 |
| 实时通信 | Spring WebSocket | 消息推送 + 在线状态通知 |
| ORM | MyBatis | 数据持久化 |
| 数据库 | MySQL 8.0+ | 用户、好友、消息、会话存储 |
| 缓存/认证 | Redis 7.x | Token 管理（服务端主动失效）+ 在线状态 |
| 认证 | JWT (JJWT 0.12.5) | 无状态认证 |
| 安全 | HttpOnly Cookie | 防 XSS 窃取 Token |
| 加密 | BCrypt (Spring Security Crypto) | 密码加密 |
| Redis 连接池 | Commons Pool2 + Lettuce | 性能优化，避免反复建连 |
| 构建 | Maven | 依赖管理 |
| 前端 | HTML/CSS/JS + jQuery + WebSocket API | 赛博朋克主题界面 |

---

## 架构图

```
浏览器 (HttpOnly Cookie 自动携带 JWT)
    │
    ├─ HTTP API ──→ JwtInterceptor ──→ Redis 校验 ──→ Controller ──→ Service ──→ MySQL
    │                    │                  │
    │             解析 Cookie JWT     查 token:user:{id} 是否存在
    │
    └─ WebSocket ──→ JwtWebSocketInterceptor ──→ WebSocketAPI
                           │                           │
                    握手时校验 JWT+Redis          消息转发 + 在线状态
```

---

## 项目结构

```
java_chatroom/
├── pom.xml
│
├── src/main/java/com/example/java_chatroom/
│   ├── config/                          # 配置层
│   │   ├── WebSocketConfig.java         # WebSocket 注册
│   │   ├── WebMvcConfig.java            # 拦截器注册
│   │   ├── JwtInterceptor.java          # HTTP 请求 JWT 拦截
│   │   ├── JwtWebSocketInterceptor.java # WebSocket 握手 JWT 校验
│   │   └── GlobalExceptionHandler.java  # 全局异常处理
│   │
│   ├── controller/                      # API 层
│   │   ├── UserController.java          # 登录/注册/搜索/退出
│   │   ├── FriendController.java        # 好友列表/请求处理
│   │   ├── MessageSessionController.java# 会话列表/创建
│   │   ├── MessageController.java       # 历史消息
│   │   └── WebSocketAPI.java            # WebSocket 消息处理核心
│   │
│   ├── service/                         # 业务逻辑
│   │   ├── impl/
│   │   │   ├── UserServiceImpl.java
│   │   │   ├── FriendServiceImpl.java
│   │   │   ├── MessageSessionServiceImpl.java
│   │   │   └── MessageServiceImpl.java
│   │   ├── RedisTokenService.java       # Redis Token 管理
│   │   └── ...
│   │
│   ├── mapper/                          # MyBatis Mapper
│   ├── entity/                          # 实体类
│   │   ├── ApiResult.java               # 统一响应格式
│   │   ├── ResultCode.java              # 响应码枚举
│   │   └── ...
│   ├── exception/
│   │   └── BusinessException.java       # 业务异常
│   ├── component/
│   │   └── OnlineUserManger.java        # 在线用户管理（内存+Redis）
│   └── util/
│       └── JwtUtil.java                 # JWT 生成/解析
│
├── src/main/resources/
│   ├── application.yml                  # 本地配置
│   ├── application-server.yml           # 服务器配置
│   ├── mapper/                          # MyBatis XML 映射
│   └── static/                          # 前端资源
│       ├── login.html / register.html / client.html
│       ├── css/    (赛博朋克主题)
│       ├── js/client.js
│       └── img/
│
└── 项目完整文档.md                       # 项目详细说明
```

---

## API 接口一览

所有接口返回统一格式 `{ code, message, data }`。

### 用户模块

| 方法 | 路径 | 说明 | 登录 |
|------|------|------|------|
| POST | `/login` | 登录，返回 User + 设置 HttpOnly Cookie | ❌ |
| POST | `/register` | 注册新用户 | ❌ |
| GET | `/userInfo` | 获取当前用户信息 | ✅ |
| GET | `/searchUser` | 搜索用户（排除自己和好友） | ✅ |
| POST | `/addFriend` | 发送好友请求 | ✅ |
| GET | `/logout` | 退出登录（删 Redis + 清 Cookie） | ✅ |

### 好友模块

| 方法 | 路径 | 说明 | 登录 |
|------|------|------|------|
| GET | `/friendList` | 好友列表（含在线状态） | ✅ |
| POST | `/handleRequest` | 处理好友请求（同意/拒绝） | ✅ |
| GET | `/getFriendRequests` | 收到的好友请求列表 | ✅ |

### 会话/消息模块

| 方法 | 路径 | 说明 | 登录 |
|------|------|------|------|
| GET | `/sessionList` | 会话列表 | ✅ |
| POST | `/session` | 创建/获取私聊会话 | ✅ |
| GET | `/message` | 历史消息（最近 100 条） | ✅ |

### WebSocket

| 路径 | 说明 |
|------|------|
| `/WebSocketMessage` | 实时消息通道，通过 Cookie 中的 JWT 认证 |

---

## 数据库设计（6 张表）

| 表 | 说明 | 关键字段 |
|----|------|---------|
| user | 用户 | userId, username, password(BCrypt) |
| friend | 好友关系 | userId, friendId, createTime |
| friend_request | 好友请求 | fromUserId, toUserId, status(0待处理/1同意/2拒绝) |
| message_session | 会话 | sessionId, lastTime |
| message_session_user | 会话成员 | sessionId, userId |
| message | 消息 | messageId, fromId, sessionId, content, postTime |

### Redis 数据结构

| Key | 类型 | 用途 |
|-----|------|------|
| `token:user:{userId}` | String | 存储用户 JWT（24h 过期） |
| `online_users` | Set | 在线用户 ID 集合 |

---

## 核心功能

### 1. 认证系统（JWT + Redis + HttpOnly Cookie）

```
登录 → 生成 JWT → 存 Redis → 设置 HttpOnly Cookie（JS 无法读取）
请求 → Cookie 自动携带 → 拦截器解析 JWT → 查 Redis 校验 → 通过
退出 → 删 Redis 记录 → 清 Cookie → token 立即失效
踢人 → redis-cli DEL token:user:3 → 用户下次请求被拦截
```

JWT 和 Redis **双重校验**，解决了纯 JWT 方案无法服务端撤销 token 的痛点。

### 2. 在线状态（Redis Set）

- WebSocket 连接时，用户 ID 加入 Redis `online_users` 集合
- 断开时移除，并向好友推送上线/下线通知
- 好友列表接口通过 `SISMEMBER` 查询在线状态
- 前端实时显示绿色发光圆点 / 灰色圆点

### 3. WebSocket 实时消息

- 握手阶段通过 JwtWebSocketInterceptor 从 Cookie 解析 JWT 认证
- 消息接收后查询会话成员，遍历推送
- 消息同时持久化到 MySQL，用于历史记录

### 4. 全局异常处理

- `BusinessException` 业务异常 → 返回 400
- `DuplicateKeyException` → 返回 400（用户名重复）
- `Exception` → 返回 500
- 统一 `ApiResult<T>` + `ResultCode` 枚举

---

## 快速启动

### 环境要求

- JDK 17+
- MySQL 8.0+
- Redis 7.x
- Maven 3.6+

### 本地运行

```bash
# 1. 创建数据库
mysql -u root -p -e "CREATE DATABASE java_chatroom DEFAULT CHARSET utf8mb4;"

# 2. 修改配置 application.yml（数据库密码、Redis 端口）

# 3. 启动 Redis（确保 6379 或你配置的端口已启动）

# 4. 打包运行
mvn package -DskipTests
mvn spring-boot:run

# 5. 访问
http://localhost:8080/login.html
```

### 服务器部署

```bash
# 1. 安装 Redis
yum install -y redis && systemctl start redis

# 2. 上传 jar + application-server.yml 到服务器

# 3. 启动
nohup java -jar java_chatroom-0.0.1-SNAPSHOT.jar \
    --spring.config.additional-location=./application-server.yml \
    > app.log 2>&1 &

# 4. 查看日志
tail -f app.log
```

### 运维命令

```bash
# 停止项目
kill -9 $(lsof -t -i:8080)

# 查看 Redis 数据
redis-cli keys '*'
redis-cli SMEMBERS online_users

# 踢人下线
redis-cli DEL token:user:3
redis-cli SREM online_users 3

# 清空 Redis
redis-cli FLUSHALL
```

---

## 遇到的问题与解决方案

| 问题 | 解决方案 |
|------|---------|
| JWT 无法服务端撤销 | 引入 Redis 双校验，退出/踢人直接删 Redis |
| Token 存 localStorage 不安全 | 改为 HttpOnly Cookie，JS 无法读取 |
| Redis 操作慢 | 配置 Lettuce 连接池（min-idle=2, max-active=8） |
| 前端加载慢 | CDN 换用 cdnjs + 关闭 HTTP/2 |
| 数据库 password 字段太短 | 改为 VARCHAR(128) |
| 搜索结果包含自己和好友 | 后端过滤 excludeIds 列表 |
| WebSocket 无认证 | 加 JwtWebSocketInterceptor 握手时校验 |

详见 [`项目完整文档.md`](./项目完整文档.md) 第 14 章。

---

## 功能特性

- [x] 注册/登录（BCrypt 加密）
- [x] JWT 认证 + Redis 双校验
- [x] HttpOnly Cookie 防 XSS
- [x] 服务端主动踢人/退出立即失效
- [x] 实时消息收发（WebSocket）
- [x] 在线状态显示（绿点/灰点）
- [x] 好友搜索（排除自己+已加好友）
- [x] 好友请求处理
- [x] 会话管理 + 历史消息（最近 100 条）
- [x] 全局异常处理 + 统一响应格式
- [x] 赛博朋克主题界面
- [x] 腾讯云服务器部署
- [x] Redis 连接池优化

---

## 许可证

本项目仅供学习和交流使用。
