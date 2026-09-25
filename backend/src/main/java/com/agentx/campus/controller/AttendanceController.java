package com.agentx.campus.controller;

import com.agentx.campus.service.AttendanceSessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/faculty/attendance-sessions")
@PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
public class AttendanceController {

    private final AttendanceSessionService service;

    public AttendanceController(AttendanceSessionService service) {
        this.service = service;
    }

    /** POST /faculty/attendance-sessions — create a new session, default all students PRESENT */
    @PostMapping
    public ResponseEntity<?> createSession(Authentication auth, @RequestBody Map<String, Object> body) {
        try {
            Long sectionId = Long.valueOf(body.get("sectionId").toString());
            String date = body.getOrDefault("date", java.time.LocalDate.now().toString()).toString();
            String period = body.getOrDefault("period", "Period 1").toString();
            String subjectCode = body.getOrDefault("subjectCode", "CS101").toString();
            String subjectName = body.getOrDefault("subjectName", subjectCode).toString();
            return ResponseEntity.ok(service.createSession(sectionId, date, period, subjectCode, subjectName, auth.getName()));
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** PUT /faculty/attendance-sessions/{sessionId}/entries — bulk mark-by-exception */
    @PutMapping("/{sessionId}/entries")
    public ResponseEntity<?> bulkUpdateEntries(Authentication auth,
                                                @PathVariable Long sessionId,
                                                @RequestBody List<Map<String, Object>> updates) {
        try {
            return ResponseEntity.ok(service.bulkUpdateEntries(sessionId, updates, auth.getName()));
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** GET /faculty/attendance-sessions?sectionId=X — list sessions for a section */
    @GetMapping
    public ResponseEntity<?> getSessionsForSection(Authentication auth,
                                                    @RequestParam Long sectionId) {
        try {
            return ResponseEntity.ok(service.getSessionsForSection(sectionId, auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
