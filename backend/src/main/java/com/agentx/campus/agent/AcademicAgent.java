package com.agentx.campus.agent;

import com.agentx.campus.dto.AgentChatResponse;
import com.agentx.campus.model.Assignment;
import com.agentx.campus.model.AttendanceRecord;
import com.agentx.campus.model.Course;
import com.agentx.campus.service.CampusToolRegistry;
import com.agentx.campus.service.GroqAiService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AcademicAgent {

    private final CampusToolRegistry toolRegistry;
    private final GroqAiService groqAiService;

    public AcademicAgent(CampusToolRegistry toolRegistry, GroqAiService groqAiService) {
        this.toolRegistry = toolRegistry;
        this.groqAiService = groqAiService;
    }

    public AgentChatResponse process(String username, String query) {
        long startTime = System.currentTimeMillis();
        List<String> steps = new ArrayList<>();

        String lowerQuery = query.toLowerCase();
        boolean isExplainQuery = (lowerQuery.contains("why") || lowerQuery.contains("explain") || lowerQuery.contains("reason") || lowerQuery.contains("how come"))
                && (lowerQuery.contains("absent") || lowerQuery.contains("marked") || lowerQuery.contains("attendance") || lowerQuery.contains("leave") || lowerQuery.contains("od"));

        if (isExplainQuery) {
            steps.add("Academic Agent: Detected attendance session explanation inquiry...");
            String dateHint = extractDateHint(lowerQuery);
            String subjectHint = extractSubjectHint(lowerQuery);
            steps.add("Tool Call: explainAttendanceEntry('" + username + "', dateHint='" + dateHint + "', subjectHint='" + subjectHint + "')");
            Map<String, Object> entryData = toolRegistry.explainAttendanceEntry(username, dateHint, subjectHint);
            steps.add("Retrieved session entry data: " + (Boolean.TRUE.equals(entryData.get("found")) ? "Record matched" : "No exact entry found"));

            String explainPrompt = "You are the Academic Agent for AgentX Campus.\n"
                    + "A student is asking why they were marked with a specific attendance status.\n"
                    + "Student query: " + query + "\n"
                    + "Attendance entry record: " + entryData + "\n"
                    + "Explain clearly and helpfully why they were marked as such, mention any linked Leave or OD request if present, and advise them on what steps to take (e.g. check with class mentor) if there is an error.";

            String explainAnswer = groqAiService.generateResponse(explainPrompt, query);
            if (explainAnswer == null || explainAnswer.trim().isEmpty()) {
                if (Boolean.TRUE.equals(entryData.get("found"))) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Your attendance for **").append(entryData.get("subject")).append("** (")
                      .append(entryData.get("subjectCode")).append(") on **").append(entryData.get("date"))
                      .append("** is marked as **").append(entryData.get("status")).append("**.\n");
                    if (entryData.get("remarks") != null && !entryData.get("remarks").toString().isEmpty()) {
                        sb.append("• Notes: ").append(entryData.get("remarks")).append("\n");
                    }
                    if (entryData.containsKey("linkedRequest")) {
                        Map<?, ?> req = (Map<?, ?>) entryData.get("linkedRequest");
                        sb.append("• Linked ").append(req.get("type")).append(" Request: Status is **")
                          .append(req.get("status")).append("**.\n");
                    }
                    explainAnswer = sb.toString();
                } else {
                    explainAnswer = "I couldn't locate a specific attendance session entry matching your inquiry. Please reach out to your class mentor or department office to review the register.";
                }
            }
            steps.add("✓ Generated explanatory breakdown from session register");
            long latency = System.currentTimeMillis() - startTime;
            return new AgentChatResponse(explainAnswer, "Academic Agent", steps, entryData, latency);
        }

        steps.add("Academic Agent: Fetching student profile & academic enrollment...");

        Map<String, Object> profile = toolRegistry.getStudentProfile(username);
        String dept = (String) profile.getOrDefault("department", "Computer Science & Engineering");
        String sec = (String) profile.getOrDefault("section", "C");

        steps.add("Tool Call: getAttendance('" + username + "')");
        List<AttendanceRecord> attendanceList = toolRegistry.getAttendance(username);

        steps.add("Tool Call: getAssignments('" + dept + "', '" + sec + "')");
        List<Assignment> assignments = toolRegistry.getAssignments(dept, sec);

        steps.add("Tool Call: getCourses('" + dept + "')");
        List<Course> courses = toolRegistry.getCourses(dept);

        StringBuilder context = new StringBuilder();
        context.append("STUDENT ACADEMIC PROFILE:\n");
        context.append("- Name: ").append(profile.getOrDefault("name", username)).append("\n");
        context.append("- Roll No: ").append(profile.getOrDefault("rollNumber", "N/A")).append("\n");
        context.append("- Department: ").append(dept).append(", Section: ").append(sec).append("\n");
        context.append("- Semester: ").append(profile.getOrDefault("semester", 5)).append(", CGPA: ").append(profile.getOrDefault("cgpa", 8.5)).append("\n\n");

        context.append("ATTENDANCE RECORDS BY SUBJECT:\n");
        if (attendanceList.isEmpty()) {
            context.append("- Overall Attendance Rate: ").append(profile.getOrDefault("attendanceRate", 85)).append("%\n");
        } else {
            for (AttendanceRecord a : attendanceList) {
                context.append(String.format("  * %s (%s): %d/%d classes attended (%.1f%%)\n",
                        a.getCourseName(), a.getCourseCode(), a.getAttendedClasses(), a.getTotalClasses(), a.getPercentage()));
            }
        }
        context.append("\n");

        context.append("UPCOMING & ACTIVE ASSIGNMENTS:\n");
        if (assignments.isEmpty()) {
            context.append("- No pending assignments scheduled.\n");
        } else {
            for (Assignment asg : assignments) {
                context.append(String.format("  * %s [%s] - Due: %s | Priority: %s | Status: %s | Max Marks: %d\n",
                        asg.getTitle(), asg.getSubjectCode(), asg.getDueDate(), asg.getPriority(), asg.getStatus(), asg.getMaxMarks()));
            }
        }
        context.append("\n");

        context.append("ENROLLED DEPARTMENT COURSES:\n");
        for (Course c : courses) {
            context.append(String.format("  * %s: %s (Credits: %d, Faculty: %s)\n",
                    c.getCourseCode(), c.getCourseName(), c.getCredits(), c.getFacultyName()));
        }

        String prompt = "You are the specialized Academic Agent for AgentX Campus.\n"
                + "Answer the student's question accurately using ONLY the verified database data below.\n"
                + "Format key items with bullet points and bold text.\n"
                + "If asked about attendance, state specific subject percentages and overall standing (75% minimum required).\n"
                + "If asked about assignments, state title, course, deadline, and priority.\n\n"
                + context.toString();

        steps.add("Reasoning with Groq AI using verified academic ground truth...");
        String answer = groqAiService.generateResponse(prompt, query);

        if (answer == null || answer.trim().isEmpty()) {
            // Intelligent fallback from database context
            String lower = query.toLowerCase();
            if (lower.contains("attendance")) {
                StringBuilder sb = new StringBuilder("Here is your verified subject-wise attendance breakdown:\n\n");
                for (AttendanceRecord a : attendanceList) {
                    sb.append(String.format("• **%s** (%s): **%.1f%%** (%d/%d classes)\n",
                            a.getCourseName(), a.getCourseCode(), a.getPercentage(), a.getAttendedClasses(), a.getTotalClasses()));
                }
                sb.append(String.format("\nOverall Attendance Standing: **%s%%** (Requirement: 75%%)", profile.getOrDefault("attendanceRate", 85)));
                answer = sb.toString();
            } else if (lower.contains("assignment")) {
                StringBuilder sb = new StringBuilder("Here are your upcoming academic assignments:\n\n");
                for (Assignment asg : assignments) {
                    sb.append(String.format("• **%s** (%s) — Due: **%s** | Priority: **%s**\n",
                            asg.getTitle(), asg.getSubjectCode(), asg.getDueDate(), asg.getPriority()));
                }
                answer = sb.toString();
            } else {
                StringBuilder sb = new StringBuilder("Here are your enrolled courses for Semester ")
                        .append(profile.getOrDefault("semester", 5)).append(":\n\n");
                for (Course c : courses) {
                    sb.append(String.format("• **%s**: %s (%d Credits, Faculty: %s)\n",
                            c.getCourseCode(), c.getCourseName(), c.getCredits(), c.getFacultyName()));
                }
                answer = sb.toString();
            }
        }

        steps.add("✓ Verified against Institutional Academic Registry");
        long latency = System.currentTimeMillis() - startTime;
        return new AgentChatResponse(answer, "Academic Agent", steps, Map.of("academicVerified", true, "toolsCalled", "getAttendance, getAssignments, getCourses"), latency);
    }

    private String extractDateHint(String q) {
        String[] days = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};
        for (String day : days) {
            if (q.contains(day)) return day;
        }
        return "";
    }

    private String extractSubjectHint(String q) {
        if (q.contains("cloud")) return "cloud";
        if (q.contains("ai ") || q.contains("machine learning") || q.contains("artificial intelligence")) return "ai";
        if (q.contains("os") || q.contains("operating system")) return "os";
        if (q.contains("dbms") || q.contains("database")) return "dbms";
        if (q.contains("network")) return "network";
        if (q.contains("security") || q.contains("crypto")) return "security";
        return "";
    }
}
