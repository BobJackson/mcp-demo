package com.wangyousong.practice.clientdemo;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class McpDemoService {

    private final List<McpSyncClient> mcpClients;

    /**
     * 列出所有 MCP Server 提供的工具
     */
    public List<McpSchema.Tool> listTools() {
        return mcpClients.getFirst().listTools().tools();
    }

    /**
     * 列出所有 MCP Server 提供的 Prompt
     */
    public List<McpSchema.Prompt> listPrompts() {
        return mcpClients.getFirst().listPrompts().prompts();
    }

    /**
     * 调用 add 工具
     */
    public String callAdd(int a, int b) {
        McpSchema.CallToolResult result = mcpClients.getFirst().callTool(
                new McpSchema.CallToolRequest("add", Map.of("a", a, "b", b), Map.of())
        );
        return extractText(result);
    }

    /**
     * 读取配置资源
     */
    public String readConfig(String key) {
        McpSchema.ReadResourceResult result = mcpClients.getFirst().readResource(
                new McpSchema.ReadResourceRequest("config://" + key, Map.of())
        );
        return result.contents().stream()
                .filter(c -> c instanceof McpSchema.TextResourceContents)
                .map(c -> ((McpSchema.TextResourceContents) c).text())
                .reduce("", String::concat);
    }

    /**
     * 获取 Prompt
     */
    public String getGreeting(String name) {
        McpSchema.GetPromptResult result = mcpClients.getFirst().getPrompt(
                new McpSchema.GetPromptRequest("greeting", Map.of("name", name), null)
        );
        return result.messages().stream()
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
