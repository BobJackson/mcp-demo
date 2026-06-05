package com.wangyousong.practice.mcpdemo;

import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.annotation.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class CalculatorTools {

    private final Map<String, String> configData = Map.of("name", "Bob", "age", "30");

    @McpTool(name = "add", description = "Add two numbers together")
    public int add(
            @McpToolParam(description = "First number") int a,
            @McpToolParam(description = "Second number") int b) {
        log.info("Adding {} and {}", a, b);
        return a + b;
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