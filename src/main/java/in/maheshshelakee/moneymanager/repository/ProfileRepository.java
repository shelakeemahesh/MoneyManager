package in.maheshshelakee.moneymanager.repository;

import in.maheshshelakee.moneymanager.entity.ProfileEntity;
import in.maheshshelakee.moneymanager.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<ProfileEntity, Long> {

    Optional<ProfileEntity> findByEmail(String email);

    Optional<ProfileEntity> findByActivationToken(String token);

    long countByStatus(UserStatus status);
}
