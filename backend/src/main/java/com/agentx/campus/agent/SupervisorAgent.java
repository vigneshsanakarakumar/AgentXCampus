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

    private final UserRepository userRepository;
    private final AgentTaskLogRepository taskLogRepository;
    private final AiConversationRepository conversationRepository;
    private final AiMessageRepository messageRepository;

    public SupervisorAgent(
            AcademicAgent academicAgent,
            DocumentRagAgent documentRagAgent,
            ScheduleAgent scheduleAgent,
            StudentSupportAgent studentSupportAgent,
            TaskPlanningAgent taskPlanningAgent,
            GrievanceAgent grievanceAgent,
            CampusResourceAgent campusResourceAgent,
            UserRepository userRepository,
            AgentTaskLogRepository taskLogRepository,
            AiConversationRepository conversationRepository,
            AiMessageRepository messageRepository) {
        this.academicAgent = academicAgent;
        this.documentRagAgent = documentRagAgent;
        this.scheduleAgent = scheduleAgent;
        this.studentSupportAgent = studentSupportAgent;
        this.taskPlanningAgent = taskPlanningAgent;
        this.grievanceAgent = grievanceAgent;
        this.campusResourceAgent = campusResourceAgent;
        this.userRepository = userRepository;
        this.taskLogRepository = taskLogRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    public AgentChatResponse routeAndExecute(String username, String query) {
        long startTime = System.currentTimeMillis();
        String lower = query.toLowerCase().trim();
        AgentChatResponse response;
        String intent;
        String toolsUsed;
        int sourcesCount = 0;

        // Multi-Agent Intent Routing
        if (isAttendanceExplainQuery(lower)) {
            intent = "Attendance Session Explainer";
            toolsUsed = "explainAttendanceEntry, getAttendance, findLeaveRequest";
            response = academicAgent.process(username, query);
        } else if (isPlanningQuery(lower)) {
            intent = "Task Planning";
            toolsUsed = "getAssignments, getCourses, createStudentTask";
            response = taskPlanningAgent.process(username, query);
        } else if (isGrievanceQuery(lower)) {
            intent = "Campus Grievance & Incident Dispatch";
            toolsUsed = "extractStructuredMetadata, createGrievance, notifyUser";
            response = grievanceAgent.process(username, query);
        } else if (isResourceQuery(lower)) {
            intent = "Campus Resource & Facility Availability";
            toolsUsed = "findAllResources, getTimetableOccupancies, verifyFacilitySchedule";
            response = campusResourceAgent.process(username, query);
        } else if (isSupportQuery(lower)) {
            intent = "Campus Operations & Support";
            toolsUsed = "getCampusResources, createGrievance";
            response = studentSupportAgent.process(username, query);
        } else if (isDocumentRagQuery(lower)) {
            intent = "Institutional Policy & Regulations (RAG)";
            toolsUsed = "searchKnowledgeBase, ragRetrieval";
            response = documentRagAgent.process(username, query);
            sourcesCount = (response.getActionData() instanceof java.util.Map<?, ?> map && map.containsKey("sources")) ? 3 : 1;
        } else if (isScheduleQuery(lower)) {
            intent = "Academic Schedule & Events";
            toolsUsed = "getTodaySchedule, getWeeklySchedule, getUpcomingEvents";
            response = scheduleAgent.process(username, query);
        } else {
            intent = "Academic Registry";
            toolsUsed = "getAttendance, getAssignments, getCourses, getStudentProfile";
            response = academicAgent.process(username, query);
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
                q.contains("guideline") || q.contains("handbook");
    }

    private boolean isScheduleQuery(String q) {
        return q.contains("today") || q.contains("tomorrow") || q.contains("class") ||
                q.contains("lecture") || q.contains("timetable") || q.contains("events") ||
                q.contains("hackathon") || q.contains("symposium") || q.contains("notice") ||
                q.contains("circular") || q.contains("when is");
    }
}
