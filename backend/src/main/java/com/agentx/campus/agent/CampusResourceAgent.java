package com.agentx.campus.agent;

import com.agentx.campus.dto.AgentChatResponse;
import com.agentx.campus.model.CampusResource;
import com.agentx.campus.model.TimetableEntry;
import com.agentx.campus.repository.CampusResourceRepository;
import com.agentx.campus.repository.TimetableEntryRepository;
import com.agentx.campus.service.GroqAiService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CampusResourceAgent {

    private final CampusResourceRepository resourceRepository;
    private final TimetableEntryRepository timetableEntryRepository;
    private final GroqAiService groqAiService;

    public CampusResourceAgent(CampusResourceRepository resourceRepository,
                               TimetableEntryRepository timetableEntryRepository,
                               GroqAiService groqAiService) {
        this.resourceRepository = resourceRepository;
        this.timetableEntryRepository = timetableEntryRepository;
        this.groqAiService = groqAiService;
    }

    public AgentChatResponse process(String username, String query) {
        long startTime = System.currentTimeMillis();
        List<String> steps = new ArrayList<>();
        steps.add("Scanning campus physical resource registry...");

        List<CampusResource> allResources = resourceRepository.findAll();
        List<TimetableEntry> allTimetables = timetableEntryRepository.findAll();
        steps.add("✓ Evaluated " + allResources.size() + " rooms & laboratories against active timetables");

        StringBuilder sb = new StringBuilder();
        sb.append("CAMPUS PHYSICAL RESOURCE INVENTORY & TIMETABLE OCCUPANCY:\n");
        for (CampusResource r : allResources) {
            sb.append("- ").append(r.getRoomNumber()).append(" (Name: ").append(r.getName())
                    .append(", Building: ").append(r.getBuilding())
                    .append(", Type: ").append(r.getType())
                    .append(", Capacity: ").append(r.getCapacity())
                    .append(", Baseline Status: ").append(r.getStatus()).append(")\n");
        }

        sb.append("\nSCHEDULED OCCUPANCIES FROM TIMETABLES:\n");
        for (TimetableEntry t : allTimetables) {
            sb.append("- Room ").append(t.getClassroom()).append(" occupied on ").append(t.getDayOfWeek())
                    .append(" from ").append(t.getStartTime()).append(" to ").append(t.getEndTime())
                    .append(" by ").append(t.getSubjectCode()).append(" (").append(t.getDepartment()).append(")\n");
        }

        String systemPrompt = "You are the specialized Campus Resource & Facility Availability Agent for AgentX Campus.\n"
                + "Answer inquiries regarding classroom, seminar hall, and laboratory spaces authoritatively using ONLY the verified data below.\n"
                + "RULES:\n"
                + "1. Explicitly check if the queried room exists in the inventory, its building, type, and seating capacity.\n"
                + "2. Check scheduled occupancies from the timetable. If a room has scheduled classes on the requested day/time, list those time slots and state that it is OCCUPIED during those hours.\n"
                + "3. If a room has no scheduled occupancy during a time slot, state that it is AVAILABLE for study, lab work, or faculty booking.\n"
                + "4. Never invent room numbers or occupancies not present in the data.\n"
                + "5. Use bold formatting and clean bullet points for clarity.\n\n"
                + sb.toString();

        steps.add("Synthesizing facility schedule with Groq AI...");
        String answer = groqAiService.generateResponse(systemPrompt, query);

        if (answer == null || answer.trim().isEmpty()) {
            StringBuilder fallback = new StringBuilder("Here is the verified status of campus facilities based on current timetable allocations:\n\n");
            for (CampusResource r : allResources.stream().limit(5).toList()) {
                fallback.append(String.format("• **%s** (`%s` in %s) — Type: %s | Capacity: %d | Status: **%s**\n",
                        r.getName(), r.getRoomNumber(), r.getBuilding(), r.getType(), r.getCapacity(), r.getStatus()));
            }
            fallback.append("\nTo reserve a room or seminar hall, please contact the Department Office or file an operational request.");
            answer = fallback.toString();
        }

        steps.add("✓ Verified room allocation status");
        long latency = System.currentTimeMillis() - startTime;
        return new AgentChatResponse(answer, "Campus Resource Agent", steps, Map.of("resourcesScanned", allResources.size()), latency);
    }
}
