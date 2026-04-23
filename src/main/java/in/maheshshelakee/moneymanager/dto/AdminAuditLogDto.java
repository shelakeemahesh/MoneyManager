package in.maheshshelakee.moneymanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for audit log responses.
 * Prevents exposing the JPA entity (and its lazy-loaded associations) directly
 * to the client, avoiding serialisation/N+1 issues at the controller layer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAuditLogDto {
    private Long id;
    private String adminEmail;
    private String targetUserEmail;
    private String action;
    private String details;
    private String ipAddress;
    private LocalDateTime createdAt;
}
