package com.agentx.campus.controller;

import com.agentx.campus.dto.*;
import com.agentx.campus.model.*;
import com.agentx.campus.service.AdminService;
import com.agentx.campus.service.RequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;
    private final RequestService requestService;

    public AdminController(AdminService adminService, RequestService requestService) {
        this.adminService = adminService;
        this.requestService = requestService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        return ResponseEntity.ok(adminService.getAdminDashboard());
    }

    // --- FACULTY & MENTORS ---
    @PostMapping("/faculty")
    public ResponseEntity<?> createFaculty(@RequestBody CreateFacultyRequest req) {
        return ResponseEntity.ok(adminService.createFaculty(req));
    }

    @GetMapping("/faculty")
    public ResponseEntity<?> getAllFaculty() {
        return ResponseEntity.ok(adminService.getAllFaculty());
    }

    @PostMapping("/mentor-assignment")
    public ResponseEntity<?> assignMentor(@RequestBody MentorAssignmentRequest req) {
        return ResponseEntity.ok(adminService.assignMentor(req));
    }

    // --- STUDENTS ---
    @GetMapping("/students")
    public ResponseEntity<?> getStudents(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String section) {
        return ResponseEntity.ok(adminService.getAllStudents(department, section));
    }

    // --- TIMETABLES & CONFLICTS ---
    @GetMapping("/timetables")
    public ResponseEntity<?> getTimetables(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String section) {
        return ResponseEntity.ok(adminService.getTimetables(department, section));
    }

    @PostMapping("/timetables")
    public ResponseEntity<?> createTimetable(@RequestBody TimetableRequest req) {
        TimetableEntry entry = adminService.createTimetable(req);
        return ResponseEntity.ok(entry);
    }

    @DeleteMapping("/timetables/{id}")
    public ResponseEntity<?> deleteTimetable(@PathVariable Long id) {
        adminService.deleteTimetable(id);
        return ResponseEntity.ok(Map.of("message", "Timetable entry deleted successfully", "id", id));
    }

    @GetMapping("/conflicts")
    public ResponseEntity<?> getConflicts() {
        return ResponseEntity.ok(adminService.scanConflicts());
    }

    @PostMapping("/conflicts/scan")
    public ResponseEntity<?> scanConflictsAndGenerateApprovals() {
        return ResponseEntity.ok(adminService.triggerConflictScanAndApprovalGeneration());
    }

    @PostMapping("/approvals/{id}/approve")
    public ResponseEntity<?> approveRequest(@PathVariable Long id, Authentication authentication) {
        ApprovalRequest updated = adminService.decideApproval(id, "APPROVED", authentication.getName());
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/approvals/{id}/reject")
    public ResponseEntity<?> rejectRequest(@PathVariable Long id, Authentication authentication) {
        ApprovalRequest updated = adminService.decideApproval(id, "REJECTED", authentication.getName());
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/approvals/{id}/decision")
    public ResponseEntity<?> decideApproval(@PathVariable Long id,
                                            @RequestBody ApprovalDecisionRequest req,
                                            Authentication authentication) {
        ApprovalRequest updated = adminService.decideApproval(id, req.getDecision(), authentication.getName());
        return ResponseEntity.ok(updated);
    }

    // --- NOTICES ---
    @GetMapping("/notices")
    public ResponseEntity<?> getNotices() {
        return ResponseEntity.ok(adminService.getAllNotices());
    }

    @PostMapping("/notices")
    public ResponseEntity<?> createNotice(@RequestBody NoticeRequest req) {
        return ResponseEntity.ok(adminService.createNotice(req));
    }

    @DeleteMapping("/notices/{id}")
    public ResponseEntity<?> deleteNotice(@PathVariable Long id) {
        adminService.deleteNotice(id);
        return ResponseEntity.ok(Map.of("message", "Notice deleted successfully", "id", id));
    }

    // --- EVENTS ---
    @GetMapping("/events")
    public ResponseEntity<?> getEvents() {
        return ResponseEntity.ok(adminService.getAllEvents());
    }

    @PostMapping("/events")
    public ResponseEntity<?> createEvent(@RequestBody EventRequest req) {
        return ResponseEntity.ok(adminService.createEvent(req));
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id) {
        adminService.deleteEvent(id);
        return ResponseEntity.ok(Map.of("message", "Event deleted successfully", "id", id));
    }

    // --- KNOWLEDGE BASE / RAG DOCUMENTS ---
    @GetMapping("/documents")
    public ResponseEntity<?> getDocuments() {
        return ResponseEntity.ok(adminService.getAllDocuments());
    }

    @PostMapping("/documents")
    public ResponseEntity<?> createDocument(@RequestBody com.agentx.campus.dto.DocumentRequest req, Authentication authentication) {
        return ResponseEntity.ok(adminService.createDocument(req, authentication.getName()));
    }

    @DeleteMapping("/documents/{id}")
    public ResponseEntity<?> deleteDocument(@PathVariable Long id) {
        adminService.deleteDocument(id);
        return ResponseEntity.ok(Map.of("message", "Document deleted successfully", "id", id));
    }

    // --- COURSES & ASSIGNMENTS ---
    @GetMapping("/courses")
    public ResponseEntity<?> getCourses() {
        return ResponseEntity.ok(adminService.getAllCourses());
    }

    @GetMapping("/assignments")
    public ResponseEntity<?> getAssignments() {
        return ResponseEntity.ok(adminService.getAllAssignments());
    }

    // --- AGENT EXECUTION LOGS ---
    @GetMapping("/agent-logs")
    public ResponseEntity<?> getAgentLogs() {
        return ResponseEntity.ok(adminService.getAllAgentLogs());
    }

    // --- STAFF LOGIN REQUESTS & INVITATIONS ---
    @GetMapping("/staff-requests")
    public ResponseEntity<?> getStaffRequests(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(adminService.getStaffRequests(status));
    }

    @PostMapping("/staff-requests/{id}/approve")
    public ResponseEntity<?> approveStaffRequest(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.approveStaffRequest(id));
    }

    @PostMapping("/staff-requests/{id}/reject")
    public ResponseEntity<?> rejectStaffRequest(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(adminService.rejectStaffRequest(id, reason));
    }

    // --- MENTOR SECTION ASSIGNMENTS ---
    @GetMapping("/faculty/{facultyId}/mentor-sections")
    public ResponseEntity<?> getFacultyMentorSections(@PathVariable Long facultyId) {
        return ResponseEntity.ok(adminService.getFacultyMentorSections(facultyId));
    }

    @PostMapping("/faculty/{facultyId}/mentor-sections")
    public ResponseEntity<?> assignMentorSection(@PathVariable Long facultyId, @RequestBody MentorSectionRequestDto req) {
        return ResponseEntity.ok(adminService.assignMentorSection(facultyId, req));
    }

    @DeleteMapping("/faculty/{facultyId}/mentor-sections/{mappingId}")
    public ResponseEntity<?> removeMentorSection(@PathVariable Long facultyId, @PathVariable Long mappingId) {
        adminService.removeMentorSection(mappingId);
        return ResponseEntity.ok(Map.of("message", "Mentor-to-section mapping removed successfully", "id", mappingId));
    }

    // --- COMPLAINTS & GRIEVANCE MANAGEMENT ---
    @GetMapping("/complaints")
    public ResponseEntity<?> getComplaints(
            @RequestParam(required = false) String filerRole,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(adminService.getAdminGrievances(filerRole, status));
    }

    @PatchMapping("/complaints/{id}/status")
    public ResponseEntity<?> updateComplaintStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        String status = body != null ? body.get("status") : null;
        String notes = body != null ? (body.get("resolutionNotes") != null ? body.get("resolutionNotes") : body.get("notes")) : null;
        Grievance updated = adminService.updateGrievanceStatus(id, status, notes, authentication != null ? authentication.getName() : "admin");
        return ResponseEntity.ok(updated);
    }

    // ─── Task 2: Document requests (admin) ───────────────────────────────────

    @GetMapping("/document-requests")
    public ResponseEntity<?> getAllDocumentRequests() {
        return ResponseEntity.ok(requestService.getAllDocumentRequests());
    }

    @PatchMapping("/document-requests/{id}/status")
    public ResponseEntity<?> updateDocumentStatus(Authentication auth, @PathVariable Long id,
                                                   @RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(requestService.updateDocumentStatus(
                    id, body.getOrDefault("status", "PROCESSING"),
                    body.get("notes"), auth != null ? auth.getName() : "admin"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
