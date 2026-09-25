package com.agentx.campus.agent;

import com.agentx.campus.dto.AgentChatResponse;
import com.agentx.campus.model.ExamSchedule;
import com.agentx.campus.repository.ExamScheduleRepository;
import com.agentx.campus.service.ConflictEngine;
import com.agentx.campus.service.GroqAiService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class ExaminationAgent {

    private final ConflictEngine conflictEngine;
    private final ExamScheduleRepository examScheduleRepository;
    private final GroqAiService groqAiService;

    public ExaminationAgent(ConflictEngine conflictEngine,
                            ExamScheduleRepository examScheduleRepository,
                            GroqAiService groqAiService) {
        this.conflictEngine = conflictEngine;
        this.examScheduleRepository = examScheduleRepository;
        this.groqAiService = groqAiService;
    }

    public AgentChatResponse process(String username, String query) {
        long startTime = System.currentTimeMillis();
        List<String> steps = new ArrayList<>();
        steps.add("Examination Agent: Detected exam scheduling or conflict verification request");

        String lowerQuery = query.toLowerCase();

        // Extract subject parameters from query
        String subjectCode = "CS301";
        String subjectName = "Database Management Systems";
        if (lowerQuery.contains("network") || lowerQuery.contains("cs302")) {
            subjectCode = "CS302";
            subjectName = "Computer Networks";
        } else if (lowerQuery.contains("os") || lowerQuery.contains("operating system") || lowerQuery.contains("cs303")) {
            subjectCode = "CS303";
            subjectName = "Operating Systems";
        } else if (lowerQuery.contains("ai") || lowerQuery.contains("artificial") || lowerQuery.contains("cs304")) {
            subjectCode = "CS304";
            subjectName = "Artificial Intelligence";
        }

        String dept = "Computer Science & Engineering";
        String section = "A";
        String room = "Room 302";
        if (lowerQuery.contains("room 301") || lowerQuery.contains("hall 301")) room = "Room 301";
        if (lowerQuery.contains("room 204") || lowerQuery.contains("hall 204")) room = "Room 204";

        LocalDate examDate = LocalDate.now().plusDays(1);
        String startTimeStr = "10:00 AM";
        String endTimeStr = "01:00 PM";
        if (lowerQuery.contains("9 am") || lowerQuery.contains("09:00")) {
            startTimeStr = "09:00 AM";
            endTimeStr = "12:00 PM";
        } else if (lowerQuery.contains("2 pm") || lowerQuery.contains("02:00")) {
            startTimeStr = "02:00 PM";
            endTimeStr = "05:00 PM";
        }

        steps.add(String.format("Candidate Exam: %s (%s) | %s Sec %s | Date: %s | Time: %s - %s | Room: %s",
                subjectName, subjectCode, dept, section, examDate, startTimeStr, endTimeStr, room));

        // Build candidate
        ExamSchedule candidate = new ExamSchedule();
        candidate.setSubjectCode(subjectCode);
        candidate.setSubjectName(subjectName);
        candidate.setDepartment(dept);
        candidate.setSection(section);
        candidate.setExamDate(examDate);
        candidate.setStartTime(startTimeStr);
        candidate.setEndTime(endTimeStr);
        candidate.setRoom(room);
        candidate.setExamType("INTERNAL");

        // Execute Conflict Engine
        steps.add("Conflict Tool: Executing multi-dimensional clash detection (Room, Section, Regular Timetable)...");
        List<Map<String, Object>> conflicts = conflictEngine.checkExamConflicts(candidate, null);

        StringBuilder sb = new StringBuilder();
        Map<String, Object> actionData = new HashMap<>();
        actionData.put("candidate", Map.of(
                "subject", subjectName,
                "code", subjectCode,
                "date", examDate.toString(),
                "time", startTimeStr + " - " + endTimeStr,
                "room", room,
                "section", dept + " Sec " + section
        ));
        actionData.put("conflicts", conflicts);
        actionData.put("hasConflict", !conflicts.isEmpty());

        if (conflicts.isEmpty()) {
            steps.add("✓ Conflict Engine: Zero clashes detected. Slot and venue are clear.");
            sb.append(String.format("### ✅ Exam Slot Verified: No Conflicts Detected\n\n" +
                    "The proposed exam slot for **%s (%s)** is completely **clear** and available for scheduling.\n\n" +
                    "**Schedule Details:**\n" +
                    "• **Subject**: %s (%s)\n" +
                    "• **Target**: %s (Section %s)\n" +
                    "• **Date**: %s\n" +
                    "• **Time Slot**: %s to %s\n" +
                    "• **Assigned Room**: %s\n\n" +
                    "You can confirm this exam schedule in the Examination Scheduler module.",
                    subjectName, subjectCode, subjectName, subjectCode, dept, section, examDate, startTimeStr, endTimeStr, room));
        } else {
            steps.add(String.format("⚠️ Conflict Engine: Detected %d scheduling conflict(s)", conflicts.size()));
            sb.append(String.format("### ⚠️ Scheduling Conflict Detected for %s (%s)\n\n" +
                    "The proposed slot on **%s** from **%s to %s** in **%s** has **%d conflict(s)**:\n\n",
                    subjectName, subjectCode, examDate, startTimeStr, endTimeStr, room, conflicts.size()));

            for (int i = 0; i < conflicts.size(); i++) {
                Map<String, Object> c = conflicts.get(i);
                sb.append(String.format("**Conflict #%d: %s (%s Severity)**\n",
                        i + 1, c.get("type"), c.get("severity")));
                sb.append("• **Details**: ").append(c.get("message")).append("\n");
                if (c.containsKey("recommendation")) {
                    sb.append("• **AI Recommendation**: ").append(c.get("recommendation")).append("\n");
                }
                sb.append("\n");
            }

            sb.append("Please adjust the target room or time slot to prevent overlap before publishing.");
        }

        long latency = System.currentTimeMillis() - startTime;
        return new AgentChatResponse(sb.toString(), "Examination Agent", steps, actionData, latency);
    }
}
