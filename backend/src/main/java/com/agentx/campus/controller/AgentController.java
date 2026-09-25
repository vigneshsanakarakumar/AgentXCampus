package com.agentx.campus.controller;

import com.agentx.campus.dto.AgentChatRequest;
import com.agentx.campus.dto.AgentChatResponse;
import com.agentx.campus.service.AgentService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(Authentication authentication, @RequestBody AgentChatRequest request) {
        SseEmitter emitter = new SseEmitter(180_000L);
        agentService.processQueryStream(authentication.getName(), request.getQuery(), emitter);
        return emitter;
    }

    @GetMapping("/tasks")
    public ResponseEntity<?> getRecentTasks() {
        return ResponseEntity.ok(agentService.getRecentTasks());
    }
}
