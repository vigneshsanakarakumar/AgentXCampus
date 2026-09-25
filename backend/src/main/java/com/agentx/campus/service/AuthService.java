package com.agentx.campus.service;

import com.agentx.campus.agent.NotificationAgent;
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
import java.util.HashMap;
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
    private final UserRegistrationRequestRepository userRegistrationRequestRepository;
    private final HodProfileRepository hodProfileRepository;
    private final NotificationAgent notificationAgent;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       StudentProfileRepository studentProfileRepository,
                       FacultyProfileRepository facultyProfileRepository,
                       StaffLoginRequestRepository staffLoginRequestRepository,
                       UserRegistrationRequestRepository userRegistrationRequestRepository,
                       HodProfileRepository hodProfileRepository,
                       NotificationAgent notificationAgent,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.facultyProfileRepository = facultyProfileRepository;
        this.staffLoginRequestRepository = staffLoginRequestRepository;
        this.userRegistrationRequestRepository = userRegistrationRequestRepository;
        this.hodProfileRepository = hodProfileRepository;
        this.notificationAgent = notificationAgent;
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

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Incorrect username/email or password. Please try again.");
        }

        if (!user.isActive()) {
            Optional<UserRegistrationRequest> regOpt = userRegistrationRequestRepository.findByUser(user);
            if (regOpt.isEmpty()) {
                regOpt = userRegistrationRequestRepository.findByUsername(user.getUsername());
            }
            if (regOpt.isPresent()) {
                UserRegistrationRequest reg = regOpt.get();
                if ("PENDING".equalsIgnoreCase(reg.getStatus())) {
                    throw new AccountPendingApprovalException(
                        "Your registration request as " + reg.getRole() + " in " + reg.getDepartment() +
                        " is currently pending verification and approval by your department HOD. Please wait until your HOD approves your account before logging in."
                    );
                } else if ("REJECTED".equalsIgnoreCase(reg.getStatus())) {
                    throw new AccountPendingApprovalException(
                        "Your registration request was not approved by your department HOD. Reason: " +
                        (reg.getRejectionReason() != null ? reg.getRejectionReason() : "Eligibility criteria not satisfied.")
                    );
                }
            }
            if (user.getRole() == Role.FACULTY) {
                throw new AccountPendingApprovalException(
                    "Your faculty account is pending activation or administrator approval. Please activate your account via your invitation link."
                );
            }
            throw new BadCredentialsException("Your account is currently inactive. Please contact your campus administrator or department HOD.");
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
    public Map<String, Object> registerUser(UserRegistrationDto request) {
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

        Role requestedRole = request.getRole() != null ? request.getRole() : Role.STUDENT;
        if (requestedRole != Role.STUDENT && requestedRole != Role.FACULTY) {
            throw new IllegalArgumentException("Registration is only permitted for STUDENT or FACULTY roles.");
        }

        String username = request.getUsername().trim().toLowerCase();
        String email = request.getEmail().trim().toLowerCase();
        String department = request.getDepartment().trim();

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email '" + email + "' is already registered.");
        }

        String rollNumber = null;
        String section = "A";
        Integer year = 1;
        Integer semester = 1;

        if (requestedRole == Role.STUDENT) {
            if (request.getRollNumber() == null || request.getRollNumber().trim().isEmpty()) {
                throw new IllegalArgumentException("Roll Number is required for student registration.");
            }
            rollNumber = request.getRollNumber().trim().toUpperCase();
            if (studentProfileRepository.existsByRollNumber(rollNumber)) {
                throw new IllegalArgumentException("Roll Number '" + rollNumber + "' is already registered.");
            }
            if (request.getSection() != null && !request.getSection().trim().isEmpty()) {
                section = request.getSection().trim().toUpperCase();
            }
            if (request.getYear() != null && request.getYear() >= 1 && request.getYear() <= 4) {
                year = request.getYear();
            }
            if (request.getSemester() != null && request.getSemester() >= 1 && request.getSemester() <= 8) {
                semester = request.getSemester();
            }
        } else {
            // FACULTY validation
            if (request.getDesignation() == null || request.getDesignation().trim().isEmpty()) {
                throw new IllegalArgumentException("Designation is required for faculty registration (e.g. Assistant Professor).");
            }
        }

        // 1. Create User with isActive = false (Waiting for HOD approval)
        User user = new User(
                username,
                email,
                passwordEncoder.encode(request.getPassword()),
                requestedRole,
                request.getFirstName().trim(),
                request.getLastName().trim()
        );
        user.setActive(false);
        user = userRepository.save(user);

        // 2. Create Profile
        if (requestedRole == Role.STUDENT) {
            StudentProfile profile = new StudentProfile();
            profile.setUser(user);
            profile.setRollNumber(rollNumber);
            profile.setDepartment(department);
            profile.setSection(section);
            profile.setYear(year);
            profile.setSemester(semester);
            profile.setCgpa(0.0);
            profile.setAttendanceRate(100);
            studentProfileRepository.save(profile);
        } else {
            FacultyProfile fp = new FacultyProfile();
            fp.setUser(user);
            fp.setEmployeeId("EMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            fp.setDepartment(department);
            fp.setDesignation(request.getDesignation().trim());
            fp.setSpecialization(request.getSpecialization() != null ? request.getSpecialization().trim() : null);
            fp.setCabinNumber(request.getCabinNumber() != null ? request.getCabinNumber().trim() : "Faculty Cabin");
            fp.setAssignedDepartment(department);
            fp.setAssignedSection(null);
            fp.setMentor(false); // Subject staff by default
            facultyProfileRepository.save(fp);
        }

        // 3. Resolve department HOD user
        User hodUser = resolveDepartmentHodUser(department);

        // 4. Create UserRegistrationRequest record
        UserRegistrationRequest regReq = new UserRegistrationRequest();
        regReq.setUser(user);
        regReq.setUsername(username);
        regReq.setEmail(email);
        regReq.setFirstName(request.getFirstName().trim());
        regReq.setLastName(request.getLastName().trim());
        regReq.setRole(requestedRole);
        regReq.setDepartment(department);
        regReq.setStatus("PENDING");
        regReq.setHodUser(hodUser);

        if (requestedRole == Role.STUDENT) {
            regReq.setRollNumber(rollNumber);
            regReq.setSection(section);
            regReq.setYear(year);
            regReq.setSemester(semester);
        } else {
            regReq.setDesignation(request.getDesignation().trim());
            regReq.setSpecialization(request.getSpecialization() != null ? request.getSpecialization().trim() : null);
            regReq.setCabinNumber(request.getCabinNumber() != null ? request.getCabinNumber().trim() : null);
        }

        regReq = userRegistrationRequestRepository.save(regReq);

        // 5. Notify HOD
        if (hodUser != null) {
            String roleName = requestedRole == Role.FACULTY ? "Faculty Member" : "Student";
            String title = "New " + roleName + " Registration Request";
            String msg = request.getFirstName() + " " + request.getLastName() + " (" + username + ") has registered for "
                    + department + " and is awaiting your verification and approval.";
            notificationAgent.notifyUser(hodUser, title, msg, "REGISTRATION_REQUEST", "Department HOD for " + department);
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("requestId", regReq.getId());
        resp.put("username", username);
        resp.put("email", email);
        resp.put("role", requestedRole.name());
        resp.put("department", department);
        resp.put("status", "PENDING_APPROVAL");
        resp.put("message", "Registration submitted successfully! Your account is currently pending verification and approval by your Department Head of Department ("
                + department + "). You will be able to log in once your HOD approves your account.");
        return resp;
    }

    private User resolveDepartmentHodUser(String department) {
        Optional<HodProfile> hp = hodProfileRepository.findByDepartment(department);
        if (hp.isPresent() && hp.get().getUser() != null) {
            return hp.get().getUser();
        }
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.HOD)
                .findFirst()
                .orElse(null);
    }

    @Transactional
    public UserDto registerStudent(RegisterRequest request) {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setRole(Role.STUDENT);
        dto.setFirstName(request.getFirstName());
        dto.setLastName(request.getLastName());
        dto.setUsername(request.getUsername());
        dto.setEmail(request.getEmail());
        dto.setPassword(request.getPassword());
        dto.setConfirmPassword(request.getConfirmPassword());
        dto.setDepartment(request.getDepartment());
        dto.setSection(request.getSection());
        dto.setRollNumber(request.getRollNumber());
        dto.setYear(1);
        dto.setSemester(1);

        Map<String, Object> res = registerUser(dto);
        User user = userRepository.findByUsername((String) res.get("username")).orElseThrow();

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
