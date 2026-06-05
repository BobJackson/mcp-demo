package com.wangyousong.practice.clientdemo;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
public class McpDemoService {

    private final ObjectProvider<List<McpSyncClient>> mcpClientsProvider;

    public McpDemoService(@Lazy ObjectProvider<List<McpSyncClient>> mcpClientsProvider) {
        this.mcpClientsProvider = mcpClientsProvider;
    }

    private McpSyncClient getClient() {
        List<McpSyncClient> clients = mcpClientsProvider.getIfAvailable();
        if (clients == null || clients.isEmpty()) {
            throw new RuntimeException("MCP Client 不可用，请确认 MCP Server 已启动");
        }
        return clients.getFirst();
    }

    private <T> T execute(Function<McpSyncClient, T> action, T fallback) {
        try {
            return action.apply(getClient());
        } catch (Exception e) {
            log.error("MCP 调用失败: {}", e.getMessage());
            return fallback;
        }
    }

    /**
     * 列出所有 MCP Server 提供的工具
     */
    public List<McpSchema.Tool> listTools() {
        return execute(c -> c.listTools().tools(), List.of());
    }

    /**
     * 列出所有 MCP Server 提供的 Prompt
     */
    public List<McpSchema.Prompt> listPrompts() {
        return execute(c -> c.listPrompts().prompts(), List.of());
    }

    /**
     * 调用 add 工具
     */
    public String callAdd(int a, int b) {
        return execute(
                c -> extractText(c.callTool(new McpSchema.CallToolRequest("add", Map.of("a", a, "b", b), Map.of()))),
                "调用失败"
        );
    }

    /**
     * 读取配置资源
     */
    public String readConfig(String key) {
        return execute(c -> c.readResource(new McpSchema.ReadResourceRequest("config://" + key, Map.of()))
                .contents().stream()
                .filter(content -> content instanceof McpSchema.TextResourceContents)
                .map(content -> ((McpSchema.TextResourceContents) content).text())
                .reduce("", String::concat), "调用失败");
    }

    /**
     * 获取 Prompt
     */
    public String getGreeting(String name) {
        return execute(c -> c.getPrompt(new McpSchema.GetPromptRequest("greeting", Map.of("name", name), null))
                .messages().stream()
                .filter(m -> m.content() instanceof McpSchema.TextContent)
                .map(m -> ((McpSchema.TextContent) m.content()).text())
                .reduce("", String::concat), "调用失败");
    }

    private String extractText(McpSchema.CallToolResult result) {
        return result.content().stream()
                .filter(c -> c instanceof McpSchema.TextContent)
                .map(c -> ((McpSchema.TextContent) c).text())
                .reduce("", String::concat);
    }
}
