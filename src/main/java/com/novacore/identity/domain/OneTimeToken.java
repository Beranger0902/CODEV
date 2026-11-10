package com.novacore.identity.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Jeton à usage unique et à durée limitée, envoyé par e-mail. Seul son hash est stocké.
 */
@Entity
@Table(name = "one_time_tokens")
public class OneTimeToken {

	@Id
	private UUID id;

	@Column(nullable = false)
	private UUID userId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TokenPurpose purpose;

	@Column(nullable = false)
	private String tokenHash;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant expiresAt;

	private Instant usedAt;

	protected OneTimeToken() {
	}

	public static OneTimeToken issue(UUID userId, TokenPurpose purpose, String tokenHash, Instant now, Duration ttl) {
		OneTimeToken token = new OneTimeToken();
		token.id = UUID.randomUUID();
		token.userId = userId;
		token.purpose = purpose;
		token.tokenHash = tokenHash;
		token.createdAt = now;
		token.expiresAt = now.plus(ttl);
		return token;
	}

	public boolean isUsable(Instant now) {
		return usedAt == null && expiresAt.isAfter(now);
	}

	public void consume(Instant now) {
		usedAt = now;
	}

	public UUID getUserId() {
		return userId;
	}

}
