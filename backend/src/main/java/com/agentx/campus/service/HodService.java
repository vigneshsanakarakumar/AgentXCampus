package com.agentx.campus.service;

import com.agentx.campus.agent.DocumentIngestionAgent;
import com.agentx.campus.agent.NotificationAgent;
import com.agentx.campus.model.*;
import com.agentx.campus.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HodService {

    private final HodProfileRepository hodProfileRepository;
    private final FacultyProfileRepository facultyProfileRepository;
    private final FacultyMentorSectionRepository facultyMentorSectionRepository;
    private final FacultyLeaveRequestRepository facultyLeaveRequestRepository;
    private final TimetableEntryRepository timetableEntryRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;
    private final DocumentIngestionAgent documentIngestionAgent;
    private final NotificationAgent notificationAgent;
    private final UserRegistrationRequestRepository userRegistrationRequestRepository;
    private final AuditLogRepository auditLogRepository;

    public HodService(
            HodProfileRepository hodProfileRepository,
            FacultyProfileRepository facultyProfileRepository,
            FacultyMentorSectionRepository facultyMentorSectionRepository,
            FacultyLeaveRequestRepository facultyLeaveRequestRepository,
            TimetableEntryRepository timetableEntryRepository,
            StudentProfileRepository studentProfileRepository,
            AnnouncementRepository announcementRepository,
            UserRepository userRepository,
            DocumentIngestionAgent documentIngestionAgent,
            NotificationAgent notificationAgent,
            UserRegistrationRequestRepository userRegistrationRequestRepository,
            AuditLogRepository auditLogRepository) {
        this.hodProfileRepository = hodProfileRepository;
        this.facultyProfileRepository = facultyProfileRepository;
        this.facultyMentorSectionRepository = facultyMentorSectionRepository;
        this.facultyLeaveRequestRepository = facultyLeaveRequestRepository;
        this.timetableEntryRepository = timetableEntryRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.announcementRepository = announcementRepository;
        this.userRepository = userRepository;
        this.documentIngestionAgent = documentIngestionAgent;
        this.notificationAgent = notificationAgent;
        this.userRegistrationRequestRepository = userRegistrationRequestRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public String resolveDepartmentForHod(String username) {
        return hodProfileRepository.findByUserUsername(username)
                .map(HodProfile::getDepartment)
                .orElse("Computer Science & Engineering");
    }

    public Map<String, Object> getDepartmentOverview(String hodUsername) {
        String dept = resolveDepartmentForHod(hodUsername);
        HodProfile hodProfile = hodProfileRepository.findByDepartment(dept).orElse(null);

        List<FacultyProfile> facultyList = facultyProfileRepository.findByDepartment(dept);
        List<StudentProfile> studentList = studentProfileRepository.findByDepartment(dept);

        List<String> sections = studentList.stream()
                .map(StudentProfile::getSection)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        if (sections.isEmpty()) {
            sections = List.of("A", "B", "C", "D");
        }

        double avgAttendance = studentList.stream()
                .mapToInt(StudentProfile::getAttendanceRate)
                .average()
                .orElse(85.0);

        List<FacultyLeaveRequest> pendingLeaves = facultyLeaveRequestRepository
                .findByDepartmentAndStatusOrderByCreatedAtDesc(dept, "PENDING");

        List<Announcement> announcements = announcementRepository.findForDepartment(dept).stream()
                .limit(5)
                .collect(Collectors.toList());

        long pendingRegistrations = userRegistrationRequestRepository.countByDepartmentAndStatus(dept, "PENDING");

        Map<String, Object> response = new HashMap<>();
        response.put("department", dept);
        response.put("hodName", hodProfile != null ? hodProfile.getHodName() : "Head of Department");
        response.put("hodEmail", hodProfile != null ? hodProfile.getHodEmail() : "");
        response.put("cabinNumber", hodProfile != null ? hodProfile.getCabinNumber() : "HOD Office");
        response.put("facultyCount", facultyList.size());
        response.put("studentCount", studentList.size());
        response.put("sections", sections);
        response.put("avgAttendanceRate", Math.round(avgAttendance * 10.0) / 10.0);
        response.put("pendingFacultyLeaveRequestsCount", pendingLeaves.size());
        response.put("pendingRegistrationsCount", pendingRegistrations);
        response.put("recentAnnouncements", announcements);

        return response;
    }

    public List<Map<String, Object>> getFacultyRoster(String hodUsername) {
        String dept = resolveDepartmentForHod(hodUsername);
        List<FacultyProfile> facultyProfiles = facultyProfileRepository.findByDepartment(dept);

        List<Map<String, Object>> result = new ArrayList<>();
        for (FacultyProfile fp : facultyProfiles) {
            User u = fp.getUser();
            Map<String, Object> item = new HashMap<>();
            item.put("facultyId", u != null ? u.getId() : fp.getId());
            item.put("username", u != null ? u.getUsername() : "");
            item.put("name", u != null ? u.getFirstName() + " " + u.getLastName() : "Faculty");
            item.put("email", u != null ? u.getEmail() : "");
            item.put("employeeId", fp.getEmployeeId());
            item.put("designation", fp.getDesignation());
            item.put("cabinNumber", fp.getCabinNumber());
            item.put("isMentor", fp.isMentor());

            List<FacultyMentorSection> mentorSecs = facultyMentorSectionRepository.findByFacultyProfileId(fp.getId());
            List<String> mentorSectionNames = mentorSecs.stream()
                    .map(ms -> "Sec " + ms.getSection())
                    .collect(Collectors.toList());
            item.put("mentorSections", mentorSectionNames);

            if (u != null) {
                long pendingLeaves = facultyLeaveRequestRepository.findByFacultyOrderByCreatedAtDesc(u).stream()
                        .filter(l -> "PENDING".equalsIgnoreCase(l.getStatus())).count();
                item.put("pendingLeavesCount", pendingLeaves);
            } else {
                item.put("pendingLeavesCount", 0);
            }

            result.add(item);
        }
        return result;
    }

    public List<TimetableEntry> getFacultyTimetable(Long facultyUserId, String hodUsername) {
        return timetableEntryRepository.findByFacultyUserIdOrderByDayOfWeekAscStartTimeAsc(facultyUserId);
    }

    public Map<String, List<TimetableEntry>> getAllClassTimetables(String hodUsername) {
        String dept = resolveDepartmentForHod(hodUsername);
        List<TimetableEntry> entries = timetableEntryRepository.findByDepartmentOrderBySectionAscDayOfWeekAscStartTimeAsc(dept);

        return entries.stream()
                .collect(Collectors.groupingBy(TimetableEntry::getSection, TreeMap::new, Collectors.toList()));
    }

    public Map<String, List<TimetableEntry>> getAllFacultyTimetables(String hodUsername) {
        String dept = resolveDepartmentForHod(hodUsername);
        List<TimetableEntry> entries = timetableEntryRepository.findByDepartmentOrderBySectionAscDayOfWeekAscStartTimeAsc(dept);

        return entries.stream()
                .filter(e -> e.getFacultyName() != null && !e.getFacultyName().isBlank())
                .collect(Collectors.groupingBy(TimetableEntry::getFacultyName, TreeMap::new, Collectors.toList()));
    }

    public List<FacultyLeaveRequest> getFacultyLeaveRequests(String hodUsername, String statusFilter) {
        String dept = resolveDepartmentForHod(hodUsername);
        if (statusFilter != null && !statusFilter.isBlank() && !"ALL".equalsIgnoreCase(statusFilter)) {
            return facultyLeaveRequestRepository.findByDepartmentAndStatusOrderByCreatedAtDesc(dept, statusFilter.toUpperCase());
        }
        return facultyLeaveRequestRepository.findByDepartmentOrderByCreatedAtDesc(dept);
    }

    @Transactional
    public FacultyLeaveRequest updateFacultyLeaveRequestStatus(Long requestId, String status, String notes, String hodUsername) {
        FacultyLeaveRequest req = facultyLeaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Faculty leave request not found: " + requestId));

        req.setStatus(status.toUpperCase());
        req.setResolutionNotes(notes);
        req.setResolvedAt(LocalDateTime.now());

        userRepository.findByUsername(hodUsername).ifPresent(req::setHodUser);
        FacultyLeaveRequest saved = facultyLeaveRequestRepository.save(req);

        // Notify faculty user via SSE and Notification entity
        if (req.getFaculty() != null) {
            String title = "Faculty Leave " + status.toUpperCase();
            String msg = "Your " + req.getLeaveType() + " request from " + req.getFromDate() + " to " + req.getToDate()
                    + " has been " + status.toLowerCase() + " by HOD." + (notes != null ? " Remarks: " + notes : "");
            notificationAgent.notifyUser(req.getFaculty(), title, msg, "LEAVE_UPDATE", "Department HOD Decision");
        }

        return saved;
    }

    public Map<String, Object> ingestDocument(byte[] fileBytes, String filename, String textOverride,
                                             String sectionHint, String hodUsername) {
        String dept = resolveDepartmentForHod(hodUsername);
        return documentIngestionAgent.ingestDocument(fileBytes, filename, textOverride, hodUsername, dept, sectionHint);
    }

    public List<UserRegistrationRequest> getDepartmentRegistrations(String hodUsername, String statusFilter) {
        String dept = resolveDepartmentForHod(hodUsername);
        if (statusFilter != null && !statusFilter.isBlank() && !"ALL".equalsIgnoreCase(statusFilter)) {
            return userRegistrationRequestRepository.findByDepartmentAndStatusOrderByCreatedAtDesc(dept, statusFilter.toUpperCase());
        }
        return userRegistrationRequestRepository.findByDepartmentOrderByCreatedAtDesc(dept);
    }

    @Transactional
    public UserRegistrationRequest approveRegistration(Long requestId, String hodUsername) {
        String dept = resolveDepartmentForHod(hodUsername);
        UserRegistrationRequest req = userRegistrationRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Registration request not found: " + requestId));

        if (!dept.equalsIgnoreCase(req.getDepartment())) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized: This registration request does not belong to your department (" + dept + ").");
        }

        req.setStatus("APPROVED");
        req.setResolvedAt(LocalDateTime.now());
        User hod = userRepository.findByUsername(hodUsername).orElse(null);
        req.setHodUser(hod);
        UserRegistrationRequest saved = userRegistrationRequestRepository.save(req);

        // Activate underlying user
        User user = req.getUser();
        if (user == null) {
            user = userRepository.findByUsername(req.getUsername()).orElse(null);
        }
        if (user != null) {
            user.setActive(true);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

            // Audit Log
            auditLogRepository.save(new AuditLog(
                    hodUsername,
                    "HOD_APPROVE_REGISTRATION",
                    "USER_REGISTRATION",
                    requestId,
                    "HOD approved registration for " + req.getUsername() + " as " + req.getRole() + " in " + req.getDepartment()
            ));

            // Notify user
            String title = "Registration Approved by HOD";
            String msg = "Congratulations! Your registration as " + req.getRole() + " in " + req.getDepartment()
                    + " has been verified and approved by your Head of Department. You may now log in to AgentX Campus.";
            notificationAgent.notifyUser(user, title, msg, "ACCOUNT_APPROVED", "Department HOD Decision");
        }

        return saved;
    }

    @Transactional
    public UserRegistrationRequest rejectRegistration(Long requestId, String reason, String hodUsername) {
        String dept = resolveDepartmentForHod(hodUsername);
        UserRegistrationRequest req = userRegistrationRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Registration request not found: " + requestId));

        if (!dept.equalsIgnoreCase(req.getDepartment())) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized: This registration request does not belong to your department (" + dept + ").");
        }

        req.setStatus("REJECTED");
        req.setRejectionReason(reason != null && !reason.isBlank() ? reason : "Rejected by Department Head of Department.");
        req.setResolvedAt(LocalDateTime.now());
        User hod = userRepository.findByUsername(hodUsername).orElse(null);
        req.setHodUser(hod);
        UserRegistrationRequest saved = userRegistrationRequestRepository.save(req);

        // Audit Log
        auditLogRepository.save(new AuditLog(
                hodUsername,
                "HOD_REJECT_REGISTRATION",
                "USER_REGISTRATION",
                requestId,
                "HOD rejected registration for " + req.getUsername() + " (" + req.getRole() + "). Reason: " + req.getRejectionReason()
        ));

        return saved;
    }
}
