package com.agentx.campus.agent;

import com.agentx.campus.dto.AgentChatResponse;
import com.agentx.campus.model.Opportunity;
import com.agentx.campus.service.OpportunityService;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Specialized Opportunity & Internship Agent.
 * Recommends internships, hackathons, and certifications matched to student department and standing.
 */
@Service
public class OpportunityAgent {

    private final OpportunityService opportunityService;

    public OpportunityAgent(OpportunityService opportunityService) {
        this.opportunityService = opportunityService;
    }

    public AgentChatResponse process(String username, String query) {
        long startTime = System.currentTimeMillis();
        List<String> steps = new ArrayList<>();
        steps.add("Opportunity Agent: Scanning campus opportunity board for eligible opportunities...");
        steps.add("Tool Call: searchInternships('" + username + "')");

        String typeFilter = null;
        String lower = query.toLowerCase();
        if (lower.contains("hackathon")) typeFilter = "HACKATHON";
        else if (lower.contains("internship")) typeFilter = "INTERNSHIP";
        else if (lower.contains("certification")) typeFilter = "CERTIFICATION";

        List<Opportunity> opps = opportunityService.getStudentOpportunities(username, typeFilter);
        steps.add(String.format("Found %d matching opportunit(ies)", opps.size()));

        StringBuilder sb = new StringBuilder();
        sb.append("### 🚀 Campus Opportunities & Internships for **").append(username).append("**\n\n");
        if (opps.isEmpty()) {
            sb.append("No active opportunities found matching your criteria right now. Check back soon!\n");
        } else {
            for (Opportunity o : opps) {
                sb.append("• **").append(o.getTitle()).append("** [").append(o.getType()).append("]\n")
                        .append("  ").append(o.getDescription()).append("\n")
                        .append("  *Eligibility:* ").append(o.getEligibilityText() != null ? o.getEligibilityText() : "All students").append("\n")
                        .append("  *Deadline:* ").append(o.getDeadline() != null ? o.getDeadline() : "Ongoing").append("\n")
                        .append("  *Link:* ").append(o.getExternalLink() != null ? o.getExternalLink() : "N/A").append("\n\n");
            }
        }

        sb.append("📌 *Source: Institutional Placement & Career Opportunities Board*");
        long latency = System.currentTimeMillis() - startTime;
        return new AgentChatResponse(sb.toString(), "Opportunity / Internship Agent", steps, opps, latency);
    }
}
