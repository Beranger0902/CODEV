package com.novacore.identity.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

	Optional<UserSession> findByRefreshTokenHash(String refreshTokenHash);

	Optional<UserSession> findByPreviousTokenHash(String previousTokenHash);

	List<UserSession> findByUserIdAndRevokedAtIsNullAndExpiresAtAfterOrderByLastUsedAtDesc(UUID userId, Instant now);

	@Modifying
	@Query("update UserSession s set s.revokedAt = :now where s.userId = :userId and s.revokedAt is null")
	int revokeAll(UUID userId, Instant now);

	@Modifying
	@Query("""
			update UserSession s set s.revokedAt = :now
			where s.userId = :userId and s.id <> :keptSessionId and s.revokedAt is null""")
	int revokeAllExcept(UUID userId, UUID keptSessionId, Instant now);

}
