package in.maheshshelakee.moneymanager.controller;

import in.maheshshelakee.moneymanager.dto.ForgotPasswordRequest;
import in.maheshshelakee.moneymanager.dto.LoginRequest;
import in.maheshshelakee.moneymanager.dto.LoginResponse;
import in.maheshshelakee.moneymanager.dto.ProfileDTO;
import in.maheshshelakee.moneymanager.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    // POST /register
    @PostMapping("/register")
    public ResponseEntity<ProfileDTO> registerProfile(@Valid @RequestBody ProfileDTO profileDTO) {
        ProfileDTO registered = profileService.registerProfile(profileDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(registered);
    }

    // POST /login
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = profileService.login(request);
        return ResponseEntity.ok(response);
    }

    // GET /activate?token=
    @GetMapping("/activate")
    public ResponseEntity<Map<String, String>> activateAccount(@RequestParam String token) {
        String message = profileService.activateAccount(token);
        return ResponseEntity.ok(Map.of("message", message));
    }

    // POST /forgot-password
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        profileService.forgotPassword(request);
        return ResponseEntity.ok(Map.of("message", "If that email exists, a reset link has been sent."));
    }

    // POST /reset-password
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody in.maheshshelakee.moneymanager.dto.ResetPasswordRequest request) {
        profileService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Password reset successfully."));
    }
}