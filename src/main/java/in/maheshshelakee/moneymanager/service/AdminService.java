package in.maheshshelakee.moneymanager.service;

import in.maheshshelakee.moneymanager.dto.AdminDashboardResponse;
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
                .map(p -> AdminUserDto.builder()
                        .id(p.getId())
                        .fullName(p.getFullName())
                        .email(p.getEmail())
                        .createdAt(p.getCreatedAt())
                        .isActive(p.getIsActive())
                        .status(p.getStatus())
                        .build());
    }

    @Transactional
    public void updateUserStatus(Long userId, UserStatus newStatus, String adminEmail, String ipAddress) {
        ProfileEntity admin = profileRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        ProfileEntity targetUser = profileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserStatus oldStatus = targetUser.getStatus();
        targetUser.setStatus(newStatus);
        
        // Also toggle isActive flag for consistency if needed, assuming BANNED/SUSPENDED implies inactive
        if (newStatus == UserStatus.BANNED || newStatus == UserStatus.SUSPENDED) {
            targetUser.setIsActive(false);
        } else if (newStatus == UserStatus.ACTIVE && oldStatus != UserStatus.ACTIVE) {
            targetUser.setIsActive(true);
        }
        
        profileRepository.save(targetUser);

        // create audit log
        AdminAuditLog logEntry = AdminAuditLog.builder()
                .admin(admin)
                .targetUser(targetUser)
                .action("UPDATE_USER_STATUS")
                .details(String.format("Changed status from %s to %s for user %s", oldStatus, newStatus, targetUser.getEmail()))
                .ipAddress(ipAddress)
                .build();

        adminAuditLogRepository.save(logEntry);
        
        log.info("Admin {} changed status of user {} to {}", adminEmail, targetUser.getEmail(), newStatus);
    }
}
