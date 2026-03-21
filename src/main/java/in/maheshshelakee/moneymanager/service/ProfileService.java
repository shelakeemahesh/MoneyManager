package in.maheshshelakee.moneymanager.service;

import in.maheshshelakee.moneymanager.dto.ForgotPasswordRequest;
import in.maheshshelakee.moneymanager.dto.LoginRequest;
import in.maheshshelakee.moneymanager.dto.LoginResponse;
import in.maheshshelakee.moneymanager.dto.ProfileDTO;
import in.maheshshelakee.moneymanager.entity.CategoryEntity;
import in.maheshshelakee.moneymanager.entity.ProfileEntity;
import in.maheshshelakee.moneymanager.repository.CategoryRepository;
import in.maheshshelakee.moneymanager.repository.ProfileRepository;
import in.maheshshelakee.moneymanager.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final CategoryRepository categoryRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

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

        // Create default categories (no circular dependency)
        createDefaultCategories(newProfile);

        // Send activation email
        String activationLink = baseUrl + "/activate?token=" + newProfile.getActivationToken();

        String subject = "Activate your Money Manager account";
        String body = "Hi " + newProfile.getFullName() + ",\n\n"
                + "Click the link below to activate your account:\n"
                + activationLink + "\n\n"
                + "– Money Manager Team";

        emailService.sendEmail(newProfile.getEmail(), subject, body);

        return toDTO(newProfile);
    }

    // ─── DEFAULT CATEGORIES ─────────────────────────────────────────────────────
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createDefaultCategories(ProfileEntity profile) {

        List<Object[]> defaults = List.of(
                new Object[]{"Salary","INCOME","💼","#22c55e"},
                new Object[]{"Freelance","INCOME","🖥️","#10b981"},
                new Object[]{"Investments","INCOME","📈","#06b6d4"},
                new Object[]{"Other Income","INCOME","💰","#6366f1"},
                new Object[]{"Food","EXPENSE","🍔","#ef4444"},
                new Object[]{"Transport","EXPENSE","🚗","#f97316"},
                new Object[]{"Shopping","EXPENSE","🛍️","#8b5cf6"},
                new Object[]{"Health","EXPENSE","🏥","#ec4899"},
                new Object[]{"Utilities","EXPENSE","💡","#f59e0b"},
                new Object[]{"Entertainment","EXPENSE","🎬","#14b8a6"},
                new Object[]{"Education","EXPENSE","📚","#3b82f6"},
                new Object[]{"Other","EXPENSE","📦","#6b7280"}
        );

        List<CategoryEntity> entities = defaults.stream()
                .map(row -> CategoryEntity.builder()
                        .name((String) row[0])
                        .type((String) row[1])
                        .icon((String) row[2])
                        .color((String) row[3])
                        .profile(profile)
                        .build())
                .toList();

        categoryRepository.saveAll(entities);
    }

    // ─── ACCOUNT ACTIVATION ────────────────────────────────────────────────────
    @Transactional
    public String activateAccount(String token) {
        ProfileEntity profile = profileRepository.findByActivationToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid or expired activation token"));

        if (Boolean.TRUE.equals(profile.getIsActive())) {
            return "Account already activated.";
        }

        profile.setIsActive(true);
        profile.setActivationToken(null);
        profileRepository.save(profile);

        return "Account activated successfully!";
    }

    // ─── LOGIN ─────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        ProfileEntity profile = profileRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), profile.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        if (!Boolean.TRUE.equals(profile.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account not activated");
        }

        String token = jwtUtil.generateToken(
                email,
                profile.getRole().name(),
                profile.getStatus().name()
        );

        return new LoginResponse(token, toDTO(profile));
    }

    // ─── FORGOT PASSWORD ───────────────────────────────────────────────────────
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {

        profileRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .ifPresent(profile -> {

                    String token = UUID.randomUUID().toString();
                    profile.setActivationToken(token);
                    profileRepository.save(profile);

                    String resetLink = baseUrl + "/reset-password?token=" + token;

                    String subject = "Reset Password";
                    String body = "Reset your password:\n" + resetLink;

                    emailService.sendEmail(profile.getEmail(), subject, body);
                });
    }

    // ─── HELPER ────────────────────────────────────────────────────────────────
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