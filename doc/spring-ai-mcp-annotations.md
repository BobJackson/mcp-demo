# Spring AI MCP 四大核心注解详解

## 1. @McpTool — 工具注册

**作用**：将方法标记为 MCP 工具，框架自动根据方法签名生成 JSON Schema，供 Client 发现和调用。

**示例**：

```java
@McpTool(name = "add", description = "Add two numbers together")
public int add(
        @McpToolParam(description = "First number") int a,
        @McpToolParam(description = "Second number") int b) {
    return a + b;
}
```

- `name`：工具名，Client 调用时的标识
- `description`：给 LLM 看的描述，**这是 LLM 决定调用哪个工具的关键依据**
- `@McpToolParam`：为参数生成 JSON Schema 的 `description` 和 `required` 字段

**Client 侧调用方式**：

```java
getClient().callTool(new McpSchema.CallToolRequest("add", Map.of("a", a, "b", b), Map.of()));
```

---

## 2. @McpResource — 资源访问

**作用**：暴露数据资源，通过 URI 模板供 Client 读取。类似于 REST 的 GET，但语义上是"获取上下文数据"，不是执行操作。

**示例**：

```java
@McpResource(uri = "config://{key}", name = "Configuration")
public String getConfig(String key) {
    return configData.get(key);
}
```

- `uri = "config://{key}"`：URI 模板，`{key}` 是路径参数，框架会自动将 URI 中的值注入到方法参数
- `name`：资源名称

**Client 侧读取方式**：

```java
getClient().readResource(new McpSchema.ReadResourceRequest("config://name", Map.of()));
```

**典型场景**：配置数据、知识库内容、文件内容等只读数据的暴露。

---

## 3. @McpPrompt — 提示词模板

**作用**：定义可复用的 Prompt 模板，Client 传入参数后获取预定义的消息结构。让 LLM 的交互更可控、可复用。

**示例**：

```java
@McpPrompt(name = "greeting", description = "Generate a personalized greeting")
public McpSchema.GetPromptResult greeting(@McpArg(name = "name") String name) {
    String message = "Hello, " + name + "! Welcome to the MCP Demo.";
    return new McpSchema.GetPromptResult(
        "Greeting",
        List.of(new McpSchema.PromptMessage(
            McpSchema.Role.ASSISTANT,
            new McpSchema.TextContent(null, message, Map.of())
        )),
        Map.of());
}
```

- `@McpArg`：定义 Prompt 模板的参数
- 返回值 `GetPromptResult`：包含角色（ASSISTANT/USER）和内容消息列表

**Client 侧获取方式**：

```java
getClient().getPrompt(new McpSchema.GetPromptRequest("greeting", Map.of("name", "Bob"), null));
```

---

## 4. @McpComplete — 自动补全

**作用**：为 Prompt 参数或 Resource URI 提供**自动补全建议**，帮助 Client 实现输入提示（类似 IDE 的智能提示）。

**为 Prompt 参数补全**：

```java
@McpComplete(prompt = "greeting", argument = "name")
public List<String> completeUserName(String prefix) {
    return List.of("Bob", "Alice", "Charlie").stream()
            .filter(n -> n.startsWith(prefix))
            .toList();
}
```

- `prompt`：关联哪个 Prompt
- `argument`：为该参数提供补全
- 返回值：匹配的候选列表

**为 Resource URI 补全**：

```java
@McpComplete(resource = "config://{key}")
public List<String> completeConfigKey(String prefix) {
    return configData.keySet().stream()
            .filter(k -> k.startsWith(prefix))
            .toList();
}
```

---

## 四大注解关系总结

| 注解            | 角色         | Client 操作        | 典型用途              |
|---------------|------------|------------------|-------------------|
| `@McpTool`    | Server 的能力  | `callTool()`     | 计算、查询、执行动作        |
| `@McpResource`| Server 的知识  | `readResource()` | 配置、文档、上下文数据       |
| `@McpPrompt`  | Server 的话术  | `getPrompt()`    | 预定义交互模板           |
| `@McpComplete`| Server 的提示  | `complete()`     | 参数自动补全            |

**设计原则**：Tool 是"做事"，Resource 是"给数据"，Prompt 是"给话术"，Complete 是"给建议"。四者配合，构成完整的 MCP Server 能力体系。
