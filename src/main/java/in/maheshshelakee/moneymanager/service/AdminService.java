package in.maheshshelakee.moneymanager.service;

import in.maheshshelakee.moneymanager.dto.AdminDashboardResponse;
import in.maheshshelakee.moneymanager.dto.AdminUserDto;
import in.maheshshelakee.moneymanager.dto.ProfileDTO;
import in.maheshshelakee.moneymanager.entity.AdminAuditLog;
import in.maheshshelakee.moneymanager.entity.ProfileEntity;
import in.maheshshelakee.moneymanager.entity.UserStatus;
import in.maheshshelakee.moneymanager.exception.ResourceNotFoundException;
import in.maheshshelakee.moneymanager.repository.AdminAuditLogRepository;
import in.maheshshelakee.moneymanager.repository.ExpenseRepository;
import in.maheshshelakee.moneymanager.repository.IncomeRepository;
import in.maheshshelakee.moneymanager.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final ProfileRepository profileRepository;
    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final AdminAuditLogRepository adminAuditLogRepository;

    // ─── Dashboard ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboardStats() {
        long totalUsers = profileRepository.count();
        long activeUsers = profileRepository.countByStatus(UserStatus.ACTIVE);
        double totalIncome = incomeRepository.sumAllIncomes();
        double totalExpense = expenseRepository.sumAllExpenses();

        return AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .build();
    }

    // ─── Users ────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<AdminUserDto> getAllUsers(Pageable pageable) {
        return profileRepository.findAll(pageable)
                .map(p -> AdminUserDto.builder()
                        .id(p.getId())
                        .fullName(p.getFullName())
                        .email(p.getEmail())
                        .createdAt(p.getCreatedAt())
                        .isActive(p.getIsActive())
                        .status(p.getStatus())
                        .build());
    }

    @Transactional(readOnly = true)
    public ProfileDTO getUserById(Long id) {
        ProfileEntity user = profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return toProfileDTO(user);
    }

    @Transactional(readOnly = true)
    public Page<ProfileDTO> getAllUsersAsDTO(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return profileRepository.findAll(pageable).map(this::toProfileDTO);
    }

    // ─── User Status Update ───────────────────────────────────────────────────

    @Transactional
    public ProfileDTO updateUserStatus(Long userId, UserStatus newStatus,
                                       String adminEmail, String ipAddress,
                                       String reason) {
        ProfileEntity admin = profileRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        ProfileEntity targetUser = profileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Prevent admin from banning themselves
        if (admin.getId().equals(targetUser.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Admin cannot change their own status");
        }

        UserStatus oldStatus = targetUser.getStatus();
        targetUser.setStatus(newStatus);
        if (newStatus == UserStatus.BANNED || newStatus == UserStatus.SUSPENDED) {
            targetUser.setIsActive(false);
        } else if (newStatus == UserStatus.ACTIVE) {
            targetUser.setIsActive(true);
        }
        profileRepository.save(targetUser);

        String details = String.format("Status changed from %s to %s for user %s. Reason: %s",
                oldStatus, newStatus, targetUser.getEmail(),
                reason != null ? reason : "No reason provided");

        AdminAuditLog logEntry = AdminAuditLog.builder()
                .admin(admin)
                .targetUser(targetUser)
                .action("UPDATE_USER_STATUS")
                .details(details)
                .ipAddress(ipAddress)
                .build();
        adminAuditLogRepository.save(logEntry);

        log.info("Admin {} changed status of user {} from {} to {}", adminEmail, targetUser.getEmail(), oldStatus, newStatus);
        return toProfileDTO(targetUser);
    }

    // ─── System Stats ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", profileRepository.count());
        stats.put("activeUsers", profileRepository.countByStatus(UserStatus.ACTIVE));
        stats.put("suspendedUsers", profileRepository.countByStatus(UserStatus.SUSPENDED));
        stats.put("bannedUsers", profileRepository.countByStatus(UserStatus.BANNED));
        return stats;
    }

    // ─── Audit Logs ───────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<AdminAuditLog> getAuditLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return adminAuditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private ProfileDTO toProfileDTO(ProfileEntity entity) {
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
