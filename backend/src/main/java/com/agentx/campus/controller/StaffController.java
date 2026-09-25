package com.agentx.campus.controller;

import com.agentx.campus.service.StaffService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/staff")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(Authentication authentication) {
        return ResponseEntity.ok(staffService.getStaffDashboard(authentication.getName()));
    }
}
