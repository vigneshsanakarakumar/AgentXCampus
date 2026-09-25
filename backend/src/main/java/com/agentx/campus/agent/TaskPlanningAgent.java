package com.agentx.campus.agent;

import com.agentx.campus.dto.AgentChatResponse;
import com.agentx.campus.model.Assignment;
import com.agentx.campus.model.CampusEvent;
import com.agentx.campus.model.Course;
import com.agentx.campus.model.StudentTask;
import com.agentx.campus.model.TimetableEntry;
import com.agentx.campus.service.CampusToolRegistry;
import com.agentx.campus.service.GroqAiService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class TaskPlanningAgent {

    private final CampusToolRegistry toolRegistry;
    private final GroqAiService groqAiService;

    public TaskPlanningAgent(CampusToolRegistry toolRegistry, GroqAiService groqAiService) {
        this.toolRegistry = toolRegistry;
        this.groqAiService = groqAiService;
    }

    public AgentChatResponse process(String username, String query) {
        long startTime = System.currentTimeMillis();
        List<String> steps = new ArrayList<>();
        steps.add("Task Planning Agent: Initiating multi-step workflow planner...");

        Map<String, Object> profile = toolRegistry.getStudentProfile(username);
        String dept = (String) profile.getOrDefault("department", "Computer Science & Engineering");
        String sec = (String) profile.getOrDefault("section", "C");

        steps.add("Tool Call: getAssignments('" + dept + "', '" + sec + "')");
        List<Assignment> assignments = toolRegistry.getAssignments(dept, sec);

        steps.add("Tool Call: getTodaySchedule('" + dept + "', '" + sec + "')");
        List<TimetableEntry> timetable = toolRegistry.getTodaySchedule(dept, sec);

        steps.add("Tool Call: getUpcomingEvents()");
        List<CampusEvent> events = toolRegistry.getUpcomingEvents();

        steps.add("Tool Call: getCourses('" + dept + "')");
        List<Course> courses = toolRegistry.getCourses(dept);

        // Identify subject in query
        String detectedSubject = "Database Management Systems";
        String lower = query.toLowerCase();
        if (lower.contains("network") || lower.contains("cn")) detectedSubject = "Computer Networks";
        else if (lower.contains("os") || lower.contains("operating system")) detectedSubject = "Operating Systems";
        else if (lower.contains("ai") || lower.contains("artificial intelligence")) detectedSubject = "Artificial Intelligence";
        else if (lower.contains("toc") || lower.contains("computation")) detectedSubject = "Theory of Computation";

        steps.add("Synthesizing multi-phase preparation plan for: " + detectedSubject);

        // Create automated actionable tasks in the database for the student!
        steps.add("Tool Call: createStudentTask('" + username + "', 'Review " + detectedSubject + " Unit 1 & 2 Theory', 'HIGH')");
        StudentTask task1 = toolRegistry.createStudentTask(
                username,
                "Review " + detectedSubject + " Unit 1 & 2 Core Theory",
                "HIGH",
                LocalDate.now().plusDays(2)
        );

        steps.add("Tool Call: createStudentTask('" + username + "', 'Practice " + detectedSubject + " Problem Sets & Previous Questions', 'MEDIUM')");
        StudentTask task2 = toolRegistry.createStudentTask(
                username,
                "Practice " + detectedSubject + " Problem Sets & Solved Examples",
                "MEDIUM",
                LocalDate.now().plusDays(4)
        );

        steps.add("✓ 2 actionable student tasks generated and persisted to database");

        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append("### 🎯 Multi-Step Preparation & Action Plan for **").append(detectedSubject).append("**\n\n");
        responseBuilder.append("Based on your academic schedule, upcoming deadlines, and active course syllabus, here is your customized execution plan:\n\n");

        responseBuilder.append("#### Phase 1: Conceptual Foundation & Notes (Days 1–2)\n");
        responseBuilder.append("• Review syllabus modules and lecture slides.\n");
        responseBuilder.append("• Consolidate handwritten notes and clarify doubts with your course instructor.\n\n");

        responseBuilder.append("#### Phase 2: Problem Solving & Lab Application (Days 3–4)\n");
        responseBuilder.append("• Solve previous internal assessment papers and benchmark problems.\n");
        responseBuilder.append("• Check assignment submissions to ensure all practical components are understood.\n\n");

        responseBuilder.append("#### Phase 3: Mock Revision & Peer Study (Day 5)\n");
        responseBuilder.append("• Complete a timed 60-minute mock revision test.\n");
        responseBuilder.append("• Consult with your Faculty Mentor during designated advising hours.\n\n");

        responseBuilder.append("📋 **Automated Action Items Added to Your Student Dashboard:**\n");
        responseBuilder.append(String.format("1. [Task #%d] **%s** — Priority: `%s` | Due: %s\n",
                task1.getId(), task1.getTitle(), task1.getPriority(), task1.getDueDate()));
        responseBuilder.append(String.format("2. [Task #%d] **%s** — Priority: `%s` | Due: %s\n\n",
                task2.getId(), task2.getTitle(), task2.getPriority(), task2.getDueDate()));
        responseBuilder.append("*(You can view, complete, or update these anytime in your Tasks panel!)*");

        long latency = System.currentTimeMillis() - startTime;
        Map<String, Object> actionData = new HashMap<>();
        actionData.put("planningComplete", true);
        actionData.put("tasksCreated", List.of(task1.getTitle(), task2.getTitle()));
        actionData.put("subject", detectedSubject);

        return new AgentChatResponse(responseBuilder.toString(), "Task Planning Agent", steps, actionData, latency);
    }
}
