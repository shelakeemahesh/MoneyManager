package in.maheshshelakee.moneymanager.repository;

import in.maheshshelakee.moneymanager.entity.IncomeEntity;
import in.maheshshelakee.moneymanager.entity.ProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IncomeRepository extends JpaRepository<IncomeEntity, Long> {

    List<IncomeEntity> findByProfileOrderByDateDesc(ProfileEntity profile);

    Optional<IncomeEntity> findByIdAndProfile(Long id, ProfileEntity profile);

    List<IncomeEntity> findByProfileAndDateBetweenOrderByDateDesc(ProfileEntity profile, LocalDate start, LocalDate end);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM IncomeEntity i")
    Double sumAllIncomes();

    // Added: per-user income sum for future dashboard use
    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM IncomeEntity i WHERE i.profile = :profile")
    Double sumAmountByProfile(@Param("profile") ProfileEntity profile);
}
