package com.agentx.campus.controller;

import com.agentx.campus.dto.AgentChatRequest;
import com.agentx.campus.dto.AgentChatResponse;
import com.agentx.campus.service.AgentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/agent")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping("/chat")
    public ResponseEntity<AgentChatResponse> chat(Authentication authentication, @RequestBody AgentChatRequest request) {
        AgentChatResponse response = agentService.processQuery(authentication.getName(), request.getQuery());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tasks")
    public ResponseEntity<?> getRecentTasks() {
        return ResponseEntity.ok(agentService.getRecentTasks());
    }
}
