package com.agentx.campus;

import com.agentx.campus.agent.NotificationAgent;
import com.agentx.campus.model.*;
import com.agentx.campus.repository.*;
import com.agentx.campus.service.AttendanceRecoveryEngine;
import com.agentx.campus.service.ConflictEngine;
import com.agentx.campus.service.RequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AttendanceRecoveryAndLeaveWorkflowTest {

    private AttendanceRecoveryEngine recoveryEngine;
    private RequestService requestService;

    private UserRepository userRepository;
    private StudentProfileRepository studentProfileRepository;
    private FacultyMentorSectionRepository mentorSectionRepository;
    private LeaveRequestRepository leaveRepo;
    private ODRequestRepository odRepo;
    private DocumentRequestRepository docRepo;
    private AttendanceSessionRepository sessionRepo;
    private AttendanceEntryRepository entryRepo;
    private NotificationAgent notificationAgent;
    private AuditLogRepository auditLogRepository;
    private AttendanceRecordRepository attendanceRecordRepository;
    private TimetableEntryRepository timetableEntryRepository;
    private ConflictEngine conflictEngine;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        studentProfileRepository = mock(StudentProfileRepository.class);
        mentorSectionRepository = mock(FacultyMentorSectionRepository.class);
        leaveRepo = mock(LeaveRequestRepository.class);
        odRepo = mock(ODRequestRepository.class);
        docRepo = mock(DocumentRequestRepository.class);
        sessionRepo = mock(AttendanceSessionRepository.class);
        entryRepo = mock(AttendanceEntryRepository.class);
        NotificationRepository notifRepo = mock(NotificationRepository.class);
        com.agentx.campus.service.NotificationStreamService notifStream = mock(com.agentx.campus.service.NotificationStreamService.class);
        when(notifRepo.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));
        notificationAgent = new NotificationAgent(notifRepo, studentProfileRepository, notifStream);
        auditLogRepository = mock(AuditLogRepository.class);
        attendanceRecordRepository = mock(AttendanceRecordRepository.class);
        timetableEntryRepository = mock(TimetableEntryRepository.class);
        conflictEngine = mock(ConflictEngine.class);

        recoveryEngine = new AttendanceRecoveryEngine(
                attendanceRecordRepository,
                timetableEntryRepository,
                studentProfileRepository,
                userRepository,
                conflictEngine
        );

        requestService = new RequestService(
                userRepository,
                studentProfileRepository,
                mentorSectionRepository,
                leaveRepo,
                odRepo,
                docRepo,
                sessionRepo,
                entryRepo,
                notificationAgent,
                auditLogRepository
        );
    }

    @Test
    @DisplayName("Deterministic Recovery Math: 67.5% (27/40) requires exactly 12 consecutive classes to reach 75%")
    void testAttendanceRecoveryCalculation() {
        int attended = 27;
        int total = 40;
        int needed = recoveryEngine.calculateRequiredClasses(attended, total, 75.0);

        // x = 3T - 4A = 3(40) - 4(27) = 120 - 108 = 12
        assertEquals(12, needed);

        // Verify mathematically
        int newAttended = attended + needed;
        int newTotal = total + needed;
        double newPercentage = (newAttended * 100.0) / newTotal;

        assertEquals(39, newAttended);
        assertEquals(52, newTotal);
        assertEquals(75.0, newPercentage, 0.001);
    }

    @Test
    @DisplayName("Recovery Math: Attendance >= 75% requires 0 classes")
    void testAttendanceRecoveryZeroWhenEligible() {
        assertEquals(0, recoveryEngine.calculateRequiredClasses(30, 40, 75.0)); // exactly 75%
        assertEquals(0, recoveryEngine.calculateRequiredClasses(35, 40, 75.0)); // 87.5%
    }

    @Test
    @DisplayName("Leave Request Validation: toDate before fromDate throws IllegalArgumentException")
    void testLeaveDateValidationRejectsInvalidRange() {
        User student = new User("vasan", "pass", "vasan@test.edu", Role.STUDENT);
        when(userRepository.findByUsername("vasan")).thenReturn(Optional.of(student));

        Map<String, Object> body = Map.of(
                "fromDate", "2026-09-25",
                "toDate", "2026-09-20", // earlier than fromDate
                "reason", "Medical appointment",
                "leaveType", "MEDICAL"
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            requestService.submitLeaveRequest("vasan", body);
        });

        assertTrue(ex.getMessage().contains("\"To Date\" cannot be earlier than \"From Date\"."));
        verify(leaveRepo, never()).save(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("Leave Request: Overlapping leave request throws IllegalArgumentException")
    void testLeaveOverlappingPrevention() {
        User student = new User("vasan", "pass", "vasan@test.edu", Role.STUDENT);
        when(userRepository.findByUsername("vasan")).thenReturn(Optional.of(student));

        LeaveRequest existing = new LeaveRequest();
        existing.setStudent(student);
        existing.setFromDate(LocalDate.of(2026, 9, 10));
        existing.setToDate(LocalDate.of(2026, 9, 15));
        existing.setStatus("APPROVED");

        when(leaveRepo.findByStudentOrderByCreatedAtDesc(student)).thenReturn(List.of(existing));

        Map<String, Object> body = Map.of(
                "fromDate", "2026-09-12",
                "toDate", "2026-09-18",
                "reason", "Fever",
                "leaveType", "MEDICAL"
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            requestService.submitLeaveRequest("vasan", body);
        });

        assertTrue(ex.getMessage().contains("already covers these dates"));
    }

    @Test
    @DisplayName("Leave Approval Idempotency: Duplicate approval does not re-process")
    void testLeaveApprovalIdempotency() {
        User mentor = new User("ramesh.k", "pass", "ramesh@test.edu", Role.FACULTY);
        mentor.setId(10L);
        when(userRepository.findByUsername("ramesh.k")).thenReturn(Optional.of(mentor));

        LeaveRequest req = new LeaveRequest();
        req.setId(1L);
        req.setStatus("APPROVED");
        req.setAssignedToUser(mentor);

        when(leaveRepo.findById(1L)).thenReturn(Optional.of(req));

        LeaveRequest result = requestService.updateLeaveStatus(1L, "APPROVED", "Already approved", "ramesh.k");

        assertEquals("APPROVED", result.getStatus());
        // Verify no duplicate notification or double save
        verify(leaveRepo, never()).save(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("Leave Rejection: Rejection does not cascade to attendance entries")
    void testLeaveRejectionDoesNotModifyAttendance() {
        User mentor = new User("ramesh.k", "pass", "ramesh@test.edu", Role.FACULTY);
        mentor.setId(10L);
        when(userRepository.findByUsername("ramesh.k")).thenReturn(Optional.of(mentor));

        LeaveRequest req = new LeaveRequest();
        req.setId(2L);
        req.setStatus("PENDING");
        req.setAssignedToUser(mentor);
        req.setFromDate(LocalDate.of(2026, 9, 20));
        req.setToDate(LocalDate.of(2026, 9, 21));

        when(leaveRepo.findById(2L)).thenReturn(Optional.of(req));
        when(leaveRepo.save(any(LeaveRequest.class))).thenAnswer(i -> i.getArgument(0));

        LeaveRequest result = requestService.updateLeaveStatus(2L, "REJECTED", "Insufficient notice", "ramesh.k");

        assertEquals("REJECTED", result.getStatus());
        // Verify attendance entries were NOT modified to LEAVE
        verify(entryRepo, never()).save(any(AttendanceEntry.class));
    }

    @Test
    @DisplayName("LeaveRequest Model: getTotalDays and getLeaveDates compute accurate calendars")
    void testLeaveRequestDateCalculations() {
        LeaveRequest req = new LeaveRequest();
        req.setFromDate(LocalDate.of(2026, 9, 10));
        req.setToDate(LocalDate.of(2026, 9, 12));

        assertEquals(3, req.getTotalDays());
        assertEquals(List.of("2026-09-10", "2026-09-11", "2026-09-12"), req.getLeaveDates());
    }
}
