package in.maheshshelakee.moneymanager.service;

import in.maheshshelakee.moneymanager.dto.AdminDashboardResponse;
import in.maheshshelakee.moneymanager.dto.AdminStatsResponse;
import in.maheshshelakee.moneymanager.dto.AdminUserDto;
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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final ProfileRepository profileRepository;
    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final AdminAuditLogRepository adminAuditLogRepository;

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

    public Page<AdminUserDto> getAllUsers(Pageable pageable) {
        return profileRepository.findAll(pageable)
                .map(this::toDto);
    }

    public AdminUserDto getUserById(Long id) {
        ProfileEntity user = profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return toDto(user);
    }

    /**
     * Returns detailed user statistics (total, active, suspended, banned).
     * Moved from AdminStatsController to proper service layer.
     */
    public AdminStatsResponse getSystemStats() {
        return AdminStatsResponse.builder()
                .totalUsers(profileRepository.count())
                .activeUsers(profileRepository.countByStatus(UserStatus.ACTIVE))
                .suspendedUsers(profileRepository.countByStatus(UserStatus.SUSPENDED))
                .bannedUsers(profileRepository.countByStatus(UserStatus.BANNED))
                .build();
    }

    @Transactional
    public AdminUserDto updateUserStatus(Long userId, UserStatus newStatus, String reason,
                                         String adminEmail, String ipAddress) {
        ProfileEntity admin = profileRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        ProfileEntity targetUser = profileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        UserStatus oldStatus = targetUser.getStatus();
        targetUser.setStatus(newStatus);

        if (newStatus == UserStatus.BANNED || newStatus == UserStatus.SUSPENDED) {
            targetUser.setIsActive(false);
        } else if (newStatus == UserStatus.ACTIVE && oldStatus != UserStatus.ACTIVE) {
            targetUser.setIsActive(true);
        }

        profileRepository.save(targetUser);

        AdminAuditLog logEntry = AdminAuditLog.builder()
                .admin(admin)
                .targetUser(targetUser)
                .action("UPDATE_USER_STATUS")
                .details(String.format("Changed status from %s to %s for user %s. Reason: %s",
                        oldStatus, newStatus, targetUser.getEmail(), reason))
                .ipAddress(ipAddress)
                .build();

        adminAuditLogRepository.save(logEntry);

        log.info("Admin {} changed status of user {} from {} to {}",
                adminEmail, targetUser.getEmail(), oldStatus, newStatus);

        return toDto(targetUser);
    }

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
