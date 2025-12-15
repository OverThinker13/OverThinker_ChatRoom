# 🚀 Java Chatroom 实时聊天室系统

一个基于 **Spring Boot** 和 **WebSocket** 技术实现的轻量级实时聊天室项目。

## ✨ 项目概述

这是一个采用 **前后端分离** 架构的 Web 聊天应用。它专注于提供一个稳定、实时的消息通信平台，支持用户认证、好友管理、以及核心的一对一私聊功能。

| 特性 | 描述 |
| :--- | :--- |
| **实时通信** | 基于 WebSocket 实现，消息秒级推送。 |
| **核心功能** | 用户注册登录、好友列表、私聊会话、消息历史记录。 |
| **后端架构** | Spring Boot 配合 MyBatis，快速构建 RESTful API。 |
| **前端技术** | 传统 HTML/CSS/JavaScript + jQuery，轻量易维护。 |

## 🛠️ 技术栈一览

| 类别 | 技术名称 | 角色与描述 |
| :--- | :--- | :--- |
| **后端框架** | **Spring Boot 2.7.6** | 快速开发 Web 应用，简化配置。 |
| **实时通信** | **Spring WebSocket** | 实现客户端与服务器的双向持久连接。 |
| **数据访问** | **MyBatis** | 灵活的持久层框架，SQL 与代码分离。 |
| **数据库** | **MySQL** | 关系型数据库，存储用户信息和聊天记录。 |
| **构建工具** | **Maven** | 项目依赖管理与构建。 |
| **前端基础** | **HTML/CSS/JS** | 负责页面结构、样式和交互逻辑。 |
| **交互库** | **jQuery/Ajax** | 简化 DOM 操作和发起异步 HTTP 请求。 |

-----

## 🏗️ 系统架构与数据流

本项目最核心的部分是 **Spring Boot + WebSocket** 实现的实时通信机制。

### 架构示意图

本项目采用经典的三层架构（Controller/Service/Dao）配合 WebSocket 的通信模型。

### 核心通信流程：WebSocket

1.  用户通过 HTTP `/login` 登录成功后，获取会话信息。
2.  用户发起 WebSocket 连接请求到 `/WebSocketMessage`。
3.  服务器将用户 ID 和对应的 WebSocket 会话 (Session) 绑定存储。
4.  用户 A 发送消息到服务器（通过 WebSocket）。
5.  服务器根据消息中的目标用户 ID 查找对应的 WebSocket Session。
6.  服务器通过目标 Session 将消息实时推送给用户 B。

-----

## 📁 项目结构概览

清晰的模块化结构，便于开发者理解和维护：

```
java_chatroom/
├── src/
│   ├── main/
│   │   ├── java/com/example/java_chatroom/
│   │   │   ├── api/           # 🌐 Controller 层
│   │   │   ├── component/     # 🔌 组件类
│   │   │   ├── config/        # ⚙️ 配置类
│   │   │   └── model/         # 📦 数据模型
│   │   ├── resources/
│   │   │   ├── mapper/        # 📜 MyBatis XML 映射文件
│   │   │   └── static/        # 🖥️ 前端静态资源 (HTML/CSS/JS)
│   │   └── db.sql             # 💾 数据库初始化脚本
├── pom.xml                    # Maven 依赖配置
└── README.md                  # 项目说明文档 (当前文件)
```

## 🗃️ 数据库设计 (MySQL)

系统采用 5 个核心数据表来实现用户关系和消息存储。

### 核心表结构关系图

### 关键数据表 (SQL 摘录)

| 表名 | 描述 | 关键字段 | 关系说明 |
| :--- | :--- | :--- | :--- |
| **user** | 用户基本信息 | `userId`, `username`, `password` | 存储登录凭证 |
| **friend** | 用户好友关系 | `userId`, `friendId` | 记录谁是谁的好友 |
| **message\_session** | 会话信息 | `sessionId`, `lastTime` | 私聊会话的主键 |
| **message\_session\_user** | 会话用户关联 | `sessionId`, `userId` | **多对多**：一个会话关联多个用户 (用于扩展群聊) |
| **message** | 消息内容 | `messageId`, `fromId`, `sessionId`, `content`, `postTime` | 存储具体的聊天记录 |

-----

## 🎯 核心功能模块与 API

项目主要通过 RESTful API 和 WebSocket 端点实现功能。

### 认证与用户信息

