package in.maheshshelakee.moneymanager.controller;

import in.maheshshelakee.moneymanager.dto.ProfileDTO;
import in.maheshshelakee.moneymanager.dto.UserStatusUpdateDTO;
import in.maheshshelakee.moneymanager.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminService adminService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ProfileDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminService.getAllUsersAsDTO(page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfileDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfileDTO> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusUpdateDTO requestDTO,
            Authentication authentication,
            HttpServletRequest request) {

        ProfileDTO updated = adminService.updateUserStatus(
                id,
                requestDTO.getStatus(),
                authentication.getName(),
                request.getRemoteAddr(),
                requestDTO.getReason());
        return ResponseEntity.ok(updated);
    }
}
