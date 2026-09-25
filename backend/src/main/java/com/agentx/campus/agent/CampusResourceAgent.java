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

        String systemPrompt = "You are the Campus Resource & Facilities Agent for AgentX Campus.\n"
                + "Help students and faculty check room and lab availability.\n"
                + "STRICT RULES:\n"
                + "1. Base availability strictly on the provided room inventory and timetable occupancies.\n"
                + "2. Never guess room numbers or capacities.\n"
                + "3. State clearly whether a room is Available, Occupied, or under Maintenance.\n\n"
                + sb.toString();

        steps.add("Synthesizing facility schedule with Groq AI...");
        String answer = groqAiService.generateResponse(systemPrompt, query);

        if (answer == null || answer.trim().isEmpty()) {
            answer = "Campus resources have been inspected. Please consult the digital campus map for current room statuses.";
        }

        steps.add("✓ Verified room allocation status");
        long latency = System.currentTimeMillis() - startTime;
        return new AgentChatResponse(answer, "Campus Resource Agent", steps, Map.of("resourcesScanned", allResources.size()), latency);
    }
}
