package com.agentx.campus.agent;

import com.agentx.campus.dto.AgentChatResponse;
import com.agentx.campus.service.GroqAiService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GeneralAiAgent {

    private final GroqAiService groqAiService;

    public GeneralAiAgent(GroqAiService groqAiService) {
        this.groqAiService = groqAiService;
    }

    public AgentChatResponse process(String username, String query) {
        long startTime = System.currentTimeMillis();
        List<String> steps = new ArrayList<>();
        steps.add("Query Classifier: Evaluated query -> [General Campus / General AI Inquiry]");
        steps.add("Routing to General AI Copilot (No institutional RAG required)");

        String systemPrompt = "You are AgentX Campus Copilot, an intelligent, helpful academic assistant for students and faculty.\n"
                + "Answer the question clearly, concisely, and warmly. If it's a coding, algorithmic, or conceptual question, provide clean code and structured explanations.";

        String answer = groqAiService.generateResponse(systemPrompt, query);
        if (answer == null || answer.trim().isEmpty()) {
            answer = "Hello! I am your AgentX Campus AI Copilot. How can I assist you today with your coursework, coding, project development, or campus life?";
        }

        steps.add("✓ Response synthesized by General AI Copilot");
        long latency = System.currentTimeMillis() - startTime;

        return new AgentChatResponse(answer, "General AI Copilot", steps, Map.of("isRag", false), latency);
    }
}
