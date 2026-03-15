package in.maheshshelakee.moneymanager.controller;

import in.maheshshelakee.moneymanager.dto.ProfileDTO;
import in.maheshshelakee.moneymanager.dto.UserStatusUpdateDTO;
import in.maheshshelakee.moneymanager.entity.ProfileEntity;
import in.maheshshelakee.moneymanager.repository.ProfileRepository;
import in.maheshshelakee.moneymanager.service.AdminAuditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final ProfileRepository profileRepository;
    private final AdminAuditService adminAuditService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ProfileDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProfileDTO> users = profileRepository.findAll(pageable).map(this::toDTO);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfileDTO> getUserById(@PathVariable Long id) {
        ProfileEntity user = profileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return ResponseEntity.ok(toDTO(user));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfileDTO> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusUpdateDTO requestDTO,
            Authentication authentication,
            HttpServletRequest request) {

        ProfileEntity user = profileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        
        String oldStatus = user.getStatus() != null ? user.getStatus().name() : "null";
        user.setStatus(requestDTO.getStatus());
        profileRepository.save(user);

        String adminEmail = authentication.getName();
        String details = "Status changed from " + oldStatus + " to " + requestDTO.getStatus().name() 
                + ". Reason: " + requestDTO.getReason();

        adminAuditService.logAction(adminEmail, user.getId(), "USER_STATUS_UPDATED", details, request.getRemoteAddr());

        return ResponseEntity.ok(toDTO(user));
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
