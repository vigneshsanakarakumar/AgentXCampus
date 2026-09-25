package com.agentx.campus.controller;

import com.agentx.campus.dto.GrievanceRequest;
import com.agentx.campus.dto.TaskRequest;
import com.agentx.campus.model.Grievance;
import com.agentx.campus.model.StudentTask;
import com.agentx.campus.service.AttendanceSessionService;
import com.agentx.campus.service.RequestService;
import com.agentx.campus.service.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/student")
public class StudentController {

    private final StudentService studentService;
    private final AttendanceSessionService attendanceSessionService;
    private final RequestService requestService;

    public StudentController(StudentService studentService,
                             AttendanceSessionService attendanceSessionService,
                             RequestService requestService) {
        this.studentService = studentService;
        this.attendanceSessionService = attendanceSessionService;
        this.requestService = requestService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(Authentication authentication) {
        return ResponseEntity.ok(studentService.getStudentDashboard(authentication.getName()));
    }

    @GetMapping("/timetable")
    public ResponseEntity<?> getTimetable(Authentication authentication) {
        return ResponseEntity.ok(studentService.getTimetable(authentication.getName()));
    }

    @GetMapping("/assignments")
    public ResponseEntity<?> getAssignments(Authentication authentication) {
        return ResponseEntity.ok(studentService.getAssignments(authentication.getName()));
    }

    @GetMapping("/tasks")
    public ResponseEntity<?> getTasks(Authentication authentication) {
        return ResponseEntity.ok(studentService.getTasks(authentication.getName()));
    }

    @PostMapping("/tasks")
    public ResponseEntity<?> createTask(Authentication authentication, @RequestBody TaskRequest req) {
        StudentTask task = studentService.createTask(authentication.getName(), req);
        return ResponseEntity.ok(task);
    }

    @PatchMapping("/tasks/{id}/status")
    public ResponseEntity<?> updateTaskStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.getOrDefault("status", "COMPLETED");
        StudentTask task = studentService.updateTaskStatus(id, status);
        return ResponseEntity.ok(task);
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        studentService.deleteTask(id);
        return ResponseEntity.ok(Map.of("message", "Task deleted successfully"));
    }

    @PostMapping("/grievances")
    public ResponseEntity<?> createGrievance(Authentication authentication, @RequestBody GrievanceRequest req) {
        Grievance g = studentService.createGrievance(authentication.getName(), req);
        return ResponseEntity.ok(g);
    }

    // ─── Task 1: Attendance History ───────────────────────────────────────────

    @GetMapping("/attendance-history")
    public ResponseEntity<?> getAttendanceHistory(Authentication authentication) {
        return ResponseEntity.ok(attendanceSessionService.getStudentAttendanceHistory(authentication.getName()));
    }

    // ─── Task 2: Leave Requests ───────────────────────────────────────────────

    @PostMapping("/leave-requests")
    public ResponseEntity<?> submitLeave(Authentication auth, @RequestBody java.util.Map<String, Object> body) {
        try { return ResponseEntity.ok(requestService.submitLeaveRequest(auth.getName(), body)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage())); }
    }

    @GetMapping("/leave-requests")
    public ResponseEntity<?> getLeaveRequests(Authentication auth) {
        return ResponseEntity.ok(requestService.getStudentLeaveRequests(auth.getName()));
    }

    @PostMapping("/od-requests")
    public ResponseEntity<?> submitOD(Authentication auth, @RequestBody java.util.Map<String, Object> body) {
        try { return ResponseEntity.ok(requestService.submitODRequest(auth.getName(), body)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage())); }
    }

    @GetMapping("/od-requests")
    public ResponseEntity<?> getODRequests(Authentication auth) {
        return ResponseEntity.ok(requestService.getStudentODRequests(auth.getName()));
    }

    @PostMapping("/document-requests")
    public ResponseEntity<?> submitDocument(Authentication auth, @RequestBody java.util.Map<String, Object> body) {
        try { return ResponseEntity.ok(requestService.submitDocumentRequest(auth.getName(), body)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage())); }
    }

    @GetMapping("/document-requests")
    public ResponseEntity<?> getDocumentRequests(Authentication auth) {
        return ResponseEntity.ok(requestService.getStudentDocumentRequests(auth.getName()));
    }
}
