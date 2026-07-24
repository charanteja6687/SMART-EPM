package com.smartepm.service.impl;

import com.smartepm.dto.request.*;
import com.smartepm.dto.response.JwtResponse;
import com.smartepm.entity.Employee;
import com.smartepm.entity.Role;
import com.smartepm.entity.User;
import com.smartepm.exception.BadRequestException;
import com.smartepm.exception.DuplicateResourceException;
import com.smartepm.exception.ResourceNotFoundException;
import com.smartepm.repository.EmployeeRepository;
import com.smartepm.repository.UserRepository;
import com.smartepm.security.JwtTokenProvider;
import com.smartepm.security.UserPrincipal;
import com.smartepm.service.ActivityLogService;
import com.smartepm.service.AuthService;
import com.smartepm.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int OTP_VALIDITY_MINUTES = 5;

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final ActivityLogService activityLogService;
    private final EmailService emailService;

    @Override
    public JwtResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtTokenProvider.generateToken(authentication);
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        logger.info("User '{}' logged in successfully", principal.getUsername());
        activityLogService.log("LOGIN", "AUTH", principal.getId(), "User '" + principal.getUsername() + "' logged in");

        return JwtResponse.builder()
                .token(jwt)
                .type("Bearer")
                .userId(principal.getId())
                .username(principal.getUsername())
                .email(principal.getEmail())
                .role(principal.getAuthorities().iterator().next().getAuthority().replace("ROLE_", ""))
                .build();
    }

    @Override
    @Transactional
    public JwtResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email is already registered");
        }

        try {
            // No "Employee ID" is ever collected from the user. If registering as EMPLOYEE,
            // we automatically find an existing Employee record with a matching email
            // (e.g. one an admin already created), or transparently create one — so
            // registration always succeeds and never depends on the person knowing an
            // internal numeric ID.
            Employee employee = null;
            if (request.getRole() == Role.EMPLOYEE) {
                employee = findOrCreateEmployeeForRegistration(request);
            }

            User user = User.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(request.getRole())
                    .enabled(true)
                    .employee(employee)
                    .build();

            user = userRepository.save(user);
            logger.info("New user registered: '{}' with role '{}'", user.getUsername(), user.getRole());
            activityLogService.log("REGISTER", "AUTH", user.getId(),
                    "New user registered: '" + user.getUsername() + "' (" + user.getRole() + ")");

            String jwt = jwtTokenProvider.generateTokenFromUserId(user.getId(), user.getUsername());

            return JwtResponse.builder()
                    .token(jwt)
                    .type("Bearer")
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .build();

        } catch (DataIntegrityViolationException ex) {
            // Defense-in-depth: even though the logic above is written to avoid duplicate
            // rows, this guarantees registration NEVER surfaces a raw SQL/constraint error
            // to the user — it always comes back as a clean, readable message instead.
            logger.error("Registration failed due to a data integrity violation: {}", ex.getMessage());
            throw new DuplicateResourceException(
                    "Registration failed because this email or username conflicts with an existing record. Please try a different one.");
        }
    }

    /**
     * Finds an existing Employee by email (including soft-deleted ones, which get
     * reactivated), or creates a minimal new Employee record on the fly. This is what makes
     * EMPLOYEE registration "just work" without any manual Employee ID entry — an admin can
     * always go fill in the rest of the employee's details (department, designation, etc.)
     * afterwards from the Employees page.
     */
    private Employee findOrCreateEmployeeForRegistration(RegisterRequest request) {
        return employeeRepository.findByEmail(request.getEmail())
                .map(existing -> {
                    if (existing.getDeletedAt() != null) {
                        existing.setDeletedAt(null); // reactivate a previously soft-deleted employee
                        existing = employeeRepository.save(existing);
                        logger.info("Reactivated soft-deleted employee id={} during registration", existing.getId());
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    Employee created = Employee.builder()
                            .fullName(request.getUsername())
                            .email(request.getEmail())
                            .department("Unassigned")
                            .active(true)
                            .build();
                    created = employeeRepository.save(created);
                    logger.info("Auto-created employee id={} for new registration '{}'", created.getId(), request.getUsername());
                    return created;
                });
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with email: '" + request.getEmail() + "'"));

        String otp = generateOtp();
        user.setResetOtpCode(otp);
        user.setResetOtpExpiry(LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES));
        userRepository.save(user);

        emailService.sendOtpEmail(user.getEmail(), otp);
        logger.info("Password reset OTP generated for user '{}'", user.getUsername());
        activityLogService.log("FORGOT_PASSWORD", "AUTH", user.getId(), "OTP requested for '" + user.getUsername() + "'");
    }

    @Override
    public void verifyOtp(VerifyOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with email: '" + request.getEmail() + "'"));

        validateOtp(user, request.getOtp());
        // Valid — intentionally not consumed here; resetPassword() re-validates and consumes it.
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with email: '" + request.getEmail() + "'"));

        validateOtp(user, request.getOtp());

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetOtpCode(null);
        user.setResetOtpExpiry(null);
        userRepository.save(user);

        logger.info("Password reset successfully for user '{}'", user.getUsername());
        activityLogService.log("PASSWORD_RESET", "AUTH", user.getId(), "Password reset for '" + user.getUsername() + "'");
    }

    private void validateOtp(User user, String suppliedOtp) {
        if (user.getResetOtpCode() == null || user.getResetOtpExpiry() == null) {
            throw new BadRequestException("No password reset was requested for this account. Please request an OTP first.");
        }
        if (!user.getResetOtpCode().equals(suppliedOtp)) {
            throw new BadRequestException("Invalid OTP. Please check and try again.");
        }
        if (LocalDateTime.now().isAfter(user.getResetOtpExpiry())) {
            throw new BadRequestException("OTP has expired. Please request a new one.");
        }
    }

    private String generateOtp() {
        int otp = 100000 + RANDOM.nextInt(900000); // always exactly 6 digits
        return String.valueOf(otp);
    }
}
