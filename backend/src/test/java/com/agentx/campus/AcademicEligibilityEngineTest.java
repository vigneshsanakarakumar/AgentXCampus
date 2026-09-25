package com.agentx.campus;

import com.agentx.campus.dto.AgentChatResponse;
import com.agentx.campus.model.AttendanceRecord;
import com.agentx.campus.model.CampusDocument;
import com.agentx.campus.model.ExamSchedule;
import com.agentx.campus.model.Role;
import com.agentx.campus.model.StudentProfile;
import com.agentx.campus.model.User;
import com.agentx.campus.repository.*;
import com.agentx.campus.service.AcademicEligibilityEngine;
import com.agentx.campus.service.GroqAiService;
import com.agentx.campus.service.RagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class AcademicEligibilityEngineTest {

    private UserRepository userRepo;
    private StudentProfileRepository profileRepo;
    private AttendanceRecordRepository attendanceRepo;
    private ExamScheduleRepository examRepo;
    private ODRequestRepository odRepo;
    private LeaveRequestRepository leaveRepo;
    private RagService ragService;
    private GroqAiService groqAiService;
    private AcademicEligibilityEngine eligibilityEngine;

    private User student;
    private StudentProfile profile;
    private ExamSchedule exam;

    @BeforeEach
    void setUp() {
        userRepo = Mockito.mock(UserRepository.class);
        profileRepo = Mockito.mock(StudentProfileRepository.class);
        attendanceRepo = Mockito.mock(AttendanceRecordRepository.class);
        examRepo = Mockito.mock(ExamScheduleRepository.class);
        odRepo = Mockito.mock(ODRequestRepository.class);
        leaveRepo = Mockito.mock(LeaveRequestRepository.class);
        groqAiService = new GroqAiService("", "llama-3.3-70b-versatile", "https://api.groq.com", new com.fasterxml.jackson.databind.ObjectMapper());

        CampusDocumentRepository docRepo = Mockito.mock(CampusDocumentRepository.class);
        CampusDocument doc1 = new CampusDocument(
                "Autonomous Academic Regulations 2026",
                "REGULATION",
                "Regulations",
                "1. ATTENDANCE REQUIREMENTS & CONDONATION:\n" +
                "A candidate with not less than 75% is eligible. Shortage between 65% and 74% can be condoned on valid medical grounds with ₹750 fee within 3 working days. Candidates below 65% are strictly NOT permitted to write the exam.",
                "All", "PDF", "2026.1", "Dean"
        );
        when(docRepo.findByActiveTrueOrderByCreatedAtDesc()).thenReturn(List.of(doc1));
        ragService = new RagService(docRepo);
        ragService.rebuildVectorIndex();

        eligibilityEngine = new AcademicEligibilityEngine(
                userRepo, profileRepo, attendanceRepo, examRepo, odRepo, leaveRepo, ragService, groqAiService
        );

        student = new User();
        student.setId(1L);
        student.setUsername("alex.chen");
        student.setFirstName("Alex");
        student.setLastName("Chen");
        student.setRole(Role.STUDENT);

        profile = new StudentProfile();
        profile.setUser(student);
        profile.setDepartment("Computer Science & Engineering");
        profile.setSection("A");
        profile.setSemester(5);

        exam = new ExamSchedule();
        exam.setId(50L);
        exam.setSubjectCode("CS301");
        exam.setSubjectName("Database Management Systems");
        exam.setDepartment("Computer Science & Engineering");
        exam.setSection("A");
        exam.setExamDate(LocalDate.now().plusDays(1));
        exam.setStartTime("10:00 AM");
        exam.setEndTime("01:00 PM");
        exam.setRoom("Hall A-101");

        when(userRepo.findByUsername("alex.chen")).thenReturn(Optional.of(student));
        when(profileRepo.findByUser(student)).thenReturn(Optional.of(profile));
        when(examRepo.findByDepartmentAndSectionOrderByExamDateAscStartTimeAsc("Computer Science & Engineering", "A"))
                .thenReturn(List.of(exam));
        when(odRepo.findByStudentOrderByCreatedAtDesc(student)).thenReturn(Collections.emptyList());
        when(leaveRepo.findByStudentOrderByCreatedAtDesc(student)).thenReturn(Collections.emptyList());
    }

    @Test
    @DisplayName("Student with 85% attendance is DIRECTLY ELIGIBLE")
    void testDirectEligibility() {
        AttendanceRecord rec = new AttendanceRecord(student, "CS301", "Database Management Systems", 40, 34); // 85%
        when(attendanceRepo.findByUserOrderByCourseCodeAsc(student)).thenReturn(List.of(rec));

        AgentChatResponse res = eligibilityEngine.evaluateStudentExamEligibility("alex.chen", "Am I eligible for tomorrow's exam?");

        assertNotNull(res);
        assertTrue(res.getMessage().contains("fully eligible"), "Expected fully eligible status");
        @SuppressWarnings("unchecked")
        Map<String, Object> data1 = (Map<String, Object>) res.getActionData();
        assertEquals("ELIGIBLE (DIRECT)", data1.get("verdict"));
        assertTrue((Boolean) data1.get("isEligible"));
        assertFalse((Boolean) data1.get("requiresCondonation"));
    }

    @Test
    @DisplayName("Student with 68% attendance requires CONDONATION and ₹750 fee")
    void testCondonationRequired() {
        AttendanceRecord rec = new AttendanceRecord(student, "CS301", "Database Management Systems", 40, 27); // 67.5% -> 68%
        when(attendanceRepo.findByUserOrderByCourseCodeAsc(student)).thenReturn(List.of(rec));

        AgentChatResponse res = eligibilityEngine.evaluateStudentExamEligibility("alex.chen", "What is the condonation fee if my attendance is 68%?");

        assertNotNull(res);
        assertTrue(res.getMessage().contains("750"), "Must state ₹750 condonation fee");
        assertTrue(res.getMessage().contains("3 working days"), "Must state 3 working days deadline");
        @SuppressWarnings("unchecked")
        Map<String, Object> data2 = (Map<String, Object>) res.getActionData();
        assertEquals("CONDITIONAL (CONDONATION REQUIRED)", data2.get("verdict"));
        assertTrue((Boolean) data2.get("requiresCondonation"));
    }

    @Test
    @DisplayName("Student with 55% attendance is NOT ELIGIBLE / DETAINED")
    void testDetainedIneligible() {
        AttendanceRecord rec = new AttendanceRecord(student, "CS301", "Database Management Systems", 40, 22); // 55%
        when(attendanceRepo.findByUserOrderByCourseCodeAsc(student)).thenReturn(List.of(rec));

        AgentChatResponse res = eligibilityEngine.evaluateStudentExamEligibility("alex.chen", "Can I sit for CS301 exam?");

        assertNotNull(res);
        assertTrue(res.getMessage().contains("strictly NOT eligible") || res.getMessage().contains("Detention"),
                "Expected ineligible detention notice");
        @SuppressWarnings("unchecked")
        Map<String, Object> data3 = (Map<String, Object>) res.getActionData();
        assertEquals("NOT ELIGIBLE (DETAINED)", data3.get("verdict"));
        assertFalse((Boolean) data3.get("isEligible"));
    }
}
