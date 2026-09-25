package com.agentx.campus.service;

import com.agentx.campus.agent.NotificationAgent;
import com.agentx.campus.dto.*;
import com.agentx.campus.model.*;
import com.agentx.campus.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final FacultyProfileRepository facultyProfileRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final TimetableEntryRepository timetableEntryRepository;
    private final GrievanceRepository grievanceRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final AgentTaskLogRepository agentTaskLogRepository;
    private final CampusDocumentRepository campusDocumentRepository;
    private final AnnouncementRepository announcementRepository;
    private final CampusEventRepository campusEventRepository;
    private final CourseRepository courseRepository;
    private final AssignmentRepository assignmentRepository;
    private final FacultyMentorSectionRepository facultyMentorSectionRepository;
    private final StaffLoginRequestRepository staffLoginRequestRepository;
    private final PasswordEncoder passwordEncoder;
    private final ConflictEngine conflictEngine;
    private final NotificationAgent notificationAgent;
    private final ObjectMapper objectMapper;

    public AdminService(UserRepository userRepository,
                        FacultyProfileRepository facultyProfileRepository,
                        StudentProfileRepository studentProfileRepository,
                        TimetableEntryRepository timetableEntryRepository,
                        GrievanceRepository grievanceRepository,
                        ApprovalRequestRepository approvalRequestRepository,
                        AgentTaskLogRepository agentTaskLogRepository,
                        CampusDocumentRepository campusDocumentRepository,
                        AnnouncementRepository announcementRepository,
                        CampusEventRepository campusEventRepository,
                        CourseRepository courseRepository,
                        AssignmentRepository assignmentRepository,
                        FacultyMentorSectionRepository facultyMentorSectionRepository,
                        StaffLoginRequestRepository staffLoginRequestRepository,
                        PasswordEncoder passwordEncoder,
                        ConflictEngine conflictEngine,
                        NotificationAgent notificationAgent,
                        ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.facultyProfileRepository = facultyProfileRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.timetableEntryRepository = timetableEntryRepository;
        this.grievanceRepository = grievanceRepository;
        this.approvalRequestRepository = approvalRequestRepository;
        this.agentTaskLogRepository = agentTaskLogRepository;
        this.campusDocumentRepository = campusDocumentRepository;
        this.announcementRepository = announcementRepository;
        this.campusEventRepository = campusEventRepository;
        this.courseRepository = courseRepository;
        this.assignmentRepository = assignmentRepository;
        this.facultyMentorSectionRepository = facultyMentorSectionRepository;
        this.staffLoginRequestRepository = staffLoginRequestRepository;
        this.passwordEncoder = passwordEncoder;
        this.conflictEngine = conflictEngine;
        this.notificationAgent = notificationAgent;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> getAdminDashboard() {
        Map<String, Object> response = new HashMap<>();

        long userCount = userRepository.count();
        long openComplaints = grievanceRepository.countByStatus("OPEN");
        long pendingApprovals = approvalRequestRepository.countByStatus("PENDING");
        long agentTasksToday = agentTaskLogRepository.count();
        long facultyCount = facultyProfileRepository.count();
        long studentCount = studentProfileRepository.count();
        long timetablesCount = timetableEntryRepository.count();
        long activeCourses = courseRepository.count();
        long pendingAssignments = assignmentRepository.count();
        long upcomingEvents = campusEventRepository.count();
        long activeNotices = announcementRepository.count();
        long documentsCount = campusDocumentRepository.count();

        // Proactive conflicts
        List<Map<String, Object>> detectedConflicts = conflictEngine.scanConflicts();

        response.put("totalUsers", userCount);
        response.put("facultyCount", facultyCount);
        response.put("studentCount", studentCount);
        response.put("openComplaints", openComplaints);
        response.put("pendingApprovals", pendingApprovals);
        response.put("agentTasksToday", agentTasksToday);
        response.put("activeWorkflows", pendingApprovals);
        response.put("todaysClasses", timetablesCount);
        response.put("activeCourses", activeCourses);
        response.put("pendingAssignments", pendingAssignments);
        response.put("upcomingEvents", upcomingEvents);
        response.put("activeNotices", activeNotices);
        response.put("documentsCount", documentsCount);
        response.put("conflictsCount", detectedConflicts.size());
        response.put("conflicts", detectedConflicts);

        List<ApprovalRequest> approvals = approvalRequestRepository.findAllByOrderByRequestedAtDesc();
        response.put("approvals", approvals);

        List<Grievance> grievances = grievanceRepository.findAllByOrderByCreatedAtDesc();
        response.put("grievances", grievances);

        List<AgentTaskLog> agentLogs = agentTaskLogRepository.findTop10ByOrderByCreatedAtDesc();
        response.put("agentLogs", agentLogs);

        return response;
    }

    // --- NOTICE MANAGEMENT ---
    public List<Announcement> getAllNotices() {
        return announcementRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public Announcement createNotice(NoticeRequest req) {
        Announcement a = new Announcement();
        a.setTitle(req.getTitle());
        a.setContent(req.getContent());
        a.setPriority(req.getPriority() != null ? req.getPriority() : "NORMAL");
        a.setTargetAudience(req.getTargetAudience() != null ? req.getTargetAudience() : "ALL");
        a.setAuthorRole(req.getAuthorRole() != null ? req.getAuthorRole() : "ADMIN");
        a.setCreatedAt(LocalDateTime.now());
        return announcementRepository.save(a);
    }

    @Transactional
    public void deleteNotice(Long id) {
        announcementRepository.deleteById(id);
    }

    // --- EVENT MANAGEMENT ---
    public List<CampusEvent> getAllEvents() {
        return campusEventRepository.findAllByOrderByEventDateAsc();
    }

    @Transactional
    public CampusEvent createEvent(EventRequest req) {
        CampusEvent e = new CampusEvent();
        e.setTitle(req.getTitle());
        e.setCategory(req.getCategory() != null ? req.getCategory() : "EVENT");
        e.setDescription(req.getDescription());
        e.setLocation(req.getLocation());
        e.setEventDate(req.getEventDate());
        e.setEventTime(req.getEventTime());
        e.setOrganizer(req.getOrganizer() != null ? req.getOrganizer() : "Campus Affairs");
        e.setRegisteredCount(0);
        return campusEventRepository.save(e);
    }

    @Transactional
    public void deleteEvent(Long id) {
        campusEventRepository.deleteById(id);
    }

    // --- KNOWLEDGE BASE / RAG DOCUMENT MANAGEMENT ---
    public List<CampusDocument> getAllDocuments() {
        return campusDocumentRepository.findAll();
    }

    @Transactional
    public CampusDocument createDocument(com.agentx.campus.dto.DocumentRequest req, String uploadedBy) {
        CampusDocument doc = new CampusDocument(
                req.getTitle(),
                req.getCategory() != null ? req.getCategory() : "POLICY",
                req.getDescription(),
                req.getContent(),
                req.getDepartment() != null ? req.getDepartment() : "All Departments",
                req.getDocumentType() != null ? req.getDocumentType() : "PDF",
                req.getVersion() != null ? req.getVersion() : "1.0",
                uploadedBy
        );
        return campusDocumentRepository.save(doc);
    }

    @Transactional
    public void deleteDocument(Long id) {
        campusDocumentRepository.deleteById(id);
    }

    // --- ACADEMIC / COURSE & ASSIGNMENT MANAGEMENT ---
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public List<Assignment> getAllAssignments() {
        return assignmentRepository.findAllByOrderByDueDateAsc();
    }

    public List<AgentTaskLog> getAllAgentLogs() {
        return agentTaskLogRepository.findTop10ByOrderByCreatedAtDesc();
    }

    @Transactional
    public Map<String, Object> createFaculty(CreateFacultyRequest req) {
        if (req.getUsername() == null || req.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (req.getEmail() == null || req.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (req.getFirstName() == null || req.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required.");
        }
        if (req.getLastName() == null || req.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required.");
        }
        if (req.getDepartment() == null || req.getDepartment().trim().isEmpty()) {
            throw new IllegalArgumentException("Faculty department is required.");
        }

        String username = req.getUsername().trim().toLowerCase();
        String email = req.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username " + username + " is already taken.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email " + email + " is already registered.");
        }

        String employeeId = req.getEmployeeId() != null && !req.getEmployeeId().trim().isEmpty()
                ? req.getEmployeeId().trim().toUpperCase()
                : "EMP-" + (1000 + (int)(Math.random() * 9000));

        if (facultyProfileRepository.existsByEmployeeId(employeeId)) {
            throw new IllegalArgumentException("Employee ID " + employeeId + " already exists.");
        }

        String password = req.getPassword() != null && !req.getPassword().trim().isEmpty()
                ? req.getPassword()
                : "Faculty@123";

        User user = new User(
                username,
                email,
                passwordEncoder.encode(password),
                Role.FACULTY,
                req.getFirstName().trim(),
                req.getLastName().trim()
        );
        user = userRepository.save(user);

        FacultyProfile profile = new FacultyProfile();
        profile.setUser(user);
        profile.setEmployeeId(employeeId);
        profile.setDepartment(req.getDepartment().trim());
        profile.setDesignation(req.getDesignation() != null ? req.getDesignation().trim() : "Assistant Professor");
        profile.setAssignedDepartment(req.getAssignedDepartment() != null ? req.getAssignedDepartment().trim() : req.getDepartment().trim());
        profile.setAssignedSection(req.getAssignedSection() != null ? req.getAssignedSection().trim().toUpperCase() : null);
        profile.setMentor(true);
        profile.setCabinNumber("Block-B Cabin " + (100 + (int)(Math.random() * 900)));
        profile = facultyProfileRepository.save(profile);

        Map<String, Object> res = new HashMap<>();
        res.put("id", profile.getId());
        res.put("userId", user.getId());
        res.put("username", user.getUsername());
        res.put("name", user.getFirstName() + " " + user.getLastName());
        res.put("email", user.getEmail());
        res.put("employeeId", profile.getEmployeeId());
        res.put("department", profile.getDepartment());
        res.put("designation", profile.getDesignation());
        res.put("assignedDepartment", profile.getAssignedDepartment());
        res.put("assignedSection", profile.getAssignedSection());
        return res;
    }

    public List<Map<String, Object>> getAllFaculty() {
        List<FacultyProfile> list = facultyProfileRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (FacultyProfile fp : list) {
            User u = fp.getUser();
            Map<String, Object> map = new HashMap<>();
            map.put("id", fp.getId());
            map.put("userId", u != null ? u.getId() : null);
            map.put("username", u != null ? u.getUsername() : "");
            map.put("name", u != null ? u.getFirstName() + " " + u.getLastName() : "Faculty");
            map.put("email", u != null ? u.getEmail() : "");
            map.put("employeeId", fp.getEmployeeId());
            map.put("department", fp.getDepartment());
            map.put("designation", fp.getDesignation());
            map.put("assignedDepartment", fp.getAssignedDepartment());
            map.put("assignedSection", fp.getAssignedSection());
            map.put("cabinNumber", fp.getCabinNumber());
            map.put("isMentor", fp.isMentor());
            map.put("mentorSections", facultyMentorSectionRepository.findByFacultyProfile(fp));
            result.add(map);
        }
        return result;
    }

    @Transactional
    public Map<String, Object> assignMentor(MentorAssignmentRequest req) {
        if (req.getFacultyId() == null) {
            throw new IllegalArgumentException("Faculty ID is required.");
        }
        if (req.getAssignedDepartment() == null || req.getAssignedDepartment().trim().isEmpty()) {
            throw new IllegalArgumentException("Assigned department is required.");
        }
        if (req.getAssignedSection() == null || req.getAssignedSection().trim().isEmpty()) {
            throw new IllegalArgumentException("Assigned section is required.");
        }

        Optional<FacultyProfile> opt = facultyProfileRepository.findById(req.getFacultyId());
        if (opt.isEmpty()) {
            User user = userRepository.findById(req.getFacultyId()).orElse(null);
            if (user != null) {
                opt = facultyProfileRepository.findByUser(user);
            }
        }

        FacultyProfile profile = opt.orElseThrow(() -> new IllegalArgumentException("Faculty record not found with ID: " + req.getFacultyId()));
        profile.setAssignedDepartment(req.getAssignedDepartment().trim());
        profile.setAssignedSection(req.getAssignedSection().trim().toUpperCase());
        profile.setMentor(true);
        facultyProfileRepository.save(profile);

        User u = profile.getUser();
        Map<String, Object> res = new HashMap<>();
        res.put("id", profile.getId());
        res.put("facultyName", u != null ? u.getFirstName() + " " + u.getLastName() : "Faculty");
        res.put("assignedDepartment", profile.getAssignedDepartment());
        res.put("assignedSection", profile.getAssignedSection());
        return res;
    }

    public List<Map<String, Object>> getAllStudents(String department, String section) {
        List<StudentProfile> students;
        if (department != null && !department.trim().isEmpty() && section != null && !section.trim().isEmpty()) {
            students = studentProfileRepository.findByDepartmentAndSection(department.trim(), section.trim().toUpperCase());
        } else if (department != null && !department.trim().isEmpty()) {
            students = studentProfileRepository.findByDepartment(department.trim());
        } else {
            students = studentProfileRepository.findAll();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (StudentProfile sp : students) {
            User u = sp.getUser();
            Map<String, Object> map = new HashMap<>();
            map.put("id", sp.getId());
            map.put("userId", u != null ? u.getId() : null);
            map.put("name", u != null ? u.getFirstName() + " " + u.getLastName() : "Student");
            map.put("username", u != null ? u.getUsername() : "");
            map.put("email", u != null ? u.getEmail() : "");
            map.put("rollNumber", sp.getRollNumber());
            map.put("department", sp.getDepartment());
            map.put("section", sp.getSection());
            map.put("cgpa", sp.getCgpa());
            map.put("attendance", sp.getAttendanceRate());
            map.put("semester", sp.getSemester());
            result.add(map);
        }
        return result;
    }

    public List<TimetableEntry> getTimetables(String department, String section) {
        if (department != null && !department.trim().isEmpty() && section != null && !section.trim().isEmpty()) {
            return timetableEntryRepository.findByDepartmentAndSectionOrderByDayOfWeekAscStartTimeAsc(
                    department.trim(), section.trim().toUpperCase()
            );
        }
        return timetableEntryRepository.findAllByOrderByDepartmentAscSectionAscDayOfWeekAscStartTimeAsc();
    }

    @Transactional
    public TimetableEntry createTimetable(TimetableRequest req) {
        if (req.getDepartment() == null || req.getDepartment().trim().isEmpty()) {
            throw new IllegalArgumentException("Department is required.");
        }
        if (req.getSection() == null || req.getSection().trim().isEmpty()) {
            throw new IllegalArgumentException("Section is required.");
        }
        if (req.getSubjectCode() == null || req.getSubjectCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Subject code is required.");
        }
        if (req.getSubjectName() == null || req.getSubjectName().trim().isEmpty()) {
            throw new IllegalArgumentException("Subject name is required.");
        }

        TimetableEntry entry = new TimetableEntry();
        entry.setDepartment(req.getDepartment().trim());
        entry.setSection(req.getSection().trim().toUpperCase());
        entry.setSemester(req.getSemester() > 0 ? req.getSemester() : 1);
        entry.setSubjectCode(req.getSubjectCode().trim().toUpperCase());
        entry.setSubjectName(req.getSubjectName().trim());
        entry.setFacultyName(req.getFacultyName() != null ? req.getFacultyName().trim() : "Faculty");
        entry.setDayOfWeek(req.getDayOfWeek() != null ? req.getDayOfWeek().trim() : "Monday");
        entry.setStartTime(req.getStartTime() != null ? req.getStartTime().trim() : "09:00 AM");
        entry.setEndTime(req.getEndTime() != null ? req.getEndTime().trim() : "10:00 AM");
        entry.setClassroom(req.getClassroom() != null ? req.getClassroom().trim() : "Room 101");

        entry = timetableEntryRepository.save(entry);

        // Proactively scan for any new conflicts
        conflictEngine.generateApprovalRequestsForConflicts();

        return entry;
    }

    @Transactional
    public void deleteTimetable(Long id) {
        if (!timetableEntryRepository.existsById(id)) {
            throw new IllegalArgumentException("Timetable entry not found: " + id);
        }
        timetableEntryRepository.deleteById(id);
    }

    public List<Map<String, Object>> scanConflicts() {
        return conflictEngine.scanConflicts();
    }

    @Transactional
    public List<ApprovalRequest> triggerConflictScanAndApprovalGeneration() {
        return conflictEngine.generateApprovalRequestsForConflicts();
    }

    @Transactional
    public ApprovalRequest decideApproval(Long id, String decision, String decidedBy) {
        ApprovalRequest req = approvalRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Approval request not found"));

        boolean isApproved = "APPROVED".equalsIgnoreCase(decision);
        req.setStatus(isApproved ? "APPROVED" : "REJECTED");
        req.setDecidedAt(LocalDateTime.now());
        req.setDecidedBy(decidedBy);

        // Automated execution of approved mutation
        if (isApproved && "TIMETABLE_RESCHEDULE".equalsIgnoreCase(req.getActionType()) && req.getTargetEntityId() != null) {
            Optional<TimetableEntry> ttOpt = timetableEntryRepository.findById(req.getTargetEntityId());
            if (ttOpt.isPresent()) {
                TimetableEntry tt = ttOpt.get();
                try {
                    if (req.getMutationPayload() != null) {
                        JsonNode payload = objectMapper.readTree(req.getMutationPayload());
                        if (payload.has("newStartTime")) {
                            tt.setStartTime(payload.get("newStartTime").asText());
                        }
                        if (payload.has("newEndTime")) {
                            tt.setEndTime(payload.get("newEndTime").asText());
                        }
                        timetableEntryRepository.save(tt);

                        // Notify Section students of timetable change
                        notificationAgent.notifySection(
                                tt.getDepartment(),
                                tt.getSection(),
                                "Timetable Updated: " + tt.getSubjectName(),
                                "Your " + tt.getSubjectCode() + " class has been rescheduled to " + tt.getStartTime() + " (" + tt.getClassroom() + ").",
                                "TIMETABLE_UPDATE"
                        );
                    }
                } catch (Exception ex) {
                    // Fallback move to 11:00 AM
                    tt.setStartTime("11:00 AM");
                    tt.setEndTime("12:30 PM");
                    timetableEntryRepository.save(tt);
                    notificationAgent.notifySection(
                            tt.getDepartment(),
                            tt.getSection(),
                            "Timetable Updated: " + tt.getSubjectName(),
                            "Your " + tt.getSubjectCode() + " class has been rescheduled to 11:00 AM (" + tt.getClassroom() + ").",
                            "TIMETABLE_UPDATE"
                    );
                }
            }
        }

        return approvalRequestRepository.save(req);
    }

    // --- STAFF LOGIN REQUESTS & INVITATION SYSTEM ---
    public List<StaffLoginRequest> getStaffRequests(String status) {
        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status.trim())) {
            return staffLoginRequestRepository.findByStatusOrderByCreatedAtDesc(status.trim().toUpperCase());
        }
        return staffLoginRequestRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public Map<String, Object> approveStaffRequest(Long requestId) {
        StaffLoginRequest req = staffLoginRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Staff request not found with ID: " + requestId));

        if ("ACTIVATED".equalsIgnoreCase(req.getStatus())) {
            throw new IllegalArgumentException("This staff member has already activated their account.");
        }

        String token = UUID.randomUUID().toString();
        req.setInviteToken(token);
        req.setTokenExpiry(LocalDateTime.now().plusDays(7));
        req.setStatus("APPROVED");
        req.setApprovedAt(LocalDateTime.now());
        req.setUpdatedAt(LocalDateTime.now());
        staffLoginRequestRepository.save(req);

        // Pre-create unactivated user if not existing
        if (!userRepository.existsByEmail(req.getEmail())) {
            String tempUsername = req.getEmail().split("@")[0].toLowerCase().replaceAll("[^a-z0-9]", "");
            if (userRepository.existsByUsername(tempUsername)) {
                tempUsername = tempUsername + (100 + (int)(Math.random() * 900));
            }
            User inactiveUser = new User(
                    tempUsername,
                    req.getEmail(),
                    passwordEncoder.encode(UUID.randomUUID().toString()),
                    Role.FACULTY,
                    req.getFirstName(),
                    req.getLastName()
            );
            inactiveUser.setActive(false);
            userRepository.save(inactiveUser);
        }

        String inviteUrl = "http://localhost:5173/activate-staff?token=" + token;

        return Map.of(
                "message", "Staff request approved. Invitation link generated.",
                "requestId", req.getId(),
                "email", req.getEmail(),
                "status", "APPROVED",
                "inviteToken", token,
                "inviteUrl", inviteUrl,
                "tokenExpiry", req.getTokenExpiry().toString()
        );
    }

    @Transactional
    public StaffLoginRequest rejectStaffRequest(Long requestId, String reason) {
        StaffLoginRequest req = staffLoginRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Staff request not found with ID: " + requestId));

        req.setStatus("REJECTED");
        req.setRejectionReason(reason != null && !reason.trim().isEmpty() ? reason.trim() : "Eligibility requirements not met.");
        req.setUpdatedAt(LocalDateTime.now());
        return staffLoginRequestRepository.save(req);
    }

    // --- MENTOR TO SECTION MAPPINGS ---
    @Transactional
    public FacultyMentorSection assignMentorSection(Long facultyProfileId, MentorSectionRequestDto dto) {
        if (dto.getDepartment() == null || dto.getDepartment().trim().isEmpty()) {
            throw new IllegalArgumentException("Department is required.");
        }
        if (dto.getSection() == null || dto.getSection().trim().isEmpty()) {
            throw new IllegalArgumentException("Section is required.");
        }
        if (dto.getSemester() == null) {
            throw new IllegalArgumentException("Semester is required.");
        }

        FacultyProfile profile = facultyProfileRepository.findById(facultyProfileId)
                .orElseThrow(() -> new IllegalArgumentException("Faculty profile not found with ID: " + facultyProfileId));

        String dept = dto.getDepartment().trim();
        String sec = dto.getSection().trim().toUpperCase();
        Integer sem = dto.getSemester();
        String year = dto.getAcademicYear() != null ? dto.getAcademicYear().trim() : "2025-2026";

        if (facultyMentorSectionRepository.existsByFacultyProfileAndDepartmentAndSectionAndSemester(profile, dept, sec, sem)) {
            throw new IllegalArgumentException("Faculty is already assigned as mentor for " + dept + " Section " + sec + " (Sem " + sem + ").");
        }

        FacultyMentorSection mapping = new FacultyMentorSection(profile, dept, sec, sem, year);
        profile.setMentor(true);
        profile.setAssignedDepartment(dept);
        profile.setAssignedSection(sec);
        facultyProfileRepository.save(profile);

        return facultyMentorSectionRepository.save(mapping);
    }

    @Transactional
    public void removeMentorSection(Long mappingId) {
        facultyMentorSectionRepository.deleteById(mappingId);
    }

    public List<FacultyMentorSection> getFacultyMentorSections(Long facultyProfileId) {
        FacultyProfile profile = facultyProfileRepository.findById(facultyProfileId)
                .orElseThrow(() -> new IllegalArgumentException("Faculty profile not found with ID: " + facultyProfileId));
        return facultyMentorSectionRepository.findByFacultyProfile(profile);
    }

    // --- COMPLAINTS & GRIEVANCE MANAGEMENT ---

    public List<Grievance> getAdminGrievances(String filerRole, String status) {
        if (filerRole != null && !filerRole.trim().isEmpty()) {
            try {
                Role role = Role.valueOf(filerRole.trim().toUpperCase());
                if (status != null && !status.trim().isEmpty()) {
                    return grievanceRepository.findByUser_RoleAndStatusOrderByCreatedAtDesc(role, status.trim().toUpperCase());
                }
                return grievanceRepository.findByUser_RoleOrderByCreatedAtDesc(role);
            } catch (IllegalArgumentException ignored) {}
        }

        if (status != null && !status.trim().isEmpty()) {
            return grievanceRepository.findByStatusOrderByCreatedAtDesc(status.trim().toUpperCase());
        }

        return grievanceRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public Grievance updateGrievanceStatus(Long grievanceId, String newStatus, String resolutionNotes, String adminUsername) {
        Grievance g = grievanceRepository.findById(grievanceId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found with ID: " + grievanceId));

        if (newStatus != null && !newStatus.trim().isEmpty()) {
            String upper = newStatus.trim().toUpperCase();
            g.setStatus(upper);
            if ("RESOLVED".equals(upper)) {
                g.setResolvedAt(LocalDateTime.now());
                if (g.getUser() != null) {
                    try {
                        String notes = (resolutionNotes != null && !resolutionNotes.trim().isEmpty())
                                ? " Remarks: " + resolutionNotes.trim() : "";
                        notificationAgent.notifyUser(
                                g.getUser(),
                                "Complaint #" + g.getTicketNumber() + " Resolved",
                                "Your complaint regarding '" + (g.getDescription() != null ? g.getDescription() : "your incident")
                                        + "' has been marked RESOLVED by Campus Administration." + notes,
                                "GRIEVANCE_RESOLVED"
                        );
                    } catch (Exception ignored) {}
                }
            }
        }

        if (resolutionNotes != null && !resolutionNotes.trim().isEmpty()) {
            g.setResolutionNotes(resolutionNotes.trim());
        }

        return grievanceRepository.save(g);
    }
}
