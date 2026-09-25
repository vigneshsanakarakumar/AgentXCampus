package com.agentx.campus.controller;

import com.agentx.campus.dto.CourseworkRequestDto;
import com.agentx.campus.dto.GrievanceRequest;
import com.agentx.campus.dto.TimetableRequest;
import com.agentx.campus.model.Grievance;
import com.agentx.campus.model.TimetableEntry;
import com.agentx.campus.service.FacultyService;
import com.agentx.campus.service.RequestService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/faculty")
@PreAuthorize("hasRole('FACULTY')")
public class FacultyController {

    private final FacultyService facultyService;
    private final RequestService requestService;

    public FacultyController(FacultyService facultyService, RequestService requestService) {
        this.facultyService = facultyService;
        this.requestService = requestService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(Authentication authentication) {
        return ResponseEntity.ok(facultyService.getFacultyDashboard(authentication.getName()));
    }

    @GetMapping("/mentees")
    public ResponseEntity<?> getMentees(Authentication authentication) {
        return ResponseEntity.ok(facultyService.getMentees(authentication.getName()));
    }

    @GetMapping("/mentor-sections")
    public ResponseEntity<?> getMentorSections(Authentication authentication) {
        return ResponseEntity.ok(facultyService.getFacultyMentorSections(authentication.getName()));
    }

    @GetMapping("/mentor-sections/{sectionId}/students")
    public ResponseEntity<?> getMenteesBySection(Authentication authentication, @PathVariable Long sectionId) {
        return ResponseEntity.ok(facultyService.getMenteesBySection(authentication.getName(), sectionId));
    }

    // --- MENTOR CLASS ROSTER & ATTENDANCE SUMMARY ---

    @GetMapping("/mentor-sections/{sectionId}/roster")
    public ResponseEntity<?> getClassRoster(Authentication authentication, @PathVariable Long sectionId) {
        return ResponseEntity.ok(facultyService.getClassRoster(sectionId, authentication.getName()));
    }

    @GetMapping("/mentor-sections/{sectionId}/attendance-summary")
    public ResponseEntity<?> getClassAttendanceSummary(Authentication authentication, @PathVariable Long sectionId) {
        return ResponseEntity.ok(facultyService.getClassAttendanceSummary(sectionId, authentication.getName()));
    }

    // --- COURSEWORK ASSIGNMENT FOR WHOLE CLASS ---

    @PostMapping("/mentor-sections/{sectionId}/assignments")
    public ResponseEntity<?> assignCoursework(Authentication authentication,
                                              @PathVariable Long sectionId,
                                              @RequestBody CourseworkRequestDto req) {
        return ResponseEntity.ok(facultyService.assignCourseworkToSection(sectionId, req, authentication.getName()));
    }

    @GetMapping("/mentor-sections/{sectionId}/assignments")
    public ResponseEntity<?> getSectionAssignments(Authentication authentication, @PathVariable Long sectionId) {
        return ResponseEntity.ok(facultyService.getSectionAssignments(sectionId, authentication.getName()));
    }

    @GetMapping("/my-timetable")
    public ResponseEntity<?> getMyTimetable(Authentication authentication) {
        return ResponseEntity.ok(facultyService.getMyTimetable(authentication.getName()));
    }

    @GetMapping("/section-timetable/{sectionId}")
    public ResponseEntity<?> getSectionTimetable(Authentication authentication, @PathVariable Long sectionId) {
        return ResponseEntity.ok(facultyService.getSectionTimetable(sectionId, authentication.getName()));
    }

    @PostMapping("/section-timetable/{sectionId}")
    public ResponseEntity<?> addSectionTimetablePeriod(Authentication authentication,
                                                       @PathVariable Long sectionId,
                                                       @RequestBody TimetableRequest req) {
        return ResponseEntity.ok(facultyService.addSectionTimetablePeriod(sectionId, req, authentication.getName()));
    }

    @PutMapping("/section-timetable/{sectionId}/{entryId}")
    public ResponseEntity<?> updateSectionTimetable(Authentication authentication,
                                                    @PathVariable Long sectionId,
                                                    @PathVariable Long entryId,
                                                    @RequestBody TimetableRequest req) {
        return ResponseEntity.ok(facultyService.updateSectionTimetable(sectionId, entryId, req, authentication.getName()));
    }

    @DeleteMapping("/section-timetable/{sectionId}/{entryId}")
    public ResponseEntity<?> deleteSectionTimetablePeriod(Authentication authentication,
                                                          @PathVariable Long sectionId,
                                                          @PathVariable Long entryId) {
        facultyService.deleteSectionTimetablePeriod(sectionId, entryId, authentication.getName());
        return ResponseEntity.ok(Map.of("message", "Period removed from section timetable successfully", "entryId", entryId));
    }

    @PostMapping("/timetable/check-conflicts")
    public ResponseEntity<?> checkConflicts(@RequestBody TimetableRequest req,
                                            @RequestParam(required = false) Long excludeId) {
        return ResponseEntity.ok(facultyService.checkTimetableConflicts(req, excludeId));
    }

    @GetMapping("/timetable")
    public ResponseEntity<?> getTimetable(Authentication authentication) {
        return ResponseEntity.ok(facultyService.getTimetable(authentication.getName()));
    }

    @PostMapping("/timetable")
    public ResponseEntity<?> createTimetable(Authentication authentication, @RequestBody TimetableRequest req) {
        TimetableEntry entry = facultyService.createTimetableEntry(authentication.getName(), req);
        return ResponseEntity.ok(entry);
    }

    @DeleteMapping("/timetable/{id}")
    public ResponseEntity<?> deleteTimetable(Authentication authentication, @PathVariable Long id) {
        facultyService.deleteTimetableEntry(authentication.getName(), id);
        return ResponseEntity.ok(Map.of("message", "Timetable entry deleted successfully", "id", id));
    }

    // --- MENTOR COMPLAINTS & GRIEVANCE RESOLUTION ---

    @GetMapping("/complaints")
    public ResponseEntity<?> getMentorComplaints(Authentication authentication) {
        return ResponseEntity.ok(facultyService.getMentorComplaints(authentication.getName()));
    }

    @PatchMapping("/complaints/{id}/status")
    public ResponseEntity<?> updateComplaintStatus(Authentication authentication,
                                                   @PathVariable Long id,
                                                   @RequestBody Map<String, String> body) {
        String status = body != null ? body.get("status") : null;
        String notes = body != null ? (body.get("resolutionNotes") != null ? body.get("resolutionNotes") : body.get("notes")) : null;
        Grievance updated = facultyService.updateGrievanceStatus(id, status, notes, authentication.getName());
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/complaints")
    public ResponseEntity<?> fileFacultyComplaint(Authentication authentication, @RequestBody GrievanceRequest req) {
        Grievance created = facultyService.createFacultyGrievance(authentication.getName(), req);
        return ResponseEntity.ok(created);
    }

    // ─── Task 2: Leave / OD request management for mentor ────────────────────

    @GetMapping("/leave-requests")
    public ResponseEntity<?> getMentorLeaveRequests(Authentication auth) {
        return ResponseEntity.ok(requestService.getMentorLeaveRequests(auth.getName()));
    }

    @PatchMapping("/leave-requests/{id}/status")
    public ResponseEntity<?> updateLeaveStatus(Authentication auth, @PathVariable Long id,
                                               @RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(requestService.updateLeaveStatus(
                    id, body.getOrDefault("status", "APPROVED"),
                    body.get("notes"), auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/od-requests")
    public ResponseEntity<?> getMentorODRequests(Authentication auth) {
        return ResponseEntity.ok(requestService.getMentorODRequests(auth.getName()));
    }

    @PatchMapping("/od-requests/{id}/status")
    public ResponseEntity<?> updateODStatus(Authentication auth, @PathVariable Long id,
                                            @RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(requestService.updateODStatus(
                    id, body.getOrDefault("status", "APPROVED"),
                    body.get("notes"), auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
