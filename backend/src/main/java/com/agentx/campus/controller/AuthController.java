package com.agentx.campus.controller;

import com.agentx.campus.dto.*;
import com.agentx.campus.model.StaffLoginRequest;
import com.agentx.campus.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (com.agentx.campus.exception.AccountPendingApprovalException ex) {
            return ResponseEntity.status(403).body(Map.of("message", ex.getMessage(), "status", "PENDING_APPROVAL"));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/staff-request")
    public ResponseEntity<?> submitStaffRequest(@RequestBody StaffRequestDto request) {
        try {
            StaffLoginRequest req = authService.submitStaffRequest(request);
            return ResponseEntity.ok(Map.of(
                    "message", "Your staff access request has been submitted for administrative review.",
                    "id", req.getId(),
                    "requestId", req.getId(),
                    "email", req.getEmail(),
                    "status", req.getStatus()
            ));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping("/verify-staff-invite")
    public ResponseEntity<?> verifyStaffInvite(@RequestParam String token) {
        try {
            StaffLoginRequest req = authService.verifyStaffInviteToken(token);
            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "email", req.getEmail(),
                    "firstName", req.getFirstName(),
                    "lastName", req.getLastName(),
                    "department", req.getDepartment(),
                    "designation", req.getDesignation(),
                    "status", req.getStatus()
            ));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/activate-staff")
    public ResponseEntity<?> activateStaff(@RequestBody StaffActivationDto request) {
        try {
            Map<String, Object> result = authService.activateStaffAccount(request);
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            UserDto user = authService.registerStudent(request);
            return ResponseEntity.ok(user);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody RegisterRequest request) {
        try {
            UserDto user = authService.registerStudent(request);
            return ResponseEntity.ok(user);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of("message", "Logged out successfully."));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh() {
        return ResponseEntity.ok(Map.of("message", "Token refreshed."));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        String token = authService.forgotPassword(request.getEmail());
        if (token != null) {
            return ResponseEntity.ok(Map.of(
                "message", "If an account exists for this email, password reset instructions have been sent.",
                "devResetToken", token,
                "devResetUrl", "/reset-password?token=" + token
            ));
        }
        return ResponseEntity.ok(Map.of("message", "If an account exists for this email, password reset instructions have been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(request);
            return ResponseEntity.ok(Map.of("message", "Password updated successfully."));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail() {
        return ResponseEntity.ok(Map.of("message", "Email verified successfully."));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthenticated"));
        }
        try {
            UserDto user = authService.getCurrentUser(authentication.getName());
            return ResponseEntity.ok(user);
        } catch (Exception ex) {
            return ResponseEntity.status(404).body(Map.of("message", ex.getMessage()));
        }
    }
}
