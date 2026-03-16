package in.maheshshelakee.moneymanager.controller;

import in.maheshshelakee.moneymanager.dto.AdminDashboardResponse;
import in.maheshshelakee.moneymanager.dto.AdminUserDto;
import in.maheshshelakee.moneymanager.dto.UserStatusUpdateDTO;
import in.maheshshelakee.moneymanager.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// FIX: Added @PreAuthorize at class level — previously this controller had NO method-level
//      security, relying solely on path-level SecurityConfig. That is fragile; any mapping
//      change could silently expose endpoints.
@RestController
@RequestMapping("/admin/api")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/users")
    public ResponseEntity<Page<AdminUserDto>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllUsers(pageable));
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<Void> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody UserStatusUpdateDTO request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        String adminEmail = authentication.getName();
        String ipAddress = httpRequest.getRemoteAddr();

        adminService.updateUserStatus(userId, request.getStatus(), adminEmail, ipAddress);
        return ResponseEntity.noContent().build();
    }
}
