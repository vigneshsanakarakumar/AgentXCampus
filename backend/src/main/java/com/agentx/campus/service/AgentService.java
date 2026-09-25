package com.agentx.campus.service;

import com.agentx.campus.agent.SupervisorAgent;
import com.agentx.campus.dto.AgentChatResponse;
import com.agentx.campus.model.AgentTaskLog;
import com.agentx.campus.repository.AgentTaskLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class AgentService {

    private final SupervisorAgent supervisorAgent;
    private final AgentTaskLogRepository taskLogRepository;

    public AgentService(SupervisorAgent supervisorAgent, AgentTaskLogRepository taskLogRepository) {
        this.supervisorAgent = supervisorAgent;
        this.taskLogRepository = taskLogRepository;
    }

    public AgentChatResponse processQuery(String username, String query) {
        return supervisorAgent.routeAndExecute(username, query);
    }

    public void processQueryStream(String username, String query, SseEmitter emitter) {
        CompletableFuture.runAsync(() -> {
            try {
                emitter.send(SseEmitter.event().name("step").data(Map.of("step", "Orchestrator Agent: Analyzing user intent & selecting specialized agent...")));

                AgentChatResponse response = supervisorAgent.routeAndExecute(username, query);

                // Stream execution steps
                if (response.getSteps() != null) {
                    for (String step : response.getSteps()) {
                        emitter.send(SseEmitter.event().name("step").data(Map.of("step", step)));
                        Thread.sleep(40);
                    }
                }

                // Stream response tokens (words with punctuation/spaces)
                String message = response.getMessage();
                if (message != null) {
                    String[] tokens = message.split("(?<=\\s)|(?<=[\\n])");
                    for (String token : tokens) {
                        emitter.send(SseEmitter.event().name("token").data(Map.of("token", token)));
                        Thread.sleep(15);
                    }
                }

                // Send final completion payload
                emitter.send(SseEmitter.event().name("done").data(response));
                emitter.complete();
            } catch (Exception ex) {
                try {
                    emitter.completeWithError(ex);
                } catch (Exception ignored) {}
            }
        });
    }

    public List<AgentTaskLog> getRecentTasks() {
        return taskLogRepository.findTop10ByOrderByCreatedAtDesc();
    }
}
