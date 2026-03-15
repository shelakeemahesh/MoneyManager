package in.maheshshelakee.moneymanager.controller;

import in.maheshshelakee.moneymanager.entity.UserStatus;
import in.maheshshelakee.moneymanager.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final ProfileRepository profileRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        long totalUsers = profileRepository.count();
        long activeUsers = profileRepository.countByStatus(UserStatus.ACTIVE);
        long suspendedUsers = profileRepository.countByStatus(UserStatus.SUSPENDED);
        long bannedUsers = profileRepository.countByStatus(UserStatus.BANNED);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("suspendedUsers", suspendedUsers);
        stats.put("bannedUsers", bannedUsers);

        return ResponseEntity.ok(stats);
    }
}
