package com.agentx.campus;

import com.agentx.campus.agent.NotificationAgent;
import com.agentx.campus.dto.LoginRequest;
import com.agentx.campus.dto.UserRegistrationDto;
import com.agentx.campus.exception.AccountPendingApprovalException;
import com.agentx.campus.model.*;
import com.agentx.campus.repository.*;
import com.agentx.campus.security.JwtTokenProvider;
import com.agentx.campus.service.AuthService;
import com.agentx.campus.service.HodService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserRegistrationApprovalTest {

    private AuthService authService;
    private HodService hodService;

    private UserRepository userRepository;
    private PasswordResetTokenRepository passwordResetTokenRepository;
    private StudentProfileRepository studentProfileRepository;
    private FacultyProfileRepository facultyProfileRepository;
    private StaffLoginRequestRepository staffLoginRequestRepository;
    private UserRegistrationRequestRepository userRegistrationRequestRepository;
    private HodProfileRepository hodProfileRepository;
    private NotificationAgent notificationAgent;
    private PasswordEncoder passwordEncoder;
    private JwtTokenProvider tokenProvider;
    private AuditLogRepository auditLogRepository;

    private User hodUser;
    private HodProfile hodProfile;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordResetTokenRepository = mock(PasswordResetTokenRepository.class);
        studentProfileRepository = mock(StudentProfileRepository.class);
        facultyProfileRepository = mock(FacultyProfileRepository.class);
        staffLoginRequestRepository = mock(StaffLoginRequestRepository.class);
        userRegistrationRequestRepository = mock(UserRegistrationRequestRepository.class);
        hodProfileRepository = mock(HodProfileRepository.class);
        notificationAgent = mock(NotificationAgent.class);
        passwordEncoder = mock(PasswordEncoder.class);
        tokenProvider = mock(JwtTokenProvider.class);
        auditLogRepository = mock(AuditLogRepository.class);

        when(passwordEncoder.encode(anyString())).thenAnswer(i -> "encoded_" + i.getArgument(0));
        when(passwordEncoder.matches(anyString(), anyString())).thenAnswer(i -> {
            String raw = i.getArgument(0);
            String enc = i.getArgument(1);
            return ("encoded_" + raw).equals(enc);
        });

        hodUser = new User("hod.cse", "hod.cse@campus.edu", "encoded_faculty123", Role.HOD, "Dr. Arulmozhi", "V");
        hodUser.setId(10L);
        hodUser.setActive(true);

        hodProfile = new HodProfile(hodUser, "Computer Science & Engineering", "Block A - Room 100", "+91 94432 10987");
        when(hodProfileRepository.findByDepartment("Computer Science & Engineering")).thenReturn(Optional.of(hodProfile));
        when(hodProfileRepository.findByUserUsername("hod.cse")).thenReturn(Optional.of(hodProfile));
        when(userRepository.findByUsername("hod.cse")).thenReturn(Optional.of(hodUser));

        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            if (u.getId() == null) u.setId(99L);
            return u;
        });

        when(userRegistrationRequestRepository.save(any(UserRegistrationRequest.class))).thenAnswer(i -> {
            UserRegistrationRequest r = i.getArgument(0);
            if (r.getId() == null) r.setId(101L);
            return r;
        });

        authService = new AuthService(
                userRepository,
                passwordResetTokenRepository,
                studentProfileRepository,
                facultyProfileRepository,
                staffLoginRequestRepository,
                userRegistrationRequestRepository,
                hodProfileRepository,
                notificationAgent,
                passwordEncoder,
                tokenProvider
        );

        hodService = new HodService(
                hodProfileRepository,
                facultyProfileRepository,
                mock(FacultyMentorSectionRepository.class),
                mock(FacultyLeaveRequestRepository.class),
                mock(TimetableEntryRepository.class),
                studentProfileRepository,
                mock(AnnouncementRepository.class),
                userRepository,
                mock(com.agentx.campus.agent.DocumentIngestionAgent.class),
                notificationAgent,
                userRegistrationRequestRepository,
                auditLogRepository
        );
    }

    @Test
    @DisplayName("Student registration: creates inactive user and pending request; login is blocked with pending approval")
    void testStudentRegistrationWorkflow() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setRole(Role.STUDENT);
        dto.setFirstName("Kavitha");
        dto.setLastName("M");
        dto.setUsername("kavitha.m");
        dto.setEmail("kavitha.m@campus.edu");
        dto.setPassword("pass123");
        dto.setConfirmPassword("pass123");
        dto.setDepartment("Computer Science & Engineering");
        dto.setRollNumber("717824P401");
        dto.setSection("A");
        dto.setYear(2);
        dto.setSemester(3);

        Map<String, Object> resp = authService.registerUser(dto);
        assertNotNull(resp);
        assertEquals("PENDING_APPROVAL", resp.get("status"));
        assertEquals("STUDENT", resp.get("role"));

        // Verify request was saved with PENDING
        verify(userRegistrationRequestRepository, times(1)).save(any(UserRegistrationRequest.class));
        verify(studentProfileRepository, times(1)).save(any(StudentProfile.class));

        // Simulate login attempt while pending
        User studentUser = new User("kavitha.m", "kavitha.m@campus.edu", "encoded_pass123", Role.STUDENT, "Kavitha", "M");
        studentUser.setActive(false);
        when(userRepository.findByUsernameOrEmail("kavitha.m", "kavitha.m")).thenReturn(Optional.of(studentUser));

        UserRegistrationRequest pendingReq = new UserRegistrationRequest();
        pendingReq.setUser(studentUser);
        pendingReq.setUsername("kavitha.m");
        pendingReq.setDepartment("Computer Science & Engineering");
        pendingReq.setRole(Role.STUDENT);
        pendingReq.setStatus("PENDING");
        when(userRegistrationRequestRepository.findByUser(studentUser)).thenReturn(Optional.of(pendingReq));

        LoginRequest loginReq = new LoginRequest();
        loginReq.setIdentifier("kavitha.m");
        loginReq.setPassword("pass123");

        AccountPendingApprovalException ex = assertThrows(AccountPendingApprovalException.class, () -> authService.login(loginReq));
        assertTrue(ex.getMessage().contains("pending verification and approval by your department HOD"));
    }

    @Test
    @DisplayName("Faculty registration (subject staff): creates inactive faculty profile; HOD can approve and activate account")
    void testFacultyRegistrationAndHodApproval() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setRole(Role.FACULTY);
        dto.setFirstName("Senthil");
        dto.setLastName("Kumar");
        dto.setUsername("senthil.k");
        dto.setEmail("senthil.k@campus.edu");
        dto.setPassword("faculty123");
        dto.setConfirmPassword("faculty123");
        dto.setDepartment("Computer Science & Engineering");
        dto.setDesignation("Assistant Professor (Subject Staff)");
        dto.setSpecialization("Database Systems & Cloud");
        dto.setCabinNumber("Block A - Cabin 306");

        Map<String, Object> resp = authService.registerUser(dto);
        assertNotNull(resp);
        assertEquals("PENDING_APPROVAL", resp.get("status"));

        verify(facultyProfileRepository, times(1)).save(any(FacultyProfile.class));

        // HOD approves the registration
        User facultyUser = new User("senthil.k", "senthil.k@campus.edu", "encoded_faculty123", Role.FACULTY, "Senthil", "Kumar");
        facultyUser.setActive(false);

        UserRegistrationRequest reg = new UserRegistrationRequest();
        reg.setId(205L);
        reg.setUser(facultyUser);
        reg.setUsername("senthil.k");
        reg.setDepartment("Computer Science & Engineering");
        reg.setRole(Role.FACULTY);
        reg.setStatus("PENDING");

        when(userRegistrationRequestRepository.findById(205L)).thenReturn(Optional.of(reg));

        UserRegistrationRequest approved = hodService.approveRegistration(205L, "hod.cse");
        assertEquals("APPROVED", approved.getStatus());
        assertTrue(facultyUser.isActive());
        verify(userRepository, atLeastOnce()).save(facultyUser);
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("HOD rejects registration: account remains inactive and rejection reason is logged")
    void testHodRejectRegistration() {
        User studentUser = new User("test.stu", "test.stu@campus.edu", "encoded_pass", Role.STUDENT, "Test", "Student");
        studentUser.setActive(false);

        UserRegistrationRequest reg = new UserRegistrationRequest();
        reg.setId(301L);
        reg.setUser(studentUser);
        reg.setUsername("test.stu");
        reg.setDepartment("Computer Science & Engineering");
        reg.setRole(Role.STUDENT);
        reg.setStatus("PENDING");

        when(userRegistrationRequestRepository.findById(301L)).thenReturn(Optional.of(reg));

        UserRegistrationRequest rejected = hodService.rejectRegistration(301L, "Invalid registration number", "hod.cse");
        assertEquals("REJECTED", rejected.getStatus());
        assertEquals("Invalid registration number", rejected.getRejectionReason());
        assertFalse(studentUser.isActive());
    }
}
