package com.wangyousong.practice.clientdemo;

import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import org.springframework.ai.mcp.customizer.McpClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 注册 MCP SSE Transport 定制器。
 * <p>
 * Spring AI 自动配置 SSE Transport 时，会查找
 * {@code McpClientCustomizer<HttpClientSseClientTransport.Builder>} Bean，
 * 通过 {@code builder.httpRequestCustomizer()} 将 Header 注入器应用到
 * 所有 HTTP 请求（含 SSE GET 建连 + POST 消息发送）。
 */
@Configuration
public class McpHttpClientConfig {

    /** 与 Server 端 McpAuthFilter 的 VALID_TOKENS 保持一致 */
    private static final String CLIENT_TOKEN = "token-alice";

    @Bean
    public McpClientCustomizer<HttpClientSseClientTransport.Builder> sseTransportCustomizer() {
        return (name, builder) ->
                builder.httpRequestCustomizer(new McpTokenHeaderCustomizer(CLIENT_TOKEN));
    }
}
