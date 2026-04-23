package in.maheshshelakee.moneymanager.controller;

import in.maheshshelakee.moneymanager.dto.AdminUserDto;
import in.maheshshelakee.moneymanager.dto.ApiResponse;
import in.maheshshelakee.moneymanager.dto.UserStatusUpdateDTO;
import in.maheshshelakee.moneymanager.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Unified admin user-management controller.
 * Replaces the former AdminController (/admin/api/users) and the old AdminUserController
 * that was directly injecting repositories. Now properly delegates to AdminService.
 */
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminService adminService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AdminUserDto>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminUserDto> users = adminService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminUserDto>> getUserById(@PathVariable Long id) {
        AdminUserDto user = adminService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<AdminUserDto>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusUpdateDTO requestDTO,
            Authentication authentication,
            HttpServletRequest request) {

        String adminEmail = authentication.getName();
        String ipAddress = request.getRemoteAddr();

        AdminUserDto updated = adminService.updateUserStatus(
                id, requestDTO.getStatus(), requestDTO.getReason(), adminEmail, ipAddress);

        return ResponseEntity.ok(ApiResponse.success(updated, "User status updated successfully"));
    }
}
