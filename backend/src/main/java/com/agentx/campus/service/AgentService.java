package com.agentx.campus.service;

import com.agentx.campus.agent.SupervisorAgent;
import com.agentx.campus.dto.AgentChatResponse;
import com.agentx.campus.model.AgentTaskLog;
import com.agentx.campus.repository.AgentTaskLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public List<AgentTaskLog> getRecentTasks() {
        return taskLogRepository.findTop10ByOrderByCreatedAtDesc();
    }
}
