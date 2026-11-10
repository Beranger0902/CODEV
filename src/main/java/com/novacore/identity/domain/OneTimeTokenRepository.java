package com.novacore.identity.domain;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface OneTimeTokenRepository extends JpaRepository<OneTimeToken, UUID> {

	Optional<OneTimeToken> findByTokenHashAndPurpose(String tokenHash, TokenPurpose purpose);

	boolean existsByUserIdAndPurposeAndCreatedAtAfter(UUID userId, TokenPurpose purpose, Instant since);

	/** Invalide les jetons encore valides : seul le plus récent reste utilisable. */
	@Modifying
	@Query("""
			update OneTimeToken t set t.usedAt = :now
			where t.userId = :userId and t.purpose = :purpose and t.usedAt is null""")
	int invalidateAll(UUID userId, TokenPurpose purpose, Instant now);

}
