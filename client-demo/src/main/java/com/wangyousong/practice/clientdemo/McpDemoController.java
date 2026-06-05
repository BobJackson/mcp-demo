package com.wangyousong.practice.clientdemo;

import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/demo")
@RequiredArgsConstructor
public class McpDemoController {

    private final McpDemoService mcpDemoService;

    /**
     * 列出所有工具
     * GET /demo/tools
     */
    @GetMapping("/tools")
    public List<McpSchema.Tool> listTools() {
        return mcpDemoService.listTools();
    }

    /**
     * 列出所有 Prompt
     * GET /demo/prompts
     */
    @GetMapping("/prompts")
    public List<McpSchema.Prompt> listPrompts() {
        return mcpDemoService.listPrompts();
    }

    /**
     * 调用 add 工具
     * GET /demo/add?a=1&b=2
     */
    @GetMapping("/add")
    public String add(@RequestParam int a, @RequestParam int b) {
        return mcpDemoService.callAdd(a, b);
    }

    /**
     * 读取配置
     * GET /demo/config?key=name
     */
    @GetMapping("/config")
    public String config(@RequestParam String key) {
        return mcpDemoService.readConfig(key);
    }

    /**
     * 获取 greeting prompt
     * GET /demo/greeting?name=Bob
     */
    @GetMapping("/greeting")
    public String greeting(@RequestParam String name) {
        return mcpDemoService.getGreeting(name);
    }

    /**
     * 调用 whoami 工具，查看当前客户端身份
     * GET /demo/whoami
     */
    @GetMapping("/whoami")
    public String whoami() {
        return mcpDemoService.whoami();
    }
}