| 模块 | 接口/端点 | 方式 | 描述 |
| :--- | :--- | :--- | :--- |
| 用户注册 | `/register` | POST | 创建新用户 |
| 用户登录 | `/login` | POST | 校验并建立用户会话 |
| 获取信息 | `/userInfo` | GET | 获取当前登录用户的基本信息 |

### 好友与会话管理

| 模块 | 接口/端点 | 方式 | 描述 |
| :--- | :--- | :--- | :--- |
| 获取列表 | `/friendList` | GET | 查看当前用户的所有好友 |
| 获取列表 | `/sessionList` | GET | 查看所有进行中的私聊会话 |
| 创建会话 | `/session` | POST | 与指定好友创建一个新的会话 |

### 消息服务

| 模块 | 接口/端点 | 方式 | 描述 |
| :--- | :--- | :--- | :--- |
| 历史消息 | `/message` | GET | 根据 `sessionId` 分页获取历史聊天记录 |
| 实时推送 | `/WebSocketMessage` | WebSocket | **核心**：建立实时双向通信通道 |

-----

## ⚙️ 环境与运行指南

### 🔧 运行环境要求

  * **Java Development Kit (JDK):** 1.8 或更高版本
  * **MySQL Server:** 5.7 或更高版本
  * **Maven:** 3.6 或更高版本

### 📥 步骤

1.  **克隆项目:**

    ```bash
    git clone <repository-url>
    ```

2.  **初始化数据库:**

      * 确保 MySQL 服务运行，并创建一个名为 `java_chatroom` 的数据库。
      * 执行 SQL 脚本：
        ```bash
        mysql -u root -p < src/main/java/db.sql
        ```

3.  **配置数据库连接:**

      * 打开 `src/main/resources/application.yml` 文件。
      * 修改 `username` 和 `password` 为您的 MySQL 账户信息。

    <!-- end list -->

    ```yaml
    spring:
      datasource:
        # ... url: jdbc:mysql://127.0.0.1:3306/java_chatroom? ...
        username: root
        password: your_password # <-- 替换您的密码
    ```

4.  **运行项目:**

    ```bash
    mvn clean install # 编译和打包
    mvn spring-boot:run # 启动 Spring Boot 应用
    ```

5.  **访问应用:**
    打开浏览器访问：`http://localhost:8080/login.html`

-----

## 📸 界面展示 (Screenshots)

### 登录与注册

| 登录页面 | 注册页面 |
| :---: | :---: |
| <img width="2559" height="1337" alt="image" src="https://github.com/user-attachments/assets/2c1c7eea-efc0-4b41-ad46-ba4c44f72036" /> | <img width="2559" height="1337" alt="image" src="https://github.com/user-attachments/assets/efe47403-357a-4eea-b24a-7b808ab95c72" /> |

### 聊天主界面

| 好友/会话列表 | 聊天区域 |
| :---: | :---: |
| <img width="2549" height="1403" alt="image" src="https://github.com/user-attachments/assets/0f7f5010-3e12-41a6-8539-82c586649444" /> | <img width="2559" height="1342" alt="image" src="https://github.com/user-attachments/assets/d26b27ab-c09c-4dbc-bc0b-ab375b919d08" /> |

-----

## 🌟 功能特性总结

1.  **用户认证体系:**
      * 支持用户注册新账号。
      * 实现基于会话的登录校验。
2.  **高效实时通信:**
      * 利用 WebSocket 实现消息的毫秒级推送。
      * 支持一对一私密聊天。
3.  **完善的消息与会话管理:**
      * 自动管理私聊会话的创建与激活。
      * 持久化存储消息记录，支持查看历史消息。
4.  **基础的好友关系:**
      * 展示当前用户的好友列表。

-----

## 💡 Future Enhancements (未来展望)

本项目可进一步扩展以实现更丰富的功能：

  * **好友请求功能:** 实现用户搜索、发送/接受好友请求的完整流程 (当前需手动修改 DB)。
  * **个性化展示:**用户添加头像，个性签名等。
  * **群聊支持:** 扩展会话模型，支持多人聊天室和群组管理。
  * **消息类型扩展:** 支持发送图片、文件和表情包。
  * **用户状态管理:** 实时显示用户的在线/离线状态和最后活跃时间。
  * **UI/UX 优化:** 引入更现代的前端框架或库，实现响应式设计。

## 🤝 贡献与许可

我们欢迎任何形式的贡献，包括但不限于提交 Bug 报告 (Issue) 和功能改进代码 (Pull Request)。

  * **许可证:** 本项目仅供学习和交流使用。

-----
