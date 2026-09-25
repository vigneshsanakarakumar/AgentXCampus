package com.agentx.campus.controller;

import com.agentx.campus.model.FacultyLeaveRequest;
import com.agentx.campus.service.HodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/hod")
@PreAuthorize("hasAnyRole('HOD', 'ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "HOD Department Management", description = "Endpoints for Department HOD: Class/Faculty timetables, faculty oversight, leave approvals, and AI document ingestion & circular dispatching")
public class HodController {

    private final HodService hodService;

    public HodController(HodService hodService) {
        this.hodService = hodService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get Department overview, faculty counts, student stats, and pending leave requests")
    public ResponseEntity<?> getDashboard(Authentication authentication) {
        return ResponseEntity.ok(hodService.getDepartmentOverview(authentication.getName()));
    }

    @GetMapping("/faculty")
    @Operation(summary = "Get all faculty profiles in HOD's department with mentor assignments")
    public ResponseEntity<?> getFacultyRoster(Authentication authentication) {
        return ResponseEntity.ok(hodService.getFacultyRoster(authentication.getName()));
    }

    @GetMapping("/faculty/{facultyId}/timetable")
    @Operation(summary = "Get timetable schedule for a specific faculty member")
    public ResponseEntity<?> getFacultyTimetable(Authentication authentication, @PathVariable Long facultyId) {
        return ResponseEntity.ok(hodService.getFacultyTimetable(facultyId, authentication.getName()));
    }

    @GetMapping("/timetables/classes")
    @Operation(summary = "Get all section timetables across the department (Sections A, B, C, D)")
    public ResponseEntity<?> getAllClassTimetables(Authentication authentication) {
        return ResponseEntity.ok(hodService.getAllClassTimetables(authentication.getName()));
    }

    @GetMapping("/timetables/faculty")
    @Operation(summary = "Get all faculty timetables across the department")
    public ResponseEntity<?> getAllFacultyTimetables(Authentication authentication) {
        return ResponseEntity.ok(hodService.getAllFacultyTimetables(authentication.getName()));
    }

    @GetMapping("/faculty-leave-requests")
    @Operation(summary = "Get pending and historical leave and permission requests submitted by department faculty")
    public ResponseEntity<?> getFacultyLeaveRequests(Authentication authentication,
                                                     @RequestParam(required = false) String status) {
        return ResponseEntity.ok(hodService.getFacultyLeaveRequests(authentication.getName(), status));
    }

    @PatchMapping("/faculty-leave-requests/{id}/status")
    @Operation(summary = "Approve or reject a faculty leave/permission request")
    public ResponseEntity<?> updateFacultyLeaveStatus(Authentication authentication,
                                                      @PathVariable Long id,
                                                      @RequestBody Map<String, String> body) {
        String status = body.getOrDefault("status", "APPROVED");
        String notes = body.get("notes");
        FacultyLeaveRequest updated = hodService.updateFacultyLeaveRequestStatus(id, status, notes, authentication.getName());
        return ResponseEntity.ok(updated);
    }

    @PostMapping(value = "/documents/upload")
    @Operation(summary = "Upload image or PDF document for AI extraction, classification, and targeted circular dispatching")
    public ResponseEntity<?> uploadDocument(
            Authentication authentication,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "textOverride", required = false) String textOverride,
            @RequestParam(value = "sectionHint", required = false) String sectionHint,
            @RequestBody(required = false) Map<String, Object> jsonBody) {
        try {
            byte[] bytes = file != null ? file.getBytes() : null;
            String filename = file != null ? file.getOriginalFilename() : "document.txt";
            String text = textOverride;
            String hint = sectionHint;
            if (jsonBody != null) {
                if (text == null && jsonBody.containsKey("textOverride")) {
                    text = String.valueOf(jsonBody.get("textOverride"));
                }
                if (hint == null && jsonBody.containsKey("sectionHint")) {
                    hint = String.valueOf(jsonBody.get("sectionHint"));
                }
            }
            Map<String, Object> result = hodService.ingestDocument(bytes, filename, text, hint, authentication.getName());
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", ex.getMessage()));
        }
    }

    @GetMapping("/registrations")
    @Operation(summary = "Get student and faculty registration requests pending HOD approval")
    public ResponseEntity<?> getRegistrations(Authentication authentication,
                                             @RequestParam(required = false) String status) {
        return ResponseEntity.ok(hodService.getDepartmentRegistrations(authentication.getName(), status));
    }

    @PostMapping("/registrations/{id}/approve")
    @Operation(summary = "Approve student or faculty registration request and activate account")
    public ResponseEntity<?> approveRegistration(Authentication authentication, @PathVariable Long id) {
        try {
            return ResponseEntity.ok(hodService.approveRegistration(id, authentication.getName()));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/registrations/{id}/reject")
    @Operation(summary = "Reject student or faculty registration request")
    public ResponseEntity<?> rejectRegistration(Authentication authentication,
                                                @PathVariable Long id,
                                                @RequestBody(required = false) Map<String, String> body) {
        try {
            String reason = body != null ? body.get("reason") : null;
            return ResponseEntity.ok(hodService.rejectRegistration(id, reason, authentication.getName()));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }
}
