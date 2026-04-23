package in.maheshshelakee.moneymanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Typed DTO for system-wide user statistics.
 * Replaces the raw Map&lt;String, Object&gt; that was previously returned.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsResponse {
    private long totalUsers;
    private long activeUsers;
    private long suspendedUsers;
    private long bannedUsers;
}
