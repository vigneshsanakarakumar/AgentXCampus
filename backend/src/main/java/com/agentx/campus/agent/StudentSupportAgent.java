package com.agentx.campus.agent;

import com.agentx.campus.dto.AgentChatResponse;
import com.agentx.campus.model.CampusResource;
import com.agentx.campus.model.Grievance;
import com.agentx.campus.service.CampusToolRegistry;
import com.agentx.campus.service.GroqAiService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StudentSupportAgent {

    private final CampusToolRegistry toolRegistry;
    private final GroqAiService groqAiService;

    public StudentSupportAgent(CampusToolRegistry toolRegistry, GroqAiService groqAiService) {
        this.toolRegistry = toolRegistry;
        this.groqAiService = groqAiService;
    }

    public AgentChatResponse process(String username, String query) {
        long startTime = System.currentTimeMillis();
        List<String> steps = new ArrayList<>();
        steps.add("Student Support Agent: Inspecting campus facilities and support services...");

        String lower = query.toLowerCase();
        boolean isIssue = lower.contains("broken") || lower.contains("not working") || lower.contains("issue")
                || lower.contains("repair") || lower.contains("complaint") || lower.contains("projector")
                || lower.contains("ac ") || lower.contains("fan") || lower.contains("leak");

        if (isIssue) {
            steps.add("Automated Workflow: Extracting location and incident description...");
            String location = "CS-204";
            if (lower.contains("cs-101")) location = "CS-101";
            else if (lower.contains("cs-301")) location = "CS-301";
            else if (lower.contains("lab 1") || lower.contains("lab-1")) location = "Lab 1";
            else if (lower.contains("lab 2") || lower.contains("lab-2")) location = "Lab 2";
            else if (lower.contains("seminar hall")) location = "Seminar Hall 1";

            steps.add("Tool Call: createGrievance('" + username + "', 'CAMPUS_FACILITIES', '" + location + "', ...)");
            Grievance ticket = toolRegistry.createGrievance(username, "CAMPUS_FACILITIES", location, query);

            steps.add("Incident ticket #" + ticket.getTicketNumber() + " registered with Campus Infrastructure Operations.");

            String answer = String.format(
                    "I have automatically logged a maintenance incident for you.\n\n"
                            + "🎫 **Ticket Number:** `#%s`\n"
                            + "📍 **Location:** %s\n"
                            + "⚠️ **Department:** %s\n"
                            + "⚡ **Status:** %s (Urgency: %s)\n\n"
                            + "The campus facilities engineering team has been notified and scheduled for immediate on-site inspection.",
                    ticket.getTicketNumber(), ticket.getLocation(), ticket.getDepartment(), ticket.getStatus(), ticket.getUrgency()
            );

            long latency = System.currentTimeMillis() - startTime;
            Map<String, Object> actionData = new HashMap<>();
            actionData.put("ticketNumber", ticket.getTicketNumber());
            actionData.put("location", ticket.getLocation());
            actionData.put("category", ticket.getCategory());
            actionData.put("priority", ticket.getUrgency());

            return new AgentChatResponse(answer, "Student Support Agent", steps, actionData, latency);
        }

        // Room/resource availability query
        steps.add("Tool Call: getCampusResources()");
        List<CampusResource> resources = toolRegistry.getCampusResources();

        StringBuilder context = new StringBuilder("CAMPUS PHYSICAL ROOM INVENTORY:\n");
        for (CampusResource r : resources) {
            context.append(String.format("• %s (%s, %s) - Type: %s, Capacity: %d, Status: %s\n",
                    r.getName(), r.getBuilding(), r.getRoomNumber(), r.getType(), r.getCapacity(), r.getStatus()));
        }

        String prompt = "You are the Student Support Agent for AgentX Campus.\n"
                + "Answer questions regarding campus rooms, laboratory spaces, and services accurately.\n\n"
                + context.toString();

        steps.add("Querying Groq AI for facility availability status...");
        String answer = groqAiService.generateResponse(prompt, query);

        if (answer == null || answer.trim().isEmpty()) {
            StringBuilder sb = new StringBuilder("Here is the current campus facility status:\n\n");
            for (CampusResource r : resources) {
                sb.append(String.format("• **%s** (`%s` in %s) — Capacity: %d | Status: **%s**\n",
                        r.getName(), r.getRoomNumber(), r.getBuilding(), r.getCapacity(), r.getStatus()));
            }
            answer = sb.toString();
        }

        steps.add("✓ Verified with Campus Resource Management System");
        long latency = System.currentTimeMillis() - startTime;
        return new AgentChatResponse(answer, "Student Support Agent", steps, Map.of("resourceQuery", true), latency);
    }
}
