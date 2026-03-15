package in.maheshshelakee.moneymanager.repository;

import in.maheshshelakee.moneymanager.entity.CategoryEntity;
import in.maheshshelakee.moneymanager.entity.ProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    /**
     * JOIN FETCH subcategories to avoid N+1 when listing all categories.
     */
    @Query("SELECT c FROM CategoryEntity c LEFT JOIN FETCH c.subcategories WHERE c.profile = :profile ORDER BY c.createdAt DESC")
    List<CategoryEntity> findByProfileWithSubcategories(@Param("profile") ProfileEntity profile);

    Optional<CategoryEntity> findByIdAndProfile(Long id, ProfileEntity profile);

    boolean existsByNameAndTypeAndProfile(String name, String type, ProfileEntity profile);

    long countByProfile(ProfileEntity profile);
}
