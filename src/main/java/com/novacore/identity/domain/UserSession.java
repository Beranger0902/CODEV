package com.novacore.identity.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import com.novacore.shared.web.ClientInfo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Session d'un appareil, identifiée par un refresh token dont seul le hash est conservé.
 * Le jeton change à chaque rafraîchissement (rotation) ; présenter l'ancien jeton
 * signale un vol probable et révoque la session.
 */
@Entity
@Table(name = "user_sessions")
public class UserSession {

	@Id
	private UUID id;

	@Column(nullable = false)
	private UUID userId;

	@Column(nullable = false)
	private String refreshTokenHash;

	private String previousTokenHash;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant lastUsedAt;

	@Column(nullable = false)
	private Instant expiresAt;

	private Instant revokedAt;

	private String userAgent;

	private String ipAddress;

	protected UserSession() {
	}

	public static UserSession open(UUID userId, String refreshTokenHash, Instant now, Duration ttl,
			ClientInfo client) {
		UserSession session = new UserSession();
		session.id = UUID.randomUUID();
		session.userId = userId;
		session.refreshTokenHash = refreshTokenHash;
		session.createdAt = now;
		session.lastUsedAt = now;
		session.expiresAt = now.plus(ttl);
		session.userAgent = client.userAgent();
		session.ipAddress = client.ipAddress();
		return session;
	}

	public void rotate(String newRefreshTokenHash, Instant now, ClientInfo client) {
		previousTokenHash = refreshTokenHash;
		refreshTokenHash = newRefreshTokenHash;
		lastUsedAt = now;
		ipAddress = client.ipAddress();
	}

	public boolean isActive(Instant now) {
		return revokedAt == null && expiresAt.isAfter(now);
	}

	public void revoke(Instant now) {
		if (revokedAt == null) {
			revokedAt = now;
		}
	}

	public UUID getId() {
		return id;
	}

	public UUID getUserId() {
		return userId;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getLastUsedAt() {
		return lastUsedAt;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public String getIpAddress() {
		return ipAddress;
	}

}
