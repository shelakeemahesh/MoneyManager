package in.maheshshelakee.moneymanager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String profileImageUrl;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private Boolean isActive;

    /** Used only for email-based account activation. */
    private String activationToken;

    /** Used only for password reset flow — separate from activationToken. */
    private String resetToken;

    /** Expiry timestamp for the password reset token (valid for 1 hour). */
    private LocalDateTime resetTokenExpiresAt;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @PrePersist
    void prePersist() {
        if (isActive == null) isActive = false;
        if (role == null) role = Role.USER;
        if (status == null) status = UserStatus.ACTIVE;
    }
}