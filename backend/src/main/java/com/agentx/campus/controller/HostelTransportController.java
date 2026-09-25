package com.agentx.campus.controller;

import com.agentx.campus.model.*;
import com.agentx.campus.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Hostel + Transport endpoints.
 * Student: /api/v1/student/hostel-info, /student/bus-info, /student/gate-pass, /student/bus-routes
 * Admin:   /api/v1/admin/hostel/*, /admin/transport/*
 */
@RestController
@RequestMapping("/api/v1")
public class HostelTransportController {

    private final HostelTransportService hostelService;

    public HostelTransportController(HostelTransportService hostelService) {
        this.hostelService = hostelService;
    }

    // ─── STUDENT ─────────────────────────────────────────────────────────────

    @GetMapping("/student/hostel-info")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getHostelInfo(Authentication auth) {
        return ResponseEntity.ok(hostelService.getStudentHostelInfo(auth.getName()));
    }

    @GetMapping("/student/bus-info")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getBusInfo(Authentication auth) {
        return ResponseEntity.ok(hostelService.getStudentBusInfo(auth.getName()));
    }

    @PostMapping("/student/gate-pass")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> submitGatePass(Authentication auth, @RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(hostelService.submitGatePass(auth.getName(), body));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/student/gate-pass")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getGatePasses(Authentication auth) {
        return ResponseEntity.ok(hostelService.getStudentGatePasses(auth.getName()));
    }

    @GetMapping("/student/bus-routes")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getBusRoutes() {
        return ResponseEntity.ok(hostelService.getAllRoutes());
    }

    // ─── ADMIN ───────────────────────────────────────────────────────────────

    @GetMapping("/admin/hostel/blocks")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllBlocks() {
        return ResponseEntity.ok(hostelService.getAllBlocks());
    }

    @GetMapping("/admin/hostel/gate-passes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllGatePasses() {
        return ResponseEntity.ok(hostelService.getAllGatePasses());
    }

    @PatchMapping("/admin/hostel/gate-passes/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateGatePassStatus(Authentication auth,
                                                   @PathVariable Long id,
                                                   @RequestBody Map<String, String> body) {
        try {
            String status = body.getOrDefault("status", "APPROVED");
            return ResponseEntity.ok(hostelService.updateGatePassStatus(id, status, auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/admin/transport/routes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllRoutesAdmin() {
        return ResponseEntity.ok(hostelService.getAllRoutes());
    }

    @GetMapping("/admin/transport/routes/{id}/stops")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getRouteStops(@PathVariable Long id) {
        return ResponseEntity.ok(hostelService.getRouteStops(id));
    }
}
