package in.maheshshelakee.moneymanager.controller;

import in.maheshshelakee.moneymanager.dto.AdminUserDto;
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
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final ProfileRepository profileRepository;
    private final AdminAuditService adminAuditService;

    @GetMapping
    public ResponseEntity<Page<AdminUserDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        // FIX: Was mapping to ProfileDTO (which lacks status/isActive). Now uses AdminUserDto
        //      so callers always see the user's current status.
        Page<AdminUserDto> users = profileRepository.findAll(pageable).map(this::toDto);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminUserDto> getUserById(@PathVariable Long id) {
        ProfileEntity user = profileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        // FIX: Return AdminUserDto (includes status) instead of ProfileDTO
        return ResponseEntity.ok(toDto(user));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<AdminUserDto> updateUserStatus(
            @PathVariable Long id,
            // FIX: Using UserStatusUpdateDTO (has both status + reason) consistently; removed
            //      the duplicate UpdateUserStatusRequest which only had status and no reason.
            @Valid @RequestBody UserStatusUpdateDTO requestDTO,
            Authentication authentication,
            HttpServletRequest request) {

        ProfileEntity user = profileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String oldStatus = user.getStatus() != null ? user.getStatus().name() : "null";
        user.setStatus(requestDTO.getStatus());

        // Keep isActive flag consistent with status
        if (requestDTO.getStatus() != null) {
            switch (requestDTO.getStatus()) {
                case BANNED, SUSPENDED -> user.setIsActive(false);
                case ACTIVE -> user.setIsActive(true);
            }
        }
        profileRepository.save(user);

        String adminEmail = authentication.getName();
        String details = "Status changed from " + oldStatus + " to " + requestDTO.getStatus().name()
                + ". Reason: " + requestDTO.getReason();

        adminAuditService.logAction(adminEmail, user.getId(), "USER_STATUS_UPDATED", details, request.getRemoteAddr());

        return ResponseEntity.ok(toDto(user));
    }

    // FIX: Replaced ProfileDTO mapping with AdminUserDto which exposes status and isActive
    private AdminUserDto toDto(ProfileEntity entity) {
        return AdminUserDto.builder()
                .id(entity.getId())
                .fullName(entity.getFullName())
                .email(entity.getEmail())
                .createdAt(entity.getCreatedAt())
                .isActive(entity.getIsActive())
                .status(entity.getStatus())
                .build();
    }
}
