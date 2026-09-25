package com.agentx.campus.controller;

import com.agentx.campus.service.OpportunityService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class OpportunityController {

    private final OpportunityService opportunityService;

    public OpportunityController(OpportunityService opportunityService) {
        this.opportunityService = opportunityService;
    }

    @PostMapping("/admin/opportunities")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(Authentication auth, @RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(opportunityService.createOpportunity(body, auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/admin/opportunities")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllAdmin() {
        return ResponseEntity.ok(opportunityService.getAllOpportunitiesAdmin());
    }

    @PutMapping("/admin/opportunities/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(opportunityService.updateOpportunity(id, body));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/admin/opportunities/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            opportunityService.deleteOpportunity(id);
            return ResponseEntity.ok(Map.of("message", "Opportunity deactivated."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/student/opportunities")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getForStudent(Authentication auth,
                                           @RequestParam(required = false) String type) {
        return ResponseEntity.ok(opportunityService.getStudentOpportunities(auth.getName(), type));
    }

    @GetMapping("/student/opportunities/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getDetail(@PathVariable Long id) {
        return opportunityService.getOpportunityById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
