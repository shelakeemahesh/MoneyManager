package in.maheshshelakee.moneymanager.repository;

import in.maheshshelakee.moneymanager.entity.AdminAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, Long> {

    // FIX: Added JOIN FETCH for admin and targetUser to prevent N+1 queries.
    //      The original findAllByOrderByCreatedAtDesc() would trigger 2 lazy-load
    //      queries per row (admin + targetUser) when Jackson serialised the page —
    //      20 rows per page = 40 extra round-trips per request.
    //
    //      Note: COUNT query is specified separately because FETCH joins are not
    //      allowed in count queries (Spring Data requires countQuery in this case).
    @Query(
        value = "SELECT l FROM AdminAuditLog l LEFT JOIN FETCH l.admin LEFT JOIN FETCH l.targetUser ORDER BY l.createdAt DESC",
        countQuery = "SELECT COUNT(l) FROM AdminAuditLog l"
    )
    Page<AdminAuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
