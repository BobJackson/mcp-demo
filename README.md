# MCP Demo

基于 Spring AI 的 **Model Context Protocol (MCP)** 完整演示项目，包含 Server 和 Client 两个子项目，展示如何通过 **Streamable HTTP** 传输模式暴露和调用 MCP 的核心能力：**Tool**、**Resource**、**Prompt**，并集成 **Token 鉴权** 机制实现客户端身份识别与权限管理。

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
mcp-demo/                                # MCP Server
├── src/main/java/.../mcpdemo/
│   ├── McpDemoApplication.java          # 启动类
│   ├── CalculatorTools.java             # MCP 能力定义（Tool / Resource / Prompt）
│   ├── McpAuthFilter.java               # Token 鉴权 Filter
│   └── FilterConfig.java                # Filter 注册（异步支持 + 路径拦截）
├── src/main/resources/
│   └── application.yaml                 # Server 配置（端口 8080，Streamable HTTP 模式）
├── doc/
│   └── spring-ai-mcp-annotations.md     # MCP 四大注解详解
├── build.gradle
│
└── client-demo/                         # MCP Client
    ├── src/main/java/.../clientdemo/
    │   ├── ClientDemoApplication.java         # 启动类
    │   ├── McpDemoService.java                # 封装 MCP Client 调用逻辑
    │   ├── McpDemoController.java             # 暴露 HTTP 接口供验证
    │   ├── McpHttpClientConfig.java           # Transport 定制器注册
    │   └── McpTokenHeaderCustomizer.java      # 请求级 Header 注入器
    ├── src/main/resources/
    │   └── application.yaml                   # Client 配置（端口 8081，Streamable HTTP 连接）
    └── build.gradle
```

## MCP Server（mcp-demo）

### 核心能力

Server 通过注解暴露 MCP 能力：

| 注解 | 名称 | 说明 |
|------|------|------|
| `@McpTool` | `add` | 两数相加，日志中记录客户端身份 |
| `@McpTool` | `whoami` | 返回当前调用者的客户端身份 |
| `@McpResource` | `config://{key}` | 读取内存配置（name / age） |
| `@McpPrompt` | `greeting` | 生成个性化欢迎语 |

**关键依赖**：`spring-ai-starter-mcp-server-webmvc`（Streamable HTTP 传输模式）

### Token 鉴权

通过 Servlet Filter 拦截 MCP 端点，校验 `X-MCP-Token` Header 或 `key` query 参数：

- **合法 Token**：`token-alice`、`token-bob`、`demo-key`
- 校验通过后，将客户端身份写入 `request attribute`，Tool 方法通过 `RequestContextHolder` 读取
- 校验失败返回 `401 Unauthorized`

### 传输模式配置

`application.yaml` 中通过 `protocol: STREAMABLE` 启用 Streamable HTTP 模式（端点 `/mcp`）：

```yaml
spring:
  ai:
    mcp:
      server:
        protocol: STREAMABLE
```

> 若需 SSE 模式，去掉该配置即可（默认 SSE），端点变为 `/sse` + `/mcp/message`。

## MCP Client（client-demo）

Client 通过 Streamable HTTP 连接 Server，使用 `McpSyncClient` 调用 MCP 能力，并对外暴露 HTTP 接口方便验证。

### Token 注入机制

Client 通过 `McpClientCustomizer<HttpClientStreamableHttpTransport.Builder>` 将 `McpTokenHeaderCustomizer` 注册到 Transport Builder，所有 HTTP 请求（建连 + 消息）自动携带 `X-MCP-Token` Header。

**关键依赖**：`spring-ai-starter-mcp-client` + `spring-boot-starter-web`

## 快速开始

### 1. 启动 MCP Server

```bash
cd mcp-demo
./gradlew bootRun
```

Server 启动后监听 `http://localhost:8080`，Streamable HTTP 端点为 `/mcp`。

### 2. 启动 MCP Client

```bash
cd mcp-demo/client-demo
./gradlew bootRun
```

Client 启动后监听 `http://localhost:8081`，自动通过 Streamable HTTP 连接 Server。

> **注意**：必须先启动 Server，否则 Client 初始化时会报超时错误。

### 3. 验证接口

```bash
# 列出所有 MCP 工具
curl http://localhost:8081/demo/tools

# 调用 add 工具
curl "http://localhost:8081/demo/add?a=3&b=5"
# 返回: 8

# 查看当前客户端身份（鉴权验证）
curl http://localhost:8081/demo/whoami
# 返回: You are: Alice

# 读取配置资源
curl "http://localhost:8081/demo/config?key=name"
# 返回: Bob

# 获取 Greeting Prompt
curl "http://localhost:8081/demo/greeting?name=Alice"
# 返回: Hello, Alice! Welcome to the MCP Demo. How can I help you today?
```

## 对接 AI 客户端

### 通义千问客户端

千问客户端支持 SSE / Streamable HTTP 类型 MCP Server，填入地址：

```
http://localhost:8080/mcp
```

> 若 Server 切换为 SSE 模式（去掉 `protocol: STREAMABLE`），地址为 `http://localhost:8080/sse`。

### Claude Desktop

Claude Desktop 原生支持 STDIO 模式。如需对接，可将 Server 依赖改为 `spring-ai-starter-mcp-server` 并去掉 `protocol` 配置以启用 STDIO 传输。

### 自建 MCP Client（本项目 client-demo）

通过 `spring-ai-starter-mcp-client` 编程式接入，详见 `client-demo` 子项目代码。

## Client API 一览

| 接口 | 方法 | 说明 |
|------|------|------|
| `/demo/tools` | GET | 列出 Server 暴露的所有 Tool |
| `/demo/prompts` | GET | 列出 Server 暴露的所有 Prompt |
| `/demo/add?a=&b=` | GET | 调用 add Tool |
| `/demo/whoami` | GET | 调用 whoami Tool，查看客户端身份 |
| `/demo/config?key=` | GET | 读取 Resource |
| `/demo/greeting?name=` | GET | 获取 Prompt |

## 常见问题

**Q: Client 启动报 `Client failed to initialize by explicit API call`**

MCP Server 未启动或不可达。确保先启动 Server（端口 8080），再启动 Client。

**Q: 直接 `java -jar` 报 `UnsupportedClassVersionError`**

项目使用 Java 25 编译，请用 `./gradlew bootRun` 启动，Gradle 会自动使用 toolchain 指定的 Java 版本。

**Q: curl 返回空 / 502**

系统配置了 HTTP 代理（如 `ALL_PROXY`），使用 `--noproxy localhost` 绕过代理。

**Q: Client 报 `Invalid SSE response. Status code: 401`**

Token 未正确传递。确认 Client 配置了 `McpClientCustomizer<HttpClientStreamableHttpTransport.Builder>` Bean，通过 `builder.httpRequestCustomizer()` 注入 Header。注意不要直接注册 `McpSyncHttpClientRequestCustomizer` Bean，Spring AI 自动配置不会使用它。

**Q: Client 报 `Invalid SSE response. Status code: 404`**

Server 与 Client 传输模式不匹配。确认 Server 的 `spring.ai.mcp.server.protocol` 与 Client 的连接方式一致：
- Streamable HTTP：Server 设置 `protocol: STREAMABLE`，Client 使用 `streamable-http` 配置
- SSE：Server 不设置 `protocol`，Client 使用 `sse` 配置
