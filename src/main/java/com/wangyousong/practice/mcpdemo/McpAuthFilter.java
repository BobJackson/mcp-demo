package com.wangyousong.practice.mcpdemo;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * MCP Server 鉴权 Filter
 * <p>
 * 拦截 /sse 和 /mcp/message 两个端点，校验客户端身份（Token）。
 * Token 可通过 query param（key）或 Header（X-MCP-Token）传递。
 * <p>
 * 校验通过后，将 token 存入 request attribute，供 @McpTool 方法读取。
 */
@Slf4j
public class McpAuthFilter implements Filter {

    /** 合法 Token 白名单，生产环境应改为配置或数据库 */
    private static final Set<String> VALID_TOKENS = Set.of("token-alice", "token-bob", "demo-key");

    /** Token 与客户端身份的映射 */
    private static final Map<String, String> TOKEN_TO_CLIENT = Map.of(
            "token-alice", "Alice",
            "token-bob", "Bob",
            "demo-key", "DemoUser"
    );

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // 优先从 Header 读取，再从 query param 读取
        String token = request.getHeader("X-MCP-Token");
        if (token == null || token.isBlank()) {
            token = request.getParameter("key");
        }

        String uri = request.getRequestURI();
        log.info("[McpAuth] {} {} | token={}", request.getMethod(), uri, token);

        if (token == null || !VALID_TOKENS.contains(token)) {
            log.warn("[McpAuth] 鉴权失败: uri={}, token={}", uri, token);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Invalid or missing token\"}");
            return;
        }

        // 校验通过，将客户端身份存入 request attribute
        String clientId = TOKEN_TO_CLIENT.getOrDefault(token, "unknown");
        request.setAttribute("mcp-client-token", token);
        request.setAttribute("mcp-client-id", clientId);

        log.info("[McpAuth] 鉴权通过: uri={}, clientId={}", uri, clientId);
        chain.doFilter(request, response);
    }
}
