package com.agentx.campus.controller;

import com.agentx.campus.service.ExamService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @PostMapping("/admin/exam-schedules")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public ResponseEntity<?> createExam(Authentication auth, @RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(examService.createExam(body, auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/admin/exam-schedules")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public ResponseEntity<?> getAllExams() {
        return ResponseEntity.ok(examService.getAllExams());
    }

    @PutMapping("/admin/exam-schedules/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public ResponseEntity<?> updateExam(Authentication auth, @PathVariable Long id,
                                        @RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(examService.updateExam(id, body, auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/admin/exam-schedules/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public ResponseEntity<?> deleteExam(@PathVariable Long id) {
        try {
            examService.deleteExam(id);
            return ResponseEntity.ok(Map.of("message", "Exam deleted successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/student/exams")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getStudentExams(Authentication auth) {
        return ResponseEntity.ok(examService.getStudentExams(auth.getName()));
    }
}
