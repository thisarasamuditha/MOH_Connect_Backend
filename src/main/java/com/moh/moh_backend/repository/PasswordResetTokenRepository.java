package com.moh.moh_backend.repository;

import com.moh.moh_backend.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    /** Marks every still-open link of a user as used, so only the newest link works. */
    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.usedAt = :now WHERE t.user.userId = :userId AND t.usedAt IS NULL")
    int invalidateOpenTokens(@Param("userId") Integer userId, @Param("now") LocalDateTime now);
}
