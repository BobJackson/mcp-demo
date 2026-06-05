package com.wangyousong.practice.clientdemo;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
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

    /**
     * 列出所有 MCP Server 提供的工具
     */
    public List<McpSchema.Tool> listTools() {
        return getClient().listTools().tools();
    }

    /**
     * 列出所有 MCP Server 提供的 Prompt
     */
    public List<McpSchema.Prompt> listPrompts() {
        return getClient().listPrompts().prompts();
    }

    /**
     * 调用 add 工具
     */
    public String callAdd(int a, int b) {
        return extractText(getClient().callTool(
                new McpSchema.CallToolRequest("add", Map.of("a", a, "b", b), Map.of())
        ));
    }

    /**
     * 读取配置资源
     */
    public String readConfig(String key) {
        return getClient().readResource(new McpSchema.ReadResourceRequest("config://" + key, Map.of()))
                .contents().stream()
                .filter(c -> c instanceof McpSchema.TextResourceContents)
                .map(c -> ((McpSchema.TextResourceContents) c).text())
                .reduce("", String::concat);
    }

    /**
     * 获取 Prompt
     */
    public String getGreeting(String name) {
        return getClient().getPrompt(new McpSchema.GetPromptRequest("greeting", Map.of("name", name), null))
                .messages().stream()
                .filter(m -> m.content() instanceof McpSchema.TextContent)
                .map(m -> ((McpSchema.TextContent) m.content()).text())
                .reduce("", String::concat);
    }

    private String extractText(McpSchema.CallToolResult result) {
        return result.content().stream()
                .filter(c -> c instanceof McpSchema.TextContent)
                .map(c -> ((McpSchema.TextContent) c).text())
                .reduce("", String::concat);
    }
}
