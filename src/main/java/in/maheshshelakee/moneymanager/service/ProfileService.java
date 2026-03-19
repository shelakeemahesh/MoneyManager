package in.maheshshelakee.moneymanager.service;

import in.maheshshelakee.moneymanager.dto.*;
import in.maheshshelakee.moneymanager.entity.ProfileEntity;
import in.maheshshelakee.moneymanager.entity.UserStatus;
import in.maheshshelakee.moneymanager.event.UserRegisteredEvent;
import in.maheshshelakee.moneymanager.repository.ProfileRepository;
import in.maheshshelakee.moneymanager.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.base-url}")
    private String baseUrl;

    // ─── REGISTER ─────────────────────────────────────────────────────────────
    @Transactional
    public ProfileDTO registerProfile(ProfileDTO profileDTO) {
        String email = profileDTO.getEmail().trim().toLowerCase();

        if (profileRepository.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        ProfileEntity newProfile = ProfileEntity.builder()
                .fullName(profileDTO.getFullName().trim())
                .email(email)
                .password(passwordEncoder.encode(profileDTO.getPassword()))
                .profileImageUrl(profileDTO.getProfileImageUrl())
                .activationToken(UUID.randomUUID().toString())
                .build();

        newProfile = profileRepository.save(newProfile);

        // Publish event so CategoryService seeds default categories
        // (avoids circular dependency between ProfileService ↔ CategoryService)
        eventPublisher.publishEvent(new UserRegisteredEvent(this, newProfile));

        String activationLink = baseUrl + "/activate?token=" + newProfile.getActivationToken();
        emailService.sendEmail(
                newProfile.getEmail(),
                "Activate your Money Manager account",
                "Hi " + newProfile.getFullName() + ",\n\n"
                        + "Click the link below to activate your account:\n"
                        + activationLink + "\n\n– Money Manager Team");

        return toDTO(newProfile);
    }

    // ─── ACTIVATE ─────────────────────────────────────────────────────────────
    @Transactional
    public String activateAccount(String token) {
        ProfileEntity profile = profileRepository.findByActivationToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid or expired activation token"));

        if (Boolean.TRUE.equals(profile.getIsActive())) {
            return "Account already activated";
        }

        profile.setIsActive(true);
        profile.setActivationToken(null);
        profileRepository.save(profile);
        return "Account activated successfully";
    }

    // ─── LOGIN ────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        ProfileEntity profile = profileRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), profile.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        if (profile.getStatus() == UserStatus.BANNED || profile.getStatus() == UserStatus.SUSPENDED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Account is " + profile.getStatus().name().toLowerCase());
        }

        if (!Boolean.TRUE.equals(profile.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Account not activated. Please check your email.");
        }

        String token = jwtUtil.generateToken(email, profile.getRole().name(), profile.getStatus().name());
        return new LoginResponse(token, toDTO(profile));
    }

    // ─── FORGOT PASSWORD ──────────────────────────────────────────────────────
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        // Always respond the same way to prevent user enumeration attacks
        profileRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .ifPresent(profile -> {
                    String token = UUID.randomUUID().toString();
                    // Use dedicated resetToken field — never reuse activationToken
                    profile.setResetToken(token);
                    profile.setResetTokenExpiresAt(LocalDateTime.now().plusHours(1));
                    profileRepository.save(profile);

                    String link = baseUrl + "/reset-password?token=" + token;
                    emailService.sendEmail(
                            profile.getEmail(),
                            "Reset your Money Manager password",
                            "Click the link below to reset your password (valid for 1 hour):\n"
                                    + link + "\n\nIf you didn't request this, ignore this email.");
                });
    }

    // ─── RESET PASSWORD ───────────────────────────────────────────────────────
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        ProfileEntity profile = profileRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid or expired reset token"));

        if (profile.getResetTokenExpiresAt() == null
                || profile.getResetTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Reset token has expired. Please request a new one.");
        }

        profile.setPassword(passwordEncoder.encode(request.getNewPassword()));
        profile.setResetToken(null);
        profile.setResetTokenExpiresAt(null);
        profileRepository.save(profile);
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public ProfileEntity getProfileByEmail(String email) {
        return profileRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private ProfileDTO toDTO(ProfileEntity entity) {
        return ProfileDTO.builder()
                .id(entity.getId())
                .fullName(entity.getFullName())
                .email(entity.getEmail())
                .profileImageUrl(entity.getProfileImageUrl())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}