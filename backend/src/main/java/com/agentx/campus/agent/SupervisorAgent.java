package com.agentx.campus.agent;

import com.agentx.campus.dto.AgentChatResponse;
import com.agentx.campus.model.AgentTaskLog;
import com.agentx.campus.model.AiConversation;
import com.agentx.campus.model.AiMessage;
import com.agentx.campus.model.User;
import com.agentx.campus.repository.AgentTaskLogRepository;
import com.agentx.campus.repository.AiConversationRepository;
import com.agentx.campus.repository.AiMessageRepository;
import com.agentx.campus.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SupervisorAgent {

    private final AcademicAgent academicAgent;
    private final DocumentRagAgent documentRagAgent;
    private final ScheduleAgent scheduleAgent;
    private final StudentSupportAgent studentSupportAgent;
    private final TaskPlanningAgent taskPlanningAgent;
    private final GrievanceAgent grievanceAgent;
    private final CampusResourceAgent campusResourceAgent;
    private final GeneralAiAgent generalAiAgent;
    private final ExaminationAgent examinationAgent;
    private final com.agentx.campus.service.AcademicEligibilityEngine academicEligibilityEngine;

    private final UserRepository userRepository;
    private final AgentTaskLogRepository taskLogRepository;
    private final AiConversationRepository conversationRepository;
    private final AiMessageRepository messageRepository;
    private final com.agentx.campus.service.GroqAiService groqAiService;
    private final com.agentx.campus.service.CampusToolRegistry toolRegistry;
    private final com.agentx.campus.service.AgentOrchestrator agentOrchestrator;

    public SupervisorAgent(
            AcademicAgent academicAgent,
            DocumentRagAgent documentRagAgent,
            ScheduleAgent scheduleAgent,
            StudentSupportAgent studentSupportAgent,
            TaskPlanningAgent taskPlanningAgent,
            GrievanceAgent grievanceAgent,
            CampusResourceAgent campusResourceAgent,
            GeneralAiAgent generalAiAgent,
            ExaminationAgent examinationAgent,
            com.agentx.campus.service.AcademicEligibilityEngine academicEligibilityEngine,
            UserRepository userRepository,
            AgentTaskLogRepository taskLogRepository,
            AiConversationRepository conversationRepository,
            AiMessageRepository messageRepository,
            com.agentx.campus.service.GroqAiService groqAiService,
            com.agentx.campus.service.CampusToolRegistry toolRegistry,
            @org.springframework.context.annotation.Lazy com.agentx.campus.service.AgentOrchestrator agentOrchestrator) {
        this.academicAgent = academicAgent;
        this.documentRagAgent = documentRagAgent;
        this.scheduleAgent = scheduleAgent;
        this.studentSupportAgent = studentSupportAgent;
        this.taskPlanningAgent = taskPlanningAgent;
        this.grievanceAgent = grievanceAgent;
        this.campusResourceAgent = campusResourceAgent;
        this.generalAiAgent = generalAiAgent;
        this.examinationAgent = examinationAgent;
        this.academicEligibilityEngine = academicEligibilityEngine;
        this.userRepository = userRepository;
        this.taskLogRepository = taskLogRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.groqAiService = groqAiService;
        this.toolRegistry = toolRegistry;
        this.agentOrchestrator = agentOrchestrator;
    }

    public AgentChatResponse routeAndExecute(String username, String query) {
        if (agentOrchestrator != null) {
            try {
                return agentOrchestrator.orchestrate(username, query);
            } catch (Exception ex) {
                // Fallback to internal supervisor routing if orchestrator fails
            }
        }

        long startTime = System.currentTimeMillis();
        String lower = query.toLowerCase().trim();
        AgentChatResponse response;
        String intent;
        String toolsUsed;
        int sourcesCount = 0;

        // Multi-Agent Intent Routing & Query Classifier
        if (isExamConflictQuery(lower)) {
            intent = "Examination Conflict & Scheduling";
            toolsUsed = "checkExamConflicts, getTimetableOccupancies, verifyRoomAvailability";
            response = examinationAgent.process(username, query);
        } else if (isEligibilityQuery(lower)) {
            intent = "Academic Eligibility & Rule Engine";
            toolsUsed = "getStudentProfile, getExamSchedule, getAttendanceRecord, getODRequests, searchRegulationsRAG, evaluateEligibilityRuleEngine";
            response = academicEligibilityEngine.evaluateStudentExamEligibility(username, query);
            sourcesCount = 1;
        } else if (isAttendanceExplainQuery(lower)) {
            intent = "Attendance Session Explainer";
            toolsUsed = "explainAttendanceEntry, getAttendance, findLeaveRequest";
            response = academicAgent.process(username, query);
        } else if (isGrievanceQuery(lower)) {
            intent = "Campus Grievance & Incident Dispatch";
            toolsUsed = "extractStructuredMetadata, createGrievance, notifyUser";
            response = grievanceAgent.process(username, query);
        } else if (isResourceQuery(lower)) {
            intent = "Campus Resource & Facility Availability";
            toolsUsed = "findAllResources, getTimetableOccupancies, verifyFacilitySchedule";
            response = campusResourceAgent.process(username, query);
        } else if (isDocumentRagQuery(lower)) {
            intent = "Institutional Policy & Regulations (RAG)";
            toolsUsed = "searchKnowledgeBase, semanticVectorRagRetrieval";
            response = documentRagAgent.process(username, query);
            sourcesCount = (response.getActionData() instanceof java.util.Map<?, ?> map && map.containsKey("sources")) ? 3 : 1;
        } else if (isPlanningQuery(lower)) {
            intent = "Task Planning";
            toolsUsed = "getAssignments, getCourses, createStudentTask";
            response = taskPlanningAgent.process(username, query);
        } else if (isAcademicRegistryQuery(lower)) {
            intent = "Academic Registry";
            toolsUsed = "getAttendance, getAssignments, getCourses, getStudentProfile";
            response = academicAgent.process(username, query);
        } else if (isScheduleQuery(lower)) {
            intent = "Academic Schedule & Events";
            toolsUsed = "getTodaySchedule, getWeeklySchedule, getUpcomingEvents";
            response = scheduleAgent.process(username, query);
        } else {
            // 2. Intelligent AI Intent Classification before fallback
            String aiCategory = classifyIntentWithAi(query);
            switch (aiCategory) {
                case "ACADEMIC":
                    intent = "Academic Registry (AI Routed)";
                    toolsUsed = "getAttendance, getAssignments, getCourses, getStudentProfile";
                    response = academicAgent.process(username, query);
                    break;
                case "SCHEDULE":
                    intent = "Academic Schedule & Events (AI Routed)";
                    toolsUsed = "getTodaySchedule, getWeeklySchedule, getUpcomingEvents";
                    response = scheduleAgent.process(username, query);
                    break;
                case "DOCUMENT_RAG":
                    intent = "Institutional Policy & Regulations (RAG - AI Routed)";
                    toolsUsed = "searchKnowledgeBase, semanticVectorRagRetrieval";
                    response = documentRagAgent.process(username, query);
                    sourcesCount = 3;
                    break;
                case "ELIGIBILITY":
                    intent = "Academic Eligibility & Rule Engine (AI Routed)";
                    toolsUsed = "getStudentProfile, getExamSchedule, getAttendanceRecord, evaluateEligibilityRuleEngine";
                    response = academicEligibilityEngine.evaluateStudentExamEligibility(username, query);
                    sourcesCount = 1;
                    break;
                case "EXAM_CONFLICT":
                    intent = "Examination Conflict & Scheduling (AI Routed)";
                    toolsUsed = "checkExamConflicts, getTimetableOccupancies";
                    response = examinationAgent.process(username, query);
                    break;
                case "GRIEVANCE":
                    intent = "Campus Grievance & Incident Dispatch (AI Routed)";
                    toolsUsed = "extractStructuredMetadata, createGrievance, notifyUser";
                    response = grievanceAgent.process(username, query);
                    break;
                case "RESOURCE":
                    intent = "Campus Resource & Facility Availability (AI Routed)";
                    toolsUsed = "findAllResources, getTimetableOccupancies";
                    response = campusResourceAgent.process(username, query);
                    break;
                case "TASK_PLANNING":
                    intent = "Task Planning (AI Routed)";
                    toolsUsed = "getAssignments, getCourses, createStudentTask";
                    response = taskPlanningAgent.process(username, query);
                    break;
                default:
                    // 3. Fallback Safeguard (Requirement 6): Check if query references specific campus data
                    if (hasSpecificCampusDataIntent(query)) {
                        intent = "Academic Registry (Campus Data Safeguard)";
                        toolsUsed = "findStudentInQuery, getAttendance, getStudentProfile";
                        response = academicAgent.process(username, query);
                    } else {
                        // Truly open-ended non-campus query
                        intent = "General AI Copilot";
                        toolsUsed = "generalAiReasoning";
                        response = generalAiAgent.process(username, query);
                    }
                    break;
            }
        }

        // Prepend Orchestrator classification step
        response.getSteps().add(0, "Orchestrator Agent classified query intent as [" + intent + "]");

        // Log agent execution audit in DB
        try {
            User user = userRepository.findByUsername(username).orElse(null);
            AgentTaskLog log = new AgentTaskLog();
            log.setUser(user);
            log.setAgentType(response.getAgentType());
            log.setSelectedAgents("Orchestrator Agent -> " + response.getAgentType());
            log.setToolsUsed(toolsUsed);
            log.setSourcesCount(sourcesCount);
            log.setUserQuery(query);
            log.setActionSummary(response.getMessage().length() > 250 ? response.getMessage().substring(0, 250) + "..." : response.getMessage());
            log.setExecutionStatus("SUCCESS");
            log.setLatencyMs(response.getLatencyMs());
            taskLogRepository.save(log);

            // Persist conversation and message in DB
            if (user != null) {
                List<AiConversation> convs = conversationRepository.findByUserOrderByUpdatedAtDesc(user);
                AiConversation conv;
                if (convs.isEmpty()) {
                    conv = new AiConversation(user, query.length() > 40 ? query.substring(0, 40) + "..." : query);
                    conv = conversationRepository.save(conv);
                } else {
                    conv = convs.get(0);
                    conv.setUpdatedAt(LocalDateTime.now());
                    conversationRepository.save(conv);
                }

                AiMessage userMsg = new AiMessage(conv, "USER", query, null, null, 1.0);
                AiMessage agentMsg = new AiMessage(conv, "AGENT", response.getMessage(), response.getAgentType(), toolsUsed, 0.98);
                messageRepository.save(userMsg);
                messageRepository.save(agentMsg);
            }
        } catch (Exception ex) {
            // Non-blocking log catch
        }

        return response;
    }

    private boolean isExamConflictQuery(String q) {
        return (q.contains("exam") || q.contains("examination") || q.contains("test")) &&
                (q.contains("conflict") || q.contains("schedule") || q.contains("clash") ||
                 q.contains("at 10") || q.contains("at 9") || q.contains("at 2") || q.contains("slot"));
    }

    private boolean isEligibilityQuery(String q) {
        return (q.contains("eligible") || q.contains("eligibility") || q.contains("can i write") ||
                q.contains("can i sit") || q.contains("admit card") || q.contains("hall ticket") ||
                q.contains("condonation fee") || q.contains("condonation") || q.contains("68%") ||
                (q.contains("exam") && (q.contains("tomorrow") || q.contains("write") || q.contains("sit"))));
    }

    private boolean isAttendanceExplainQuery(String q) {
        boolean hasWhy = q.contains("why") || q.contains("reason") || q.contains("explain") || q.contains("how come");
        boolean hasAttendance = q.contains("absent") || q.contains("marked") || q.contains("attendance entry") ||
                q.contains("attendance on") || q.contains("leave status") || q.contains("od status");
        return hasWhy && hasAttendance;
    }

    private boolean isPlanningQuery(String q) {
        return q.contains("plan") || q.contains("prepare") || q.contains("study") ||
                q.contains("help me with") || q.contains("schedule preparation") || q.contains("exam prep");
    }

    private boolean isGrievanceQuery(String q) {
        return q.contains("broken") || q.contains("not working") || q.contains("issue") ||
                q.contains("projector") || q.contains("ac ") || q.contains("a/c") ||
                q.contains("fan") || q.contains("complaint") || q.contains("repair") ||
                q.contains("leak") || q.contains("grievance") || q.contains("malfunction") ||
                q.contains("damaged") || q.contains("water pressure") || q.contains("report an issue");
    }

    private boolean isResourceQuery(String q) {
        return q.contains("room available") || q.contains("lab available") || q.contains("is room") ||
                q.contains("is lab") || q.contains("classroom status") || q.contains("facility available") ||
                q.contains("auditorium") || q.contains("seminar hall") || q.contains("room capacity") ||
                q.contains("free room") || q.contains("empty room") || q.contains("check room") ||
                q.contains("lab free") || q.contains("room free");
    }

    private boolean isSupportQuery(String q) {
        return q.contains("support") || q.contains("helpdesk") || q.contains("office contact") ||
                q.contains("facility") || q.contains("infrastructure help");
    }

    private boolean isDocumentRagQuery(String q) {
        return q.contains("regulation") || q.contains("rule") || q.contains("policy") ||
                q.contains("criteria") || q.contains("curfew") || q.contains("minimum attendance") ||
                q.contains("grading") || q.contains("credit") || q.contains("dress code") ||
                q.contains("hostel") || q.contains("cia evaluation") || q.contains("document") ||
                q.contains("guideline") || q.contains("handbook") ||
                q.contains("condonation") || q.contains("fee") || q.contains("68%") || q.contains("65%") ||
                q.contains("74%") || q.contains("75%") || q.contains("attendance requirement") ||
                q.contains("shortage") || q.contains("outpass") || q.contains("gate pass") ||
                q.contains("outing") || q.contains("parent") || q.contains("saturday") ||
                q.contains("can hosteller") || q.contains("hostellers") ||
                q.contains("without parents") || q.contains("placement policy") || q.contains("code of conduct");
    }

    private boolean isAcademicRegistryQuery(String q) {
        boolean attendanceIntent = toolRegistry != null && toolRegistry.isAttendanceIntent(q);
        boolean courseMentioned = toolRegistry != null && toolRegistry.findCourseInQuery(q) != null;
        boolean studentMentioned = toolRegistry != null && toolRegistry.findStudentInQuery(q) != null;

        if (attendanceIntent || studentMentioned) {
            return true;
        }

        if (courseMentioned && (attendanceIntent || q.contains("mark") || q.contains("grade") ||
                q.contains("score") || q.contains("class") || q.contains("how many") ||
                q.contains("wat") || q.contains("what") || q.contains("my") || q.contains("please"))) {
            return true;
        }

        return q.contains("attendance") || q.contains("course") || q.contains("subject") ||
                q.contains("assignment") || q.contains("mark") || q.contains("cgpa") ||
                q.contains("grade") || q.contains("profile") || q.contains("task") ||
                q.contains("enrollment") || q.contains("credits") || q.contains("syllabus") ||
                q.contains("internal mark") || q.contains("faculty mentor");
    }

    private boolean isScheduleQuery(String q) {
        return q.contains("today") || q.contains("tomorrow") || q.contains("class") ||
                q.contains("lecture") || q.contains("timetable") || q.contains("events") ||
                q.contains("hackathon") || q.contains("symposium") || q.contains("notice") ||
                q.contains("circular") || q.contains("when is");
    }

    private String classifyIntentWithAi(String query) {
        if (groqAiService == null) {
            return fallbackClassifier(query);
        }
        try {
            String classificationPrompt = "You are the AgentX Campus Multi-Agent Intent Classifier.\n"
                    + "Classify the user inquiry into EXACTLY ONE of these categories:\n"
                    + "- ACADEMIC: Student attendance, subject-wise attendance (e.g. 'Lavanya Sundar attendance', 'attendnance of Operating System', 'wat is my os attendance', 'OS attendance please', 'how many classes have I attended in Operating System'), grades, marks, CGPA, courses, assignments, student academic profile. Tolerate spelling mistakes (e.g. 'attendnance', 'atendance'), abbreviations (e.g. 'OS', 'DBMS', 'CN', 'AI', 'TOC'), and informal phrasing.\n"
                    + "- SCHEDULE: Daily class timetable, class periods, faculty teaching schedule, events, workshops, notices, hackathons.\n"
                    + "- DOCUMENT_RAG: Institutional regulations, academic handbook rules, hostel curfew, outpass/gate pass policy, condonation fee, credit requirements.\n"
                    + "- ELIGIBILITY: Exam write eligibility, hall ticket/admit card clearance, 68% attendance condonation rule.\n"
                    + "- EXAM_CONFLICT: Exam scheduling collisions, classroom exam conflicts, time slot clashes.\n"
                    + "- GRIEVANCE: Reporting broken facilities, projector/AC/fan malfunctions, water leaks, filing complaints.\n"
                    + "- RESOURCE: Classroom/laboratory availability, checking if a room or hall is empty/free.\n"
                    + "- TASK_PLANNING: Multi-step study plans, preparation roadmaps for subjects, auto-generating study tasks.\n"
                    + "- GENERAL: Open-ended non-campus inquiries, general coding, algorithms, greetings, math.\n\n"
                    + "Return ONLY the uppercase category word (e.g. ACADEMIC, SCHEDULE, DOCUMENT_RAG, etc.). Do not include explanations or markdown.";

            String res = groqAiService.generateResponse(classificationPrompt, query);
            if (res != null) {
                String clean = res.trim().toUpperCase();
                for (String cat : List.of("ACADEMIC", "SCHEDULE", "DOCUMENT_RAG", "ELIGIBILITY", "EXAM_CONFLICT", "GRIEVANCE", "RESOURCE", "TASK_PLANNING", "GENERAL")) {
                    if (clean.contains(cat)) {
                        return cat;
                    }
                }
            }
        } catch (Exception ex) {
            // Ignore failure, fall back to heuristic
        }
        return fallbackClassifier(query);
    }

    private String fallbackClassifier(String query) {
        if (query == null) return "GENERAL";
        String q = query.toLowerCase().trim();
        if (toolRegistry != null) {
            if (toolRegistry.isAttendanceIntent(q) || toolRegistry.findCourseInQuery(q) != null || toolRegistry.findStudentInQuery(q) != null) {
                return "ACADEMIC";
            }
        }
        if (isScheduleQuery(q)) return "SCHEDULE";
        if (isDocumentRagQuery(q)) return "DOCUMENT_RAG";
        if (isGrievanceQuery(q)) return "GRIEVANCE";
        if (isResourceQuery(q)) return "RESOURCE";
        if (isPlanningQuery(q)) return "TASK_PLANNING";
        if (isEligibilityQuery(q)) return "ELIGIBILITY";
        if (isExamConflictQuery(q)) return "EXAM_CONFLICT";
        return "GENERAL";
    }

    private boolean hasSpecificCampusDataIntent(String query) {
        if (query == null) return false;
        String q = query.toLowerCase();

        // 1. References a known student name or roll number in DB
        if (toolRegistry != null && toolRegistry.findStudentInQuery(query) != null) {
            return true;
        }

        // 2. References a course/subject in DB or acronym (e.g. Operating System, OS, DBMS)
        if (toolRegistry != null && toolRegistry.findCourseInQuery(query) != null) {
            return true;
        }

        // 3. Contains attendance intent (even with typos: "attendnance", "atendance")
        if (toolRegistry != null && toolRegistry.isAttendanceIntent(query)) {
            return true;
        }

        // 4. Contains standard university subject code pattern (e.g. CS301, CS302, IT202, EC...)
        if (query.matches(".*\\b[A-Za-z]{2,4}[0-9]{3}\\b.*")) {
            return true;
        }

        // 5. References section, attendance, or academic terms
        return q.contains("section a") || q.contains("section b") || q.contains("section c") ||
               q.contains("attendance") || q.contains("cgpa") || q.contains("marks") ||
               q.contains("timetable") || q.contains("syllabus") || q.contains("faculty mentor");
    }
}
