package com.wangyousong.practice.mcpdemo;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * 注册 McpAuthFilter，拦截 MCP SSE 相关请求进行 Token 鉴权。
 * <p>
 * 必须开启 asyncSupported，因为 SSE 是长连接，走异步处理。
 */
@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<McpAuthFilter> mcpAuthFilter() {
        FilterRegistrationBean<McpAuthFilter> reg = new FilterRegistrationBean<>(new McpAuthFilter());
        reg.setAsyncSupported(true);   // SSE 长连接必须开启
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE);
        // 拦截 MCP Server 的两个核心端点
        // /sse         — 客户端建立 SSE 连接（GET，可带 ?key=xxx）
        // /mcp/message — 客户端发送 JSON-RPC 消息（POST，可带 ?key=xxx）
        reg.addUrlPatterns("/sse", "/mcp/message");
        return reg;
    }
}
