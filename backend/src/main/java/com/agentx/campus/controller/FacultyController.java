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

    // --- TASK: Faculty Leave / Permission Request to HOD ---

    @PostMapping("/leave-requests")
    public ResponseEntity<?> submitFacultyLeaveRequest(Authentication auth, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(facultyService.submitFacultyLeaveRequest(auth.getName(), body));
    }

    @GetMapping("/leave-requests/my")
    public ResponseEntity<?> getMyFacultyLeaveRequests(Authentication auth) {
        return ResponseEntity.ok(facultyService.getMyFacultyLeaveRequests(auth.getName()));
    }

    // --- TASK: Faculty Timetable Upload (Individual staff or class timetable) ---

    @PostMapping(value = "/timetable/upload")
    public ResponseEntity<?> uploadTimetable(
            Authentication auth,
            @RequestParam(value = "file", required = false) org.springframework.web.multipart.MultipartFile file,
            @RequestParam(value = "textOverride", required = false) String textOverride,
            @RequestParam(value = "isClassTimetable", defaultValue = "false") boolean isClassTimetable,
            @RequestParam(value = "section", required = false) String section,
            @RequestBody(required = false) Map<String, Object> jsonBody) {
        try {
            byte[] bytes = file != null ? file.getBytes() : null;
            String filename = file != null ? file.getOriginalFilename() : "timetable.txt";
            String text = textOverride;
            boolean isClass = isClassTimetable;
            String sec = section;
            if (jsonBody != null) {
                if (text == null && jsonBody.containsKey("textOverride")) text = String.valueOf(jsonBody.get("textOverride"));
                if (jsonBody.containsKey("isClassTimetable")) isClass = Boolean.parseBoolean(String.valueOf(jsonBody.get("isClassTimetable")));
                if (sec == null && jsonBody.containsKey("section")) sec = String.valueOf(jsonBody.get("section"));
            }
            return ResponseEntity.ok(facultyService.uploadTimetable(bytes, filename, text, isClass, sec, auth.getName()));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", ex.getMessage()));
        }
    }

    // --- TASK: Quick Attendance Marker for current date ---

    @PostMapping("/attendance/quick-mark")
    public ResponseEntity<?> quickMarkAttendance(Authentication auth, @RequestBody Map<String, Object> body) {
        Long sectionId = null;
        if (body.get("sectionId") != null && !body.get("sectionId").toString().isBlank()) {
            sectionId = Long.valueOf(body.get("sectionId").toString());
        }
        String dateStr = body.getOrDefault("date", "").toString();
        java.util.List<Long> absentStudentIds = new java.util.ArrayList<>();
        if (body.get("absentStudentIds") instanceof java.util.List<?> list) {
            for (Object item : list) {
                absentStudentIds.add(Long.valueOf(item.toString()));
            }
        }
        String remarks = (String) body.get("remarks");
        return ResponseEntity.ok(facultyService.quickMarkAttendance(auth.getName(), sectionId, dateStr, absentStudentIds, remarks));
    }

    // --- TASK: View Department Events ---

    @GetMapping("/events")
    public ResponseEntity<?> getFacultyEvents(Authentication auth) {
        return ResponseEntity.ok(facultyService.getFacultyEvents(auth.getName()));
    }
}
