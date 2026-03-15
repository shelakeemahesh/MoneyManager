package in.maheshshelakee.moneymanager.repository;

import in.maheshshelakee.moneymanager.entity.ExpenseEntity;
import in.maheshshelakee.moneymanager.entity.ProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<ExpenseEntity, Long> {

    // ─── Basic Queries ─────────────────────────────────────────────────────────

    List<ExpenseEntity> findByProfileOrderByExpenseDateDesc(ProfileEntity profile);

    Optional<ExpenseEntity> findByIdAndProfile(Long id, ProfileEntity profile);

    // ─── Date Range Filter ─────────────────────────────────────────────────────

    List<ExpenseEntity> findByProfileAndExpenseDateBetweenOrderByExpenseDateDesc(
            ProfileEntity profile,
            LocalDate startDate,
            LocalDate endDate
    );

    // ─── Category Filter ───────────────────────────────────────────────────────

    List<ExpenseEntity> findByProfileAndCategoryIgnoreCaseOrderByExpenseDateDesc(
            ProfileEntity profile,
            String category
    );

    // ─── Category + Date Range Combined Filter ─────────────────────────────────

    List<ExpenseEntity> findByProfileAndCategoryIgnoreCaseAndExpenseDateBetweenOrderByExpenseDateDesc(
            ProfileEntity profile,
            String category,
            LocalDate startDate,
            LocalDate endDate
    );

    // ─── Aggregate: Total Expense Amount ───────────────────────────────────────

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM ExpenseEntity e WHERE e.profile = :profile")
    Double sumAmountByProfile(@Param("profile") ProfileEntity profile);

    // ─── Admin Aggregates ──────────────────────────────────────────────────────

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM ExpenseEntity e")
    Double sumAllExpenses();
}
