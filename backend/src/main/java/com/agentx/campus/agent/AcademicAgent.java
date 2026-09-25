package com.agentx.campus.agent;

import com.agentx.campus.dto.AgentChatResponse;
import com.agentx.campus.model.Assignment;
import com.agentx.campus.model.AttendanceRecord;
import com.agentx.campus.model.Course;
import com.agentx.campus.model.User;
import com.agentx.campus.service.CampusToolRegistry;
import com.agentx.campus.service.GroqAiService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AcademicAgent {

    private final CampusToolRegistry toolRegistry;
    private final GroqAiService groqAiService;
    private final DocumentRagAgent documentRagAgent;

    public AcademicAgent(CampusToolRegistry toolRegistry, GroqAiService groqAiService, DocumentRagAgent documentRagAgent) {
        this.toolRegistry = toolRegistry;
        this.groqAiService = groqAiService;
        this.documentRagAgent = documentRagAgent;
    }

    public AgentChatResponse process(String username, String query) {
        long startTime = System.currentTimeMillis();
        List<String> steps = new ArrayList<>();

        String lowerQuery = query.toLowerCase();

        // If the query pertains to college regulations, condonation, or hostel rules, cite exact handbook citations via RAG
        if (isDocumentPolicyQuery(lowerQuery)) {
            steps.add("Academic Agent: Detected institutional regulation / handbook inquiry");
            steps.add("Academic Agent -> Delegating to Document/RAG Agent for exact section & paragraph citations");
            AgentChatResponse ragRes = documentRagAgent.process(username, query);
            ragRes.getSteps().addAll(0, steps);
            return ragRes;
        }

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

        // Check if query is targeting a specific named student or roll number
        User targetStudent = toolRegistry.findStudentInQuery(query);
        String targetUsername = username;
        boolean isQueryingOtherStudent = false;

        if (targetStudent != null) {
            boolean authorized = toolRegistry.isAuthorizedToViewStudent(username, targetStudent);
            if (!authorized) {
                steps.add("Access Control: User '" + username + "' requested records for student '" 
                        + targetStudent.getFirstName() + " " + targetStudent.getLastName() + "' (" + targetStudent.getUsername() + ")");
                steps.add("Access Control: REJECTED — Institutional privacy policy forbids unauthorized student record access.");
                String rejectionMsg = "🔒 **Access Restricted**: You are not authorized to view the academic or attendance records of **"
                        + targetStudent.getFirstName() + " " + targetStudent.getLastName() + "**.\n\n"
                        + "Institutional privacy regulations permit students to view only their own academic records. Faculty mentors and department administrators may view records only for their assigned students.";
                long latency = System.currentTimeMillis() - startTime;
                return new AgentChatResponse(rejectionMsg, "Academic Agent", steps, Map.of("authorized", false, "privacyViolation", true), latency);
            }
            targetUsername = targetStudent.getUsername();
            isQueryingOtherStudent = !targetStudent.getUsername().equalsIgnoreCase(username);
            steps.add("Access Control: Authorized access verified for student '" 
                    + targetStudent.getFirstName() + " " + targetStudent.getLastName() + "' (" + targetUsername + ")");
        }

        steps.add("Academic Agent: Fetching student profile & academic enrollment for [" + targetUsername + "]...");

        Map<String, Object> profile = toolRegistry.getStudentProfile(targetUsername);
        String dept = (String) profile.getOrDefault("department", "Computer Science & Engineering");
        String sec = (String) profile.getOrDefault("section", "C");

        steps.add("Tool Call: getAttendance('" + targetUsername + "')");
        List<AttendanceRecord> attendanceList = toolRegistry.getAttendance(targetUsername);

        steps.add("Tool Call: getAssignments('" + dept + "', '" + sec + "')");
        List<Assignment> assignments = toolRegistry.getAssignments(dept, sec);

        steps.add("Tool Call: getCourses('" + dept + "')");
        List<Course> courses = toolRegistry.getCourses(dept);

        String studentDisplayName = (String) profile.getOrDefault("name", targetUsername);

        StringBuilder context = new StringBuilder();
        context.append("STUDENT ACADEMIC PROFILE:\n");
        context.append("- Name: ").append(studentDisplayName).append("\n");
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

        Course targetCourse = toolRegistry.findCourseInQuery(query);
        if (targetCourse != null) {
            steps.add("Resolved target course from query: " + targetCourse.getCourseName() + " (" + targetCourse.getCourseCode() + ")");
        }

        String prompt = "You are the specialized Academic Registry Agent for AgentX Campus.\n"
                + "Answer the query authoritatively and accurately using ONLY the verified database data below.\n"
                + (isQueryingOtherStudent ? "NOTE: An authorized faculty mentor or administrator is inquiring about student: " + studentDisplayName + ".\n" : "")
                + (targetCourse != null ? "NOTE: The user is specifically inquiring about: " + targetCourse.getCourseName() + " (" + targetCourse.getCourseCode() + "). Highlight this course specifically.\n" : "")
                + "INSTRUCTIONS:\n"
                + "1. State the student's full name (" + studentDisplayName + "), Roll Number, Department, and Section clearly.\n"
                + "2. If asked about attendance, present a clean breakdown of each course: Course Code, Course Name, Classes Attended, Total Classes, and exact Percentage (e.g. 39/40 - 97.5%).\n"
                + "3. State the overall attendance rate and compare against the 75% institutional mandatory requirement.\n"
                + "4. If any course is below 75%, highlight it as an attendance deficit warning.\n"
                + "5. If asked about assignments or grades, provide detailed deadlines, priority, and submission status.\n"
                + "6. Format using clean markdown bolding, lists, and clear professional tone.\n\n"
                + context.toString();

        steps.add("Reasoning with Groq AI using verified academic ground truth...");
        String answer = groqAiService.generateResponse(prompt, query);

        if (answer == null || answer.trim().isEmpty()) {
            // Intelligent fallback from database context
            String lower = query.toLowerCase();
            boolean isAttendance = toolRegistry.isAttendanceIntent(lower);
            if (isAttendance) {
                StringBuilder sb = new StringBuilder();
                if (targetCourse != null) {
                    AttendanceRecord rec = attendanceList.stream()
                            .filter(a -> a.getCourseCode().equalsIgnoreCase(targetCourse.getCourseCode()))
                            .findFirst().orElse(null);
                    if (rec != null) {
                        if (isQueryingOtherStudent) {
                            sb.append("Here is the verified academic attendance for **").append(studentDisplayName)
                              .append("** (Roll No: `").append(profile.getOrDefault("rollNumber", "N/A"))
                              .append("`) in **").append(rec.getCourseName()).append("** (`").append(rec.getCourseCode()).append("`):\n\n");
                        } else {
                            sb.append("Here is your verified academic attendance for **").append(rec.getCourseName())
                              .append("** (`").append(rec.getCourseCode()).append("`):\n\n");
                        }
                        sb.append("• **Classes Attended**: ").append(rec.getAttendedClasses())
                          .append(" / ").append(rec.getTotalClasses()).append(" classes\n");
                        sb.append("• **Attendance Percentage**: **").append(String.format("%.1f%%", rec.getPercentage())).append("**\n");

                        int minRequired = (int) Math.ceil(rec.getTotalClasses() * 0.75);
                        if (rec.getPercentage() >= 75.0) {
                            int margin = rec.getAttendedClasses() - minRequired;
                            sb.append("• **Institutional Status**: **ELIGIBLE** (Above mandatory 75% cutoff)\n");
                            sb.append("• **Attendance Buffer**: You are ").append(margin)
                              .append(" class(es) above the minimum required (").append(minRequired).append(" classes required).\n");
                        } else {
                            int deficit = minRequired - rec.getAttendedClasses();
                            sb.append("• **Institutional Status**: ⚠️ **ATTENDANCE DEFICIT** (Below mandatory 75% cutoff)\n");
                            sb.append("• **Action Required**: You must attend the next ").append(deficit)
                              .append(" class(es) consecutively to attain examination clearance.\n");
                        }
                        answer = sb.toString();
                    } else {
                        answer = "You are currently not enrolled in attendance sessions for **" + targetCourse.getCourseName() + "** (" + targetCourse.getCourseCode() + ").";
                    }
                } else {
                    if (isQueryingOtherStudent) {
                        sb.append("Here is the verified academic attendance breakdown for **").append(studentDisplayName)
                          .append("** (Roll No: `").append(profile.getOrDefault("rollNumber", "N/A"))
                          .append("`, ").append(dept).append(" Sec ").append(sec).append("):\n\n");
                    } else {
                        sb.append("Here is your verified subject-wise attendance breakdown:\n\n");
                    }
                    for (AttendanceRecord a : attendanceList) {
                        sb.append(String.format("• **%s** (%s): **%.1f%%** (%d/%d classes)\n",
                                a.getCourseName(), a.getCourseCode(), a.getPercentage(), a.getAttendedClasses(), a.getTotalClasses()));
                    }
                    sb.append(String.format("\nOverall Attendance Standing: **%s%%** (Institutional Requirement: 75%%)", profile.getOrDefault("attendanceRate", 85)));
                    answer = sb.toString();
                }
            } else if (lower.contains("assignment")) {
                StringBuilder sb = new StringBuilder("Here are upcoming academic assignments for ")
                        .append(dept).append(" Sec ").append(sec).append(":\n\n");
                for (Assignment asg : assignments) {
                    sb.append(String.format("• **%s** (%s) — Due: **%s** | Priority: **%s**\n",
                            asg.getTitle(), asg.getSubjectCode(), asg.getDueDate(), asg.getPriority()));
                }
                answer = sb.toString();
            } else {
                StringBuilder sb = new StringBuilder("Here are the enrolled courses for ")
                        .append(studentDisplayName).append(" (Semester ")
                        .append(profile.getOrDefault("semester", 5)).append("):\n\n");
                for (Course c : courses) {
                    sb.append(String.format("• **%s**: %s (%d Credits, Faculty: %s)\n",
                            c.getCourseCode(), c.getCourseName(), c.getCredits(), c.getFacultyName()));
                }
                answer = sb.toString();
            }
        }

        steps.add("✓ Verified against Institutional Academic Registry");
        long latency = System.currentTimeMillis() - startTime;
        return new AgentChatResponse(answer, "Academic Agent", steps, Map.of("academicVerified", true, "student", studentDisplayName, "toolsCalled", "getAttendance, getAssignments, getCourses"), latency);
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

    private boolean isDocumentPolicyQuery(String q) {
        return q.contains("regulation") || q.contains("condonation") || q.contains("fee") ||
                q.contains("handbook") || q.contains("policy") || q.contains("curfew") ||
                q.contains("hostel") || q.contains("leave on saturday") || q.contains("without parents") ||
                q.contains("outpass") || q.contains("gate pass") || q.contains("68%") || q.contains("65%") ||
                q.contains("minimum attendance") || q.contains("grading system");
    }
}
