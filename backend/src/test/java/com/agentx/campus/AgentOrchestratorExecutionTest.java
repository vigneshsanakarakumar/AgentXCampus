package com.agentx.campus;

import com.agentx.campus.agent.*;
import com.agentx.campus.dto.AgentChatResponse;
import com.agentx.campus.dto.ExecutionPlanDto;
import com.agentx.campus.dto.ExecutionStepDto;
import com.agentx.campus.model.*;
import com.agentx.campus.repository.*;
import com.agentx.campus.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AgentOrchestratorExecutionTest {

    private AgentOrchestrator orchestrator;

    private UserRepository userRepository;
    private StudentProfileRepository studentProfileRepository;
    private AgentExecutionPlanRepository executionPlanRepository;
    private AgentExecutionStepRepository executionStepRepository;
    private AuditLogRepository auditLogRepository;

    private AttendanceAgent attendanceAgent;
    private LeaveODAgent leaveODAgent;
    private AcademicAgent academicAgent;
    private DocumentRagAgent documentRagAgent;
    private ScheduleAgent scheduleAgent;
    private ExaminationAgent examinationAgent;
    private GrievanceAgent grievanceAgent;
    private CampusResourceAgent campusResourceAgent;
    private OpportunityAgent opportunityAgent;
    private GeneralAiAgent generalAiAgent;
    private AttendanceRecoveryEngine recoveryEngine;
    private AcademicEligibilityEngine eligibilityEngine;
    private CampusToolRegistry toolRegistry;
    private GroqAiService groqAiService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        studentProfileRepository = mock(StudentProfileRepository.class);
        executionPlanRepository = mock(AgentExecutionPlanRepository.class);
        executionStepRepository = mock(AgentExecutionStepRepository.class);
        auditLogRepository = mock(AuditLogRepository.class);

        attendanceAgent = mock(AttendanceAgent.class);
        leaveODAgent = mock(LeaveODAgent.class);
        academicAgent = mock(AcademicAgent.class);
        documentRagAgent = mock(DocumentRagAgent.class);
        scheduleAgent = mock(ScheduleAgent.class);
        examinationAgent = mock(ExaminationAgent.class);
        grievanceAgent = mock(GrievanceAgent.class);
        campusResourceAgent = mock(CampusResourceAgent.class);
        opportunityAgent = mock(OpportunityAgent.class);
        generalAiAgent = mock(GeneralAiAgent.class);
        recoveryEngine = mock(AttendanceRecoveryEngine.class);
        eligibilityEngine = mock(AcademicEligibilityEngine.class);
        toolRegistry = mock(CampusToolRegistry.class);
        groqAiService = mock(GroqAiService.class);

        when(executionPlanRepository.save(any(AgentExecutionPlan.class))).thenAnswer(i -> i.getArgument(0));

        orchestrator = new AgentOrchestrator(
                userRepository,
                studentProfileRepository,
                executionPlanRepository,
                executionStepRepository,
                auditLogRepository,
                attendanceAgent,
                leaveODAgent,
                academicAgent,
                documentRagAgent,
                scheduleAgent,
                examinationAgent,
                grievanceAgent,
                campusResourceAgent,
                opportunityAgent,
                generalAiAgent,
                recoveryEngine,
                eligibilityEngine,
                toolRegistry,
                groqAiService
        );
    }

    @Test
    @DisplayName("DEMO SCENARIO 1: 'My attendance is 68%. Help me fix it.' triggers ATTENDANCE_RECOVERY plan")
    void testAttendanceRecoveryDemoScenario() {
        User student = new User("vasan", "pass", "vasan@campus.edu", Role.STUDENT);
        when(userRepository.findByUsername("vasan")).thenReturn(Optional.of(student));

        AgentChatResponse mockAgentResp = new AgentChatResponse(
                "You need to attend 12 consecutive classes.",
                "Attendance Agent",
                List.of("Attendance Agent: Analyzing attendance...", "Tool Call: calculateRequiredClasses()"),
                Map.of("requiredConsecutiveClasses", 12),
                50L
        );
        when(attendanceAgent.process(eq("vasan"), anyString())).thenReturn(mockAgentResp);

        AgentChatResponse result = orchestrator.orchestrate("vasan", "My attendance is 68%. Help me fix it.");

        assertNotNull(result);
        assertEquals("Attendance Agent", result.getAgentType());
        assertNotNull(result.getExecutionPlan());
        assertEquals("ATTENDANCE_RECOVERY", result.getExecutionPlan().getIntent());

        // Verify task decomposition steps
        List<ExecutionStepDto> steps = result.getExecutionPlan().getSteps();
        assertEquals(5, steps.size());
        assertEquals("ATTENDANCE_AGENT", steps.get(0).getAgent());
        assertEquals("GET_ATTENDANCE", steps.get(0).getTool());
        assertEquals("ACADEMIC_AGENT", steps.get(1).getAgent());
        assertEquals("GET_UPCOMING_CLASSES", steps.get(1).getTool());
        assertEquals("ATTENDANCE_AGENT", steps.get(2).getAgent());
        assertEquals("CALCULATE_RECOVERY", steps.get(2).getTool());
        assertEquals("CONFLICT_AGENT", steps.get(3).getAgent());
        assertEquals("CHECK_CONFLICTS", steps.get(3).getTool());
        assertEquals("VERIFICATION_AGENT", steps.get(4).getAgent());
        assertEquals("VERIFY_CALCULATIONS", steps.get(4).getTool());

        // Verify deterministic verification check
        assertNotNull(result.getVerification());
        assertTrue(result.getVerification().isVerified());
        assertEquals("VERIFIED", result.getVerification().getStatus());

        // Verify audit log creation
        verify(auditLogRepository, atLeastOnce()).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("DEMO SCENARIO 2: 'Show my leave history' triggers LEAVE_OD_HISTORY plan")
    void testLeaveHistoryDemoScenario() {
        User student = new User("vasan", "pass", "vasan@campus.edu", Role.STUDENT);
        when(userRepository.findByUsername("vasan")).thenReturn(Optional.of(student));

        AgentChatResponse mockLeaveResp = new AgentChatResponse(
                "Here is your leave history.",
                "Leave / OD Agent",
                List.of("Tool Call: getStudentLeaveHistory('vasan')"),
                Collections.emptyMap(),
                30L
        );
        when(leaveODAgent.process(eq("vasan"), anyString())).thenReturn(mockLeaveResp);

        AgentChatResponse result = orchestrator.orchestrate("vasan", "I took leave on September 10 and September 17. Show my leave history.");

        assertNotNull(result);
        assertEquals("LEAVE_OD_HISTORY", result.getExecutionPlan().getIntent());
        assertTrue(result.getVerification().isVerified());
    }

    @Test
    @DisplayName("DEMO SCENARIO 4: 'What is the minimum attendance requirement?' routes to INSTITUTIONAL_REGULATION_RAG")
    void testInstitutionalRegulationRagDemoScenario() {
        User student = new User("vasan", "pass", "vasan@campus.edu", Role.STUDENT);
        when(userRepository.findByUsername("vasan")).thenReturn(Optional.of(student));

        AgentChatResponse mockRagResp = new AgentChatResponse(
                "The minimum attendance requirement is 75%. Source: Academic Regulations 2026, Page 14",
                "Document RAG Agent",
                List.of("Tool Call: semanticVectorRagRetrieval()"),
                Map.of("sources", List.of("Autonomous Academic Regulations 2026, Section 1 (Page 14)")),
                45L
        );
        when(documentRagAgent.process(eq("vasan"), anyString())).thenReturn(mockRagResp);

        AgentChatResponse result = orchestrator.orchestrate("vasan", "What is the minimum attendance requirement?");

        assertNotNull(result);
        assertEquals("INSTITUTIONAL_REGULATION_RAG", result.getExecutionPlan().getIntent());
        assertTrue(result.getVerification().isVerified());
    }

    @Test
    @DisplayName("DEMO SCENARIO 5: 'Prepare me for my upcoming exam.' triggers EXAM_PREPARATION plan")
    void testExamPreparationDemoScenario() {
        User student = new User("vasan", "pass", "vasan@campus.edu", Role.STUDENT);
        when(userRepository.findByUsername("vasan")).thenReturn(Optional.of(student));
        when(toolRegistry.getStudentProfile("vasan")).thenReturn(Map.of("department", "CSE", "section", "C"));
        when(toolRegistry.getAttendance("vasan")).thenReturn(List.of(
                new AttendanceRecord(student, "CS301", "DBMS", 40, 36)
        ));

        AgentChatResponse result = orchestrator.orchestrate("vasan", "Prepare me for my upcoming exam.");

        assertNotNull(result);
        assertEquals("EXAM_PREPARATION", result.getExecutionPlan().getIntent());
        assertTrue(result.getAnswer().contains("Proactive Exam Preparation"));
        assertTrue(result.getVerification().isVerified());
    }
}
