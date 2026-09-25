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

        String systemPrompt = "You are AgentX Campus Copilot, an intelligent, advanced AI pairing partner and tutor for students and faculty.\n"
                + "GUIDELINES:\n"
                + "1. When answering coding, algorithmic, or technical questions, provide clean, idiomatic code snippets, time/space complexity analysis, and clear step-by-step explanations.\n"
                + "2. When answering general questions or greetings, be welcoming, articulate, and academically rigorous.\n"
                + "3. Note: You handle general open-ended reasoning; specific institutional regulations, registered timetables, and personal student grades are handled by the campus registry agents.";

        String answer = groqAiService.generateResponse(systemPrompt, query);
        if (answer == null || answer.trim().isEmpty()) {
            answer = "Hello! I am your AgentX Campus AI Copilot. How can I assist you today with your coursework, coding, project development, or campus life?";
        }

        steps.add("✓ Response synthesized by General AI Copilot");
        long latency = System.currentTimeMillis() - startTime;

        return new AgentChatResponse(answer, "General AI Copilot", steps, Map.of("isRag", false), latency);
    }
}
