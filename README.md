# MCP Demo

基于 Spring AI 的 **Model Context Protocol (MCP)** 完整演示项目，包含 Server 和 Client 两个子项目，展示如何通过 SSE 传输模式暴露和调用 MCP 的三大核心能力：**Tool**、**Resource**、**Prompt**。

## 技术栈

| 组件 | 版本 |
|------|------|
| Java | 25 (通过 Gradle toolchain 指定) |
| Spring Boot | 4.0.6 |
| Spring AI | 2.0.0-M8 |
| Gradle | 9.x |
| Lombok | 由 Spring Boot BOM 管理 |

## 项目结构

```
mcp-demo/                          # MCP Server - 暴露工具、资源、Prompt
├── src/main/java/.../mcpdemo/
│   ├── McpDemoApplication.java    # 启动类
│   └── CalculatorTools.java       # MCP 能力定义（Tool / Resource / Prompt）
├── src/main/resources/
│   └── application.yaml           # Server 配置（端口 8080）
├── build.gradle
│
└── client-demo/                   # MCP Client - 连接 Server 并调用能力
    ├── src/main/java/.../clientdemo/
    │   ├── ClientDemoApplication.java   # 启动类
    │   ├── McpDemoService.java          # 封装 MCP Client 调用逻辑
    │   └── McpDemoController.java       # 暴露 HTTP 接口供验证
    ├── src/main/resources/
    │   └── application.yaml             # Client 配置（端口 8081，SSE 连接 Server）
    └── build.gradle
```

## MCP Server（mcp-demo）

Server 通过注解暴露 MCP 三大能力：

| 注解 | 名称 | 说明 |
|------|------|------|
| `@McpTool` | `add` | 两数相加 |
| `@McpResource` | `config://{key}` | 读取内存配置（name / age） |
| `@McpPrompt` | `greeting` | 生成个性化欢迎语 |

**关键依赖**：`spring-ai-starter-mcp-server-webmvc`（SSE 传输模式，基于 Spring MVC）

## MCP Client（client-demo）

Client 通过 SSE 连接 Server，使用 `McpSyncClient` 调用 MCP 能力，并对外暴露 HTTP 接口方便验证。

**关键依赖**：`spring-ai-starter-mcp-client` + `spring-boot-starter-web`

## 快速开始

### 1. 启动 MCP Server

```bash
cd mcp-demo
./gradlew bootRun
```

Server 启动后监听 `http://localhost:8080`，SSE 端点为 `/sse`。

### 2. 启动 MCP Client

```bash
cd mcp-demo/client-demo
./gradlew bootRun
```

Client 启动后监听 `http://localhost:8081`，自动通过 SSE 连接 Server。

> **注意**：必须先启动 Server，否则 Client 初始化时会报超时错误。

### 3. 验证接口

```bash
# 列出所有 MCP 工具
curl http://localhost:8081/demo/tools

# 调用 add 工具
curl "http://localhost:8081/demo/add?a=3&b=5"
# 返回: 8

# 读取配置资源
curl "http://localhost:8081/demo/config?key=name"
# 返回: Bob

# 获取 Greeting Prompt
curl "http://localhost:8081/demo/greeting?name=Alice"
# 返回: Hello, Alice! Welcome to the MCP Demo. How can I help you today?
```

## 对接 AI 客户端

### Claude Desktop（STDIO 模式不适用，需 SSE）

当前 Server 使用 SSE 模式，Claude Desktop 原生支持 STDIO。如需对接 Claude Desktop，可将 Server 依赖改回 `spring-ai-starter-mcp-server` 并配置 STDIO 传输。

### 通义千问客户端

千问客户端支持 SSE 类型 MCP Server，直接填入地址：

```
http://localhost:8080/sse
```

### 自建 MCP Client（本项目 client-demo）

通过 `spring-ai-starter-mcp-client` 编程式接入，详见 `client-demo` 子项目代码。

## Client API 一览

| 接口 | 方法 | 说明 |
|------|------|------|
| `/demo/tools` | GET | 列出 Server 暴露的所有 Tool |
| `/demo/prompts` | GET | 列出 Server 暴露的所有 Prompt |
| `/demo/add?a=&b=` | GET | 调用 add Tool |
| `/demo/config?key=` | GET | 读取 Resource |
| `/demo/greeting?name=` | GET | 获取 Prompt |

## 常见问题

**Q: Client 启动报 `Client failed to initialize by explicit API call`**

MCP Server 未启动或不可达。确保先启动 Server（端口 8080），再启动 Client。

**Q: 直接 `java -jar` 报 `UnsupportedClassVersionError`**

项目使用 Java 25 编译，请用 `./gradlew bootRun` 启动，Gradle 会自动使用 toolchain 指定的 Java 版本。

**Q: curl 返回空 / 502**

系统配置了 HTTP 代理（如 `ALL_PROXY`），使用 `--noproxy localhost` 绕过代理。
