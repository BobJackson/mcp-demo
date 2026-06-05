package com.wangyousong.practice.clientdemo;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    /**
     * 列出所有 MCP Server 提供的工具
     */
    public List<McpSchema.Tool> listTools() {
        try {
            return getClient().listTools().tools();
        } catch (Exception e) {
            log.error("MCP 调用失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 列出所有 MCP Server 提供的 Prompt
     */
    public List<McpSchema.Prompt> listPrompts() {
        try {
            return getClient().listPrompts().prompts();
        } catch (Exception e) {
            log.error("MCP 调用失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 调用 add 工具
     */
    public String callAdd(int a, int b) {
        try {
            McpSchema.CallToolResult result = getClient().callTool(
                    new McpSchema.CallToolRequest("add", Map.of("a", a, "b", b), Map.of())
            );
            return extractText(result);
        } catch (Exception e) {
            log.error("MCP 调用失败: {}", e.getMessage());
            return "调用失败: " + e.getMessage();
        }
    }

    /**
     * 读取配置资源
     */
    public String readConfig(String key) {
        try {
            McpSchema.ReadResourceResult result = getClient().readResource(
                    new McpSchema.ReadResourceRequest("config://" + key, Map.of())
            );
            return result.contents().stream()
                    .filter(c -> c instanceof McpSchema.TextResourceContents)
                    .map(c -> ((McpSchema.TextResourceContents) c).text())
                    .reduce("", String::concat);
        } catch (Exception e) {
            log.error("MCP 调用失败: {}", e.getMessage());
            return "调用失败: " + e.getMessage();
        }
    }

    /**
     * 获取 Prompt
     */
    public String getGreeting(String name) {
        try {
            McpSchema.GetPromptResult result = getClient().getPrompt(
                    new McpSchema.GetPromptRequest("greeting", Map.of("name", name), null)
            );
            return result.messages().stream()
                    .filter(m -> m.content() instanceof McpSchema.TextContent)
                    .map(m -> ((McpSchema.TextContent) m.content()).text())
                    .reduce("", String::concat);
        } catch (Exception e) {
            log.error("MCP 调用失败: {}", e.getMessage());
            return "调用失败: " + e.getMessage();
        }
    }

    private String extractText(McpSchema.CallToolResult result) {
        return result.content().stream()
                .filter(c -> c instanceof McpSchema.TextContent)
                .map(c -> ((McpSchema.TextContent) c).text())
                .reduce("", String::concat);
    }
}
