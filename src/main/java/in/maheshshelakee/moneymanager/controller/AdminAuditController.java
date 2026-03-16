package in.maheshshelakee.moneymanager.controller;

import in.maheshshelakee.moneymanager.entity.AdminAuditLog;
import in.maheshshelakee.moneymanager.repository.AdminAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// NOTE: N+1 issue on lazy admin/targetUser — see AdminAuditLogRepository for the JOIN FETCH fix
@RestController
@RequestMapping("/admin/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAuditController {

    private final AdminAuditLogRepository adminAuditLogRepository;

    @GetMapping
    public ResponseEntity<Page<AdminAuditLog>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(adminAuditLogRepository.findAllByOrderByCreatedAtDesc(pageable));
    }
}
