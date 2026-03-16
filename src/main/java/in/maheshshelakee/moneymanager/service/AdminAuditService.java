package in.maheshshelakee.moneymanager.service;

import in.maheshshelakee.moneymanager.entity.AdminAuditLog;
import in.maheshshelakee.moneymanager.entity.ProfileEntity;
import in.maheshshelakee.moneymanager.repository.AdminAuditLogRepository;
import in.maheshshelakee.moneymanager.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuditService {

    private final AdminAuditLogRepository adminAuditLogRepository;
    private final ProfileRepository profileRepository;

    // FIX 1: Removed manual .createdAt(LocalDateTime.now()) from the builder.
    //         @CreationTimestamp on AdminAuditLog.createdAt handles timestamping automatically.
    //         Setting it manually via Lombok builder was redundant and bypassed the annotation.
    //
    // FIX 2: @Async now works because @EnableAsync was added to MoneymanagerApplication.
    //         Added Slf4j so uncaught async exceptions are logged rather than silently swallowed.
    @Async
    public void logAction(String adminEmail, Long targetUserId, String action, String details, String ipAddress) {
        try {
            ProfileEntity admin = profileRepository.findByEmail(adminEmail).orElse(null);
            ProfileEntity targetUser = targetUserId != null
                    ? profileRepository.findById(targetUserId).orElse(null)
                    : null;

            if (admin == null) {
                log.warn("AdminAuditService: admin not found for email '{}', skipping audit log", adminEmail);
                return;
            }

            AdminAuditLog logEntry = AdminAuditLog.builder()
                    .admin(admin)
                    .targetUser(targetUser)
                    .action(action)
                    .details(details)
                    .ipAddress(ipAddress)
                    // FIX: createdAt intentionally omitted — @CreationTimestamp sets it on persist
                    .build();

            adminAuditLogRepository.save(logEntry);
        } catch (Exception ex) {
            // Async exceptions are otherwise silently swallowed
            log.error("AdminAuditService: failed to save audit log for admin='{}' action='{}'",
                    adminEmail, action, ex);
        }
    }
}
