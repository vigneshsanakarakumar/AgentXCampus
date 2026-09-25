package com.agentx.campus.agent;

import com.agentx.campus.dto.AgentChatResponse;
import com.agentx.campus.model.Grievance;
import com.agentx.campus.model.User;
import com.agentx.campus.repository.GrievanceRepository;
import com.agentx.campus.repository.UserRepository;
import com.agentx.campus.service.GroqAiService;
import com.agentx.campus.service.CampusToolRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class GrievanceAgent {

    private final GrievanceRepository grievanceRepository;
    private final UserRepository userRepository;
    private final GroqAiService groqAiService;
    private final NotificationAgent notificationAgent;
    private final ObjectMapper objectMapper;
    private final CampusToolRegistry campusToolRegistry;

    public GrievanceAgent(GrievanceRepository grievanceRepository,
                          UserRepository userRepository,
                          GroqAiService groqAiService,
                          NotificationAgent notificationAgent,
                          ObjectMapper objectMapper,
                          CampusToolRegistry campusToolRegistry) {
        this.grievanceRepository = grievanceRepository;
        this.userRepository = userRepository;
        this.groqAiService = groqAiService;
        this.notificationAgent = notificationAgent;
        this.objectMapper = objectMapper;
        this.campusToolRegistry = campusToolRegistry;
    }

    @Transactional
    public AgentChatResponse process(String username, String query) {
        long startTime = System.currentTimeMillis();
        List<String> steps = new ArrayList<>();
        steps.add("Analyzing natural language issue description...");

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        String systemPrompt = "You are the Grievance & Incident Classification Agent for AgentX Campus.\n"
                + "Extract structured operational issue details from the user's issue report.\n"
                + "Return ONLY a JSON object with these exact keys:\n"
                + "{\n"
                + "  \"category\": \"MAINTENANCE\" (or \"IT_SUPPORT\", \"ELECTRICAL\", \"FACILITIES\", \"GENERAL\"),\n"
                + "  \"location\": \"room or campus area extracted from text e.g. CS-204 or Block A\",\n"
                + "  \"issue\": \"concise description of problem\",\n"
                + "  \"priority\": \"HIGH\" (or \"MEDIUM\", \"LOW\")\n"
                + "}\n"
                + "Do not include any explanation or markdown, just the JSON.";

        steps.add("Extracting issue metadata using Groq AI...");
        String json = groqAiService.generateStructuredJson(systemPrompt, query);

        String category = "MAINTENANCE";
        String location = "Campus Room CS-204";
        String issue = query;
        String priority = "MEDIUM";

        if (json != null) {
            try {
                JsonNode root = objectMapper.readTree(json);
                if (root.has("category") && !root.path("category").asText().isEmpty()) {
                    category = root.path("category").asText().toUpperCase();
                }
                if (root.has("location") && !root.path("location").asText().isEmpty()) {
                    location = root.path("location").asText().toUpperCase();
                }
                if (root.has("issue") && !root.path("issue").asText().isEmpty()) {
                    issue = root.path("issue").asText();
                }
                if (root.has("priority") && !root.path("priority").asText().isEmpty()) {
                    priority = root.path("priority").asText().toUpperCase();
                }
            } catch (Exception ignored) {}
        }

        // Validate priority
        if (!List.of("HIGH", "MEDIUM", "LOW").contains(priority)) {
            priority = "MEDIUM";
        }

        steps.add("✓ Validated extracted fields: [" + category + " at " + location + ", Priority: " + priority + "]");

        // Create Real Database Grievance via CampusToolRegistry for smart routing
        Grievance g = campusToolRegistry.createGrievance(username, category, location, issue, priority, "Campus Facilities & Maintenance");
        String ticketNumber = g.getTicketNumber();
        String assignedInfo = g.getAssignedTo() != null ? g.getAssignedTo() : "Administration";

        steps.add("✓ Created real database incident ticket #" + ticketNumber + " (Assigned to: " + assignedInfo + ")");
        steps.add("Dispatched maintenance notification to " + assignedInfo);

        // Dispatch Notification to Submitter
        notificationAgent.notifyUser(user,
                "Grievance Ticket Created: #" + ticketNumber,
                "Your incident report regarding " + issue + " in " + location + " has been logged and assigned to " + assignedInfo + ".",
                "GRIEVANCE_UPDATE"
        );

        // If assigned to a mentor, notify the mentor
        if (g.getAssignedToUser() != null && !g.getAssignedToUser().getId().equals(user.getId())) {
            notificationAgent.notifyUser(g.getAssignedToUser(),
                    "New Student Grievance Assigned: #" + ticketNumber,
                    "Student " + (user.getFirstName() + " " + user.getLastName()).trim() + " reported: " + issue + " in " + location,
                    "GRIEVANCE_ASSIGNED"
            );
        }

        String answer = "I have logged your incident report into the campus operational queue:\n\n"
                + "**Incident Ticket:** #" + ticketNumber + "\n"
                + "**Category:** " + category + "\n"
                + "**Location:** " + location + "\n"
                + "**Priority:** " + priority + "\n"
                + "**Assigned To:** " + assignedInfo + "\n"
                + "**Status:** OPEN\n\n"
                + "A maintenance work-order has been registered and " + assignedInfo + " has been notified.";

        Map<String, Object> actionData = Map.of(
                "ticketNumber", ticketNumber,
                "location", location,
                "category", category,
                "priority", priority,
                "assignedTo", assignedInfo,
                "status", "OPEN"
        );

        long latency = System.currentTimeMillis() - startTime;
        return new AgentChatResponse(answer, "Grievance Agent", steps, actionData, latency);
    }
}
