package in.maheshshelakee.moneymanager.service;

import in.maheshshelakee.moneymanager.entity.AdminAuditLog;
import in.maheshshelakee.moneymanager.entity.ProfileEntity;
import in.maheshshelakee.moneymanager.repository.AdminAuditLogRepository;
import in.maheshshelakee.moneymanager.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminAuditService {

    private final AdminAuditLogRepository adminAuditLogRepository;
    private final ProfileRepository profileRepository;

    @Async
    public void logAction(String adminEmail, Long targetUserId, String action, String details, String ipAddress) {
        ProfileEntity admin = profileRepository.findByEmail(adminEmail).orElse(null);
        ProfileEntity targetUser = targetUserId != null ? profileRepository.findById(targetUserId).orElse(null) : null;

        if (admin != null) {
            AdminAuditLog log = AdminAuditLog.builder()
                    .admin(admin)
                    .targetUser(targetUser)
                    .action(action)
                    .details(details)
                    .ipAddress(ipAddress)
                    .createdAt(LocalDateTime.now())
                    .build();
            adminAuditLogRepository.save(log);
        }
    }
}
