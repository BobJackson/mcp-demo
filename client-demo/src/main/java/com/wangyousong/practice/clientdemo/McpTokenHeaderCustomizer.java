package com.wangyousong.practice.clientdemo;

import io.modelcontextprotocol.client.transport.customizer.McpSyncHttpClientRequestCustomizer;
import io.modelcontextprotocol.common.McpTransportContext;

import java.net.URI;
import java.net.http.HttpRequest;

/**
 * 为所有 MCP HTTP 请求注入 X-MCP-Token Header。
 * <p>
 * 解决场景：SSE 连接 URL 可以通过 ?key=xxx 鉴权，
 * 但 SDK 自动构建的 /mcp/message?sessionId=xxx URL 不含 key 参数，
 * 因此通过 Header 传递 token 作为兜底鉴权方式。
 */
public class McpTokenHeaderCustomizer implements McpSyncHttpClientRequestCustomizer {

    private final String token;

    public McpTokenHeaderCustomizer(String token) {
        this.token = token;
    }

    @Override
    public void customize(HttpRequest.Builder builder, String method, URI endpoint,
                          String body, McpTransportContext context) {
        builder.header("X-MCP-Token", token);
    }
}
