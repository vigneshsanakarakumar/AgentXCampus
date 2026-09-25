package com.agentx.campus.service;

import com.agentx.campus.agent.NotificationAgent;
import com.agentx.campus.model.*;
import com.agentx.campus.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Handles Leave, OD, and Document requests.
 * Routing: Student → class mentor (FACULTY) via the same section-lookup used in Grievance.
 * DocumentRequest → ADMIN directly.
 */
@Service
public class RequestService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final FacultyMentorSectionRepository mentorSectionRepository;
    private final LeaveRequestRepository leaveRepo;
    private final ODRequestRepository odRepo;
    private final DocumentRequestRepository docRepo;
    private final AttendanceSessionRepository sessionRepo;
    private final AttendanceEntryRepository entryRepo;
    private final NotificationAgent notificationAgent;

    public RequestService(UserRepository userRepository,
                          StudentProfileRepository studentProfileRepository,
                          FacultyMentorSectionRepository mentorSectionRepository,
                          LeaveRequestRepository leaveRepo,
                          ODRequestRepository odRepo,
                          DocumentRequestRepository docRepo,
                          AttendanceSessionRepository sessionRepo,
                          AttendanceEntryRepository entryRepo,
                          NotificationAgent notificationAgent) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.mentorSectionRepository = mentorSectionRepository;
        this.leaveRepo = leaveRepo;
        this.odRepo = odRepo;
        this.docRepo = docRepo;
        this.sessionRepo = sessionRepo;
        this.entryRepo = entryRepo;
        this.notificationAgent = notificationAgent;
    }

    // ─── PRIVATE HELPERS ─────────────────────────────────────────────────────

    /** Reuses the same section→mentor lookup as Grievance routing. Returns mentor or first ADMIN. */
    private User resolveApprover(String studentUsername) {
        User student = userRepository.findByUsername(studentUsername).orElseThrow();
        StudentProfile profile = studentProfileRepository.findByUser(student).orElse(null);
        if (profile != null) {
            List<FacultyMentorSection> sections = mentorSectionRepository
                    .findByDepartmentAndSection(profile.getDepartment(), profile.getSection());
            if (!sections.isEmpty()
                    && sections.get(0).getFacultyProfile() != null
                    && sections.get(0).getFacultyProfile().getUser() != null) {
                return sections.get(0).getFacultyProfile().getUser();
            }
        }
        return getFirstAdmin();
    }

    private User getFirstAdmin() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() != null && "ADMIN".equals(u.getRole().name()))
                .findFirst().orElse(null);
    }

    private LocalDate parseDate(Object obj) {
        if (obj == null) return null;
        try { return LocalDate.parse(obj.toString()); }
        catch (Exception e) { return null; }
    }

    // ─── LEAVE ───────────────────────────────────────────────────────────────

    @Transactional
    public LeaveRequest submitLeaveRequest(String username, Map<String, Object> body) {
        User student = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        User approver = resolveApprover(username);

        LeaveRequest req = new LeaveRequest();
        req.setStudent(student);
        req.setLeaveType(body.getOrDefault("leaveType", "PERSONAL").toString().toUpperCase());
        req.setFromDate(parseDate(body.get("fromDate")));
        req.setToDate(parseDate(body.get("toDate")));
        if (req.getFromDate() == null) req.setFromDate(LocalDate.now());
        if (req.getToDate() == null) req.setToDate(req.getFromDate());
        req.setPeriod(body.containsKey("period") ? body.get("period").toString() : null);
        req.setReason(body.getOrDefault("reason", "").toString());
        req.setAttachmentUrl(body.containsKey("attachmentUrl") ? body.get("attachmentUrl").toString() : null);
        req.setStatus("PENDING");
        req.setAssignedToUser(approver);
        req.setAssignedToRole(approver != null && "ADMIN".equals(approver.getRole().name()) ? "ADMIN" : "FACULTY");
        req = leaveRepo.save(req);

        // Notify the approver
        try {
            if (approver != null) {
                notificationAgent.notifyUser(approver,
                        "New Leave Request from " + req.getStudentName(),
                        req.getLeaveType() + " leave: " + req.getFromDate() + " to " + req.getToDate() + " — " + req.getReason(),
                        "LEAVE_UPDATE");
            }
        } catch (Exception ignored) {}

        return req;
    }

    public List<LeaveRequest> getStudentLeaveRequests(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        return leaveRepo.findByStudentOrderByCreatedAtDesc(user);
    }

    public List<LeaveRequest> getMentorLeaveRequests(String mentorUsername) {
        User mentor = userRepository.findByUsername(mentorUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + mentorUsername));
        return leaveRepo.findByAssignedToUserOrderByCreatedAtDesc(mentor);
    }

    @Transactional
    public LeaveRequest updateLeaveStatus(Long id, String status, String notes, String mentorUsername) {
        LeaveRequest req = leaveRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found: " + id));

        User mentor = userRepository.findByUsername(mentorUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + mentorUsername));

        // Only assigned approver or admin can update
        boolean isAssigned = req.getAssignedToUser() != null && req.getAssignedToUser().getId().equals(mentor.getId());
        boolean isAdmin = "ADMIN".equals(mentor.getRole().name());
        if (!isAssigned && !isAdmin) {
            throw new AccessDeniedException("Not authorized to update this leave request.");
        }

        req.setStatus(status.toUpperCase());
        req.setResolutionNotes(notes);
        if ("APPROVED".equals(req.getStatus()) || "REJECTED".equals(req.getStatus())) {
            req.setResolvedAt(LocalDateTime.now());
        }
        req = leaveRepo.save(req);

        // Cascade to attendance entries if approved
        if ("APPROVED".equals(req.getStatus())) {
            cascadeLeaveToAttendance(req);
        }

        // Notify student
        try {
            notificationAgent.notifyUser(req.getStudent(),
                    "Leave Request " + req.getStatus(),
                    "Your " + req.getLeaveType() + " leave (" + req.getFromDate() + " to " + req.getToDate() + ") has been " + req.getStatus().toLowerCase() + "." + (notes != null && !notes.isEmpty() ? " Note: " + notes : ""),
                    "LEAVE_UPDATE");
        } catch (Exception ignored) {}

        return req;
    }

    private void cascadeLeaveToAttendance(LeaveRequest req) {
        User student = req.getStudent();
        StudentProfile profile = studentProfileRepository.findByUser(student).orElse(null);
        if (profile == null) return;

        List<FacultyMentorSection> sections = mentorSectionRepository
                .findByDepartmentAndSection(profile.getDepartment(), profile.getSection());
        if (sections.isEmpty()) return;
        Long sectionId = sections.get(0).getId();

        LocalDate date = req.getFromDate();
        while (!date.isAfter(req.getToDate())) {
            final LocalDate d = date;
            List<AttendanceSession> daySessions = sessionRepo
                    .findBySection_IdOrderBySessionDateDescCreatedAtDesc(sectionId)
                    .stream().filter(s -> s.getSessionDate().equals(d)).toList();

            for (AttendanceSession sess : daySessions) {
                entryRepo.findBySession_IdAndStudent_Id(sess.getId(), student.getId()).ifPresent(entry -> {
                    entry.setStatus("LEAVE");
                    entry.setRemarks("Auto: Leave approved (" + req.getLeaveType() + ")");
                    entryRepo.save(entry);
                });
            }
            date = date.plusDays(1);
        }
    }

    // ─── OD ──────────────────────────────────────────────────────────────────

    @Transactional
    public ODRequest submitODRequest(String username, Map<String, Object> body) {
        User student = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        User approver = resolveApprover(username);

        ODRequest req = new ODRequest();
        req.setStudent(student);
        req.setEventName(body.getOrDefault("eventName", "Event").toString());
        req.setEventDate(parseDate(body.get("eventDate")));
        if (req.getEventDate() == null) req.setEventDate(LocalDate.now());
        req.setPeriods(body.containsKey("periods") ? body.get("periods").toString() : null);
        req.setLocation(body.containsKey("location") ? body.get("location").toString() : null);
        req.setReason(body.getOrDefault("reason", "").toString());
        req.setProofUrl(body.containsKey("proofUrl") ? body.get("proofUrl").toString() : null);
        req.setStatus("PENDING");
        req.setAssignedToUser(approver);
        req.setAssignedToRole(approver != null && "ADMIN".equals(approver.getRole().name()) ? "ADMIN" : "FACULTY");
        req = odRepo.save(req);

        try {
            if (approver != null) {
                notificationAgent.notifyUser(approver,
                        "New OD Request from " + req.getStudentName(),
                        "OD for: " + req.getEventName() + " on " + req.getEventDate(),
                        "OD_UPDATE");
            }
        } catch (Exception ignored) {}
        return req;
    }

    public List<ODRequest> getStudentODRequests(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return odRepo.findByStudentOrderByCreatedAtDesc(user);
    }

    public List<ODRequest> getMentorODRequests(String mentorUsername) {
        User mentor = userRepository.findByUsername(mentorUsername).orElseThrow();
        return odRepo.findByAssignedToUserOrderByCreatedAtDesc(mentor);
    }

    @Transactional
    public ODRequest updateODStatus(Long id, String status, String notes, String mentorUsername) {
        ODRequest req = odRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("OD request not found: " + id));
        User mentor = userRepository.findByUsername(mentorUsername).orElseThrow();

        boolean isAssigned = req.getAssignedToUser() != null && req.getAssignedToUser().getId().equals(mentor.getId());
        boolean isAdmin = "ADMIN".equals(mentor.getRole().name());
        if (!isAssigned && !isAdmin) throw new AccessDeniedException("Not authorized.");

        req.setStatus(status.toUpperCase());
        req.setResolutionNotes(notes);
        if ("APPROVED".equals(req.getStatus()) || "REJECTED".equals(req.getStatus())) {
            req.setResolvedAt(LocalDateTime.now());
        }
        req = odRepo.save(req);

        // Cascade OD to attendance entries
        if ("APPROVED".equals(req.getStatus())) {
            cascadeODToAttendance(req);
        }

        try {
            notificationAgent.notifyUser(req.getStudent(),
                    "OD Request " + req.getStatus(),
                    "Your OD for " + req.getEventName() + " (" + req.getEventDate() + ") has been " + req.getStatus().toLowerCase() + ".",
                    "OD_UPDATE");
        } catch (Exception ignored) {}
        return req;
    }

    private void cascadeODToAttendance(ODRequest req) {
        User student = req.getStudent();
        StudentProfile profile = studentProfileRepository.findByUser(student).orElse(null);
        if (profile == null) return;

        List<FacultyMentorSection> sections = mentorSectionRepository
                .findByDepartmentAndSection(profile.getDepartment(), profile.getSection());
        if (sections.isEmpty()) return;

        List<AttendanceSession> daySessions = sessionRepo
                .findBySection_IdOrderBySessionDateDescCreatedAtDesc(sections.get(0).getId())
                .stream().filter(s -> s.getSessionDate().equals(req.getEventDate())).toList();

        for (AttendanceSession sess : daySessions) {
            entryRepo.findBySession_IdAndStudent_Id(sess.getId(), student.getId()).ifPresent(entry -> {
                entry.setStatus("OD");
                entry.setRemarks("Auto: OD approved — " + req.getEventName());
                entryRepo.save(entry);
            });
        }
    }

    // ─── DOCUMENT ─────────────────────────────────────────────────────────────

    @Transactional
    public DocumentRequest submitDocumentRequest(String username, Map<String, Object> body) {
        User student = userRepository.findByUsername(username).orElseThrow();
        User admin = getFirstAdmin();

        DocumentRequest req = new DocumentRequest();
        req.setStudent(student);
        req.setDocumentType(body.getOrDefault("documentType", "BONAFIDE").toString().toUpperCase());
        req.setPurpose(body.getOrDefault("purpose", "").toString());
        req.setStatus("PENDING");
        req.setAssignedToUser(admin);
        req.setAssignedToRole("ADMIN");
        req = docRepo.save(req);

        try {
            if (admin != null) {
                notificationAgent.notifyUser(admin,
                        "Document Request: " + req.getDocumentType(),
                        "From: " + req.getStudentName() + " — " + req.getPurpose(),
                        "DOCUMENT_REQUEST_UPDATE");
            }
        } catch (Exception ignored) {}
        return req;
    }

    public List<DocumentRequest> getStudentDocumentRequests(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return docRepo.findByStudentOrderByCreatedAtDesc(user);
    }

    public List<DocumentRequest> getAllDocumentRequests() {
        return docRepo.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public DocumentRequest updateDocumentStatus(Long id, String status, String notes, String adminUsername) {
        DocumentRequest req = docRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document request not found: " + id));

        req.setStatus(status.toUpperCase());
        req.setResolutionNotes(notes);
        if (!"PENDING".equals(req.getStatus())) req.setResolvedAt(LocalDateTime.now());
        req = docRepo.save(req);

        try {
            notificationAgent.notifyUser(req.getStudent(),
                    "Document Request " + req.getStatus(),
                    "Your " + req.getDocumentType() + " request is now: " + req.getStatus() + "." + (notes != null && !notes.isEmpty() ? " Note: " + notes : ""),
                    "DOCUMENT_REQUEST_UPDATE");
        } catch (Exception ignored) {}
        return req;
    }
}
