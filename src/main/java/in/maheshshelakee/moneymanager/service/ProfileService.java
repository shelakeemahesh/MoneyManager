package in.maheshshelakee.moneymanager.service;

import in.maheshshelakee.moneymanager.dto.ForgotPasswordRequest;
import in.maheshshelakee.moneymanager.dto.LoginRequest;
import in.maheshshelakee.moneymanager.dto.LoginResponse;
import in.maheshshelakee.moneymanager.dto.ProfileDTO;
import in.maheshshelakee.moneymanager.entity.ProfileEntity;
import in.maheshshelakee.moneymanager.repository.ProfileRepository;
import in.maheshshelakee.moneymanager.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // Lazy injection to break circular dependency: ProfileService → CategoryService
    // → ProfileService
    @org.springframework.context.annotation.Lazy
    private final CategoryService categoryService;

    @Value("${app.base-url}")
    private String baseUrl;

    // ─── REGISTER ──────────────────────────────────────────────────────────────
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

        // Seed default categories for new user
        categoryService.createDefaults(newProfile);

        // Send activation email
        String activationLink = baseUrl + "/activate?token=" + newProfile.getActivationToken();
        String subject = "Activate your Money Manager account";
        String body = "Hi " + newProfile.getFullName() + ",\n\n"
                + "Click the link below to activate your account:\n"
                + activationLink + "\n\n"
                + "This link is valid until you request a new one.\n\n"
                + "– Money Manager Team";
        emailService.sendEmail(newProfile.getEmail(), subject, body);

        return toDTO(newProfile);
    }

    // ─── ACCOUNT ACTIVATION ────────────────────────────────────────────────────
    @Transactional
    public String activateAccount(String token) {
        ProfileEntity profile = profileRepository.findByActivationToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid or expired activation token"));

        if (Boolean.TRUE.equals(profile.getIsActive())) {
            return "Account already activated. You can log in.";
        }

        profile.setIsActive(true);
        profile.setActivationToken(null);
        profileRepository.save(profile);
        return "Account activated successfully! You can now log in.";
    }

    // ─── LOGIN ─────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        ProfileEntity profile = profileRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), profile.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        String token = jwtUtil.generateToken(email, profile.getRole().name(), profile.getStatus().name());
        return new LoginResponse(token, toDTO(profile));
    }

    // ─── FORGOT PASSWORD ───────────────────────────────────────────────────────
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        // Always respond OK — prevents email enumeration attacks
        profileRepository.findByEmail(request.getEmail().trim().toLowerCase()).ifPresent(profile -> {
            String resetToken = UUID.randomUUID().toString();
            profile.setActivationToken(resetToken);
            profileRepository.save(profile);

            String resetLink = baseUrl + "/reset-password?token=" + resetToken;
            String subject = "Reset your Money Manager password";
            String body = "Hi " + profile.getFullName() + ",\n\n"
                    + "Click the link below to reset your password:\n"
                    + resetLink + "\n\n"
                    + "If you did not request this, please ignore this email.\n\n"
                    + "– Money Manager Team";
            emailService.sendEmail(profile.getEmail(), subject, body);
        });
    }

    // ─── HELPERS ───────────────────────────────────────────────────────────────
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