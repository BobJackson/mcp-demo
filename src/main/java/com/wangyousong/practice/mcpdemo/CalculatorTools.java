package com.wangyousong.practice.mcpdemo;

import io.modelcontextprotocol.spec.McpSchema;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class CalculatorTools {

    private final Map<String, String> configData = Map.of("name", "Bob", "age", "30");

    /**
     * 从当前请求上下文中获取客户端身份（由 McpAuthFilter 写入）。
     * Tool 方法在 /mcp/message 请求的同一线程内同步执行，因此 RequestContextHolder 可用。
     */
    private String getClientId() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return "unknown";
        HttpServletRequest req = attrs.getRequest();
        return (String) req.getAttribute("mcp-client-id");
    }

    @McpTool(name = "add", description = "Add two numbers together")
    public int add(
            @McpToolParam(description = "First number") int a,
            @McpToolParam(description = "Second number") int b) {
        String clientId = getClientId();
        log.info("[{}] Adding {} and {}", clientId, a, b);
        return a + b;
    }

    @McpTool(name = "whoami", description = "Return the identity of the calling client")
    public String whoami() {
        String clientId = getClientId();
        log.info("[whoami] caller={}", clientId);
        return "You are: " + clientId;
    }

    @McpResource(uri = "config://{key}", name = "Configuration")
    public String getConfig(String key) {
        return configData.get(key);
    }

    @McpPrompt(name = "greeting", description = "Generate a personalized greeting")
    public McpSchema.GetPromptResult greeting(@McpArg(name = "name", description = "User name") String name) {
        String message = "Hello, " + name + "! Welcome to the MCP Demo. How can I help you today?";
        return new McpSchema.GetPromptResult(
                "Greeting",
                List.of(new McpSchema.PromptMessage(
                        McpSchema.Role.ASSISTANT,
                        new McpSchema.TextContent(null, message, Map.of())
                )),
                Map.of());
    }
}