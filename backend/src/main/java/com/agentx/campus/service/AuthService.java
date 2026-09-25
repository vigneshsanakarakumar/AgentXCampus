package com.agentx.campus.service;

import com.agentx.campus.dto.*;
import com.agentx.campus.exception.AccountPendingApprovalException;
import com.agentx.campus.model.*;
import com.agentx.campus.repository.*;
import com.agentx.campus.security.JwtTokenProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final FacultyProfileRepository facultyProfileRepository;
    private final StaffLoginRequestRepository staffLoginRequestRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       StudentProfileRepository studentProfileRepository,
                       FacultyProfileRepository facultyProfileRepository,
                       StaffLoginRequestRepository staffLoginRequestRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.facultyProfileRepository = facultyProfileRepository;
        this.staffLoginRequestRepository = staffLoginRequestRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    public LoginResponse login(LoginRequest request) {
        if (request.getIdentifier() == null || request.getPassword() == null) {
            throw new BadCredentialsException("Username/email and password are required.");
        }

        String identifier = request.getIdentifier().trim();

        // 1. Check if user is a prospective staff member with a pending or unactivated request
        Optional<StaffLoginRequest> staffReq = staffLoginRequestRepository.findByEmail(identifier);
        if (staffReq.isPresent()) {
            StaffLoginRequest sr = staffReq.get();
            if ("PENDING".equalsIgnoreCase(sr.getStatus())) {
                throw new AccountPendingApprovalException(
                    "Your staff login request is currently pending administrative approval. You cannot log in until an administrator approves your account and you activate your invitation."
                );
            } else if ("REJECTED".equalsIgnoreCase(sr.getStatus())) {
                throw new AccountPendingApprovalException(
                    "Your staff registration request was rejected by campus administration. Reason: " +
                    (sr.getRejectionReason() != null ? sr.getRejectionReason() : "Eligibility requirements not met.")
                );
            } else if ("APPROVED".equalsIgnoreCase(sr.getStatus())) {
                throw new AccountPendingApprovalException(
                    "Your staff account request has been approved! Please use the invitation link sent to you to set your password and activate your account before logging in."
                );
            }
        }

        User user = userRepository.findByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() -> new BadCredentialsException("Incorrect username/email or password. Please try again."));

        if (!user.isActive()) {
            if (user.getRole() == Role.FACULTY) {
                throw new AccountPendingApprovalException(
                    "Your faculty account is pending activation or administrator approval. Please activate your account via your invitation link."
                );
            }
            throw new BadCredentialsException("Your account is currently inactive. Please contact your campus administrator.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Incorrect username/email or password. Please try again.");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String token = tokenProvider.generateToken(user.getUsername(), user.getRole(), user.getId());
        String refreshToken = tokenProvider.generateRefreshToken(user.getUsername());

        return new LoginResponse(
                token,
                refreshToken,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getFirstName(),
                user.getLastName()
        );
    }

    @Transactional
    public UserDto registerStudent(RegisterRequest request) {
        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required.");
        }
        if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required.");
        }
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }
        if (request.getDepartment() == null || request.getDepartment().trim().isEmpty()) {
            throw new IllegalArgumentException("Department is required.");
        }
        if (request.getSection() == null || request.getSection().trim().isEmpty()) {
            throw new IllegalArgumentException("Section is required (e.g. A, B, C).");
        }
        if (request.getRollNumber() == null || request.getRollNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Roll Number is required.");
        }

        String username = request.getUsername().trim().toLowerCase();
        String email = request.getEmail().trim().toLowerCase();
        String rollNumber = request.getRollNumber().trim().toUpperCase();
        String department = request.getDepartment().trim();
        String section = request.getSection().trim().toUpperCase();

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username is already taken.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered.");
        }
        if (studentProfileRepository.existsByRollNumber(rollNumber)) {
            throw new IllegalArgumentException("Roll Number " + rollNumber + " is already registered.");
        }

        User user = new User(
                username,
                email,
                passwordEncoder.encode(request.getPassword()),
                Role.STUDENT,
                request.getFirstName().trim(),
                request.getLastName().trim()
        );
        user = userRepository.save(user);

        StudentProfile profile = new StudentProfile();
        profile.setUser(user);
        profile.setRollNumber(rollNumber);
        profile.setDepartment(department);
        profile.setSection(section);
        profile.setYear(1);
        profile.setSemester(1);
        profile.setCgpa(0.0);
        profile.setAttendanceRate(100);
        studentProfileRepository.save(profile);

        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getFirstName(),
                user.getLastName(),
                user.getAvatarUrl(),
                user.isActive()
        );
    }

    @Transactional
    public UserDto signup(SignupRequest request) {
        // Fallback for general signup
        RegisterRequest regReq = new RegisterRequest();
        regReq.setFirstName(request.getFirstName());
        regReq.setLastName(request.getLastName());
        regReq.setUsername(request.getUsername());
        regReq.setEmail(request.getEmail());
        regReq.setPassword(request.getPassword());
        regReq.setConfirmPassword(request.getConfirmPassword());
        regReq.setDepartment("Computer Science & Engineering");
        regReq.setSection("A");
        regReq.setRollNumber("ROLL-" + System.currentTimeMillis() % 100000);
        return registerStudent(regReq);
    }

    public UserDto getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getFirstName(),
                user.getLastName(),
                user.getAvatarUrl(),
                user.isActive()
        );
    }

    public String forgotPassword(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email != null ? email.trim() : "");
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken(token, user, LocalDateTime.now().plusHours(2));
            passwordResetTokenRepository.save(resetToken);
            return token;
        }
        return null;
    }

    @Transactional
    public boolean resetPassword(ResetPasswordRequest request) {
        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired password reset token."));

        if (resetToken.isUsed() || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("This password reset link has expired or has already been used.");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
        return true;
    }

    @Transactional
    public StaffLoginRequest submitStaffRequest(StaffRequestDto dto) {
        if (dto.getFirstName() == null || dto.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required.");
        }
        if (dto.getLastName() == null || dto.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required.");
        }
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (dto.getDepartment() == null || dto.getDepartment().trim().isEmpty()) {
            throw new IllegalArgumentException("Department is required.");
        }
        if (dto.getDesignation() == null || dto.getDesignation().trim().isEmpty()) {
            throw new IllegalArgumentException("Designation is required.");
        }

        String email = dto.getEmail().trim();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("An active campus account with this email address already exists.");
        }

        Optional<StaffLoginRequest> existing = staffLoginRequestRepository.findByEmail(email);
        if (existing.isPresent()) {
            StaffLoginRequest req = existing.get();
            if ("PENDING".equalsIgnoreCase(req.getStatus())) {
                throw new IllegalArgumentException("A staff registration request for " + email + " is already pending review.");
            } else if ("APPROVED".equalsIgnoreCase(req.getStatus())) {
                throw new IllegalArgumentException("Your request has already been approved! Please check your invitation link.");
            }
        }

        StaffLoginRequest req = new StaffLoginRequest(
                dto.getFirstName().trim(),
                dto.getLastName().trim(),
                email,
                dto.getDepartment().trim(),
                dto.getDesignation().trim()
        );
        return staffLoginRequestRepository.save(req);
    }

    public StaffLoginRequest verifyStaffInviteToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Invitation token is required.");
        }

        StaffLoginRequest req = staffLoginRequestRepository.findByInviteToken(token.trim())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or unrecognized invitation token."));

        if ("ACTIVATED".equalsIgnoreCase(req.getStatus())) {
            throw new IllegalArgumentException("This invitation has already been used and activated.");
        }

        if (req.getTokenExpiry() != null && req.getTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("This invitation link has expired. Please contact your administrator for a new invite.");
        }

        return req;
    }

    @Transactional
    public Map<String, Object> activateStaffAccount(StaffActivationDto dto) {
        if (dto.getToken() == null || dto.getToken().trim().isEmpty()) {
            throw new IllegalArgumentException("Invitation token is required.");
        }
        if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }

        StaffLoginRequest req = verifyStaffInviteToken(dto.getToken());

        String username = dto.getUsername().trim();
        Optional<User> existingUserOpt = userRepository.findByUsername(username);

        User user;
        if (existingUserOpt.isPresent()) {
            user = existingUserOpt.get();
            if (!user.getEmail().equalsIgnoreCase(req.getEmail())) {
                throw new IllegalArgumentException("Username '" + username + "' is already taken. Please choose another username.");
            }
            user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
            user.setActive(true);
            user.setRole(Role.FACULTY);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        } else {
            // Check if user with that email already exists
            Optional<User> byEmail = userRepository.findByEmail(req.getEmail());
            if (byEmail.isPresent()) {
                user = byEmail.get();
                user.setUsername(username);
                user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
                user.setActive(true);
                user.setRole(Role.FACULTY);
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
            } else {
                user = new User(
                        username,
                        req.getEmail(),
                        passwordEncoder.encode(dto.getPassword()),
                        Role.FACULTY,
                        req.getFirstName(),
                        req.getLastName()
                );
                user.setActive(true);
                user = userRepository.save(user);
            }
        }

        // Create or update FacultyProfile
        Optional<FacultyProfile> fpOpt = facultyProfileRepository.findByUser(user);
        FacultyProfile fp = fpOpt.orElse(new FacultyProfile());
        fp.setUser(user);
        fp.setDepartment(req.getDepartment());
        fp.setDesignation(req.getDesignation());
        if (fp.getEmployeeId() == null) {
            fp.setEmployeeId("FAC-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        }
        facultyProfileRepository.save(fp);

        req.setStatus("ACTIVATED");
        req.setApprovedAt(LocalDateTime.now());
        req.setUpdatedAt(LocalDateTime.now());
        staffLoginRequestRepository.save(req);

        return Map.of(
                "message", "Staff account successfully activated! You can now log in.",
                "username", user.getUsername(),
                "role", user.getRole().name()
        );
    }
}
