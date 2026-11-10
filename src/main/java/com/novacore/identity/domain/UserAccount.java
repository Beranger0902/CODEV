package com.novacore.identity.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "user_accounts")
public class UserAccount {

	@Id
	private UUID id;

	@Column(nullable = false)
	private String email;

	@Column(nullable = false)
	private String username;

	@Column(nullable = false)
	private String displayName;

	@Column(nullable = false)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AccountStatus status;

	private Instant emailVerifiedAt;

	@Column(nullable = false)
	private String termsVersion;

	@Column(nullable = false)
	private Instant termsAcceptedAt;

	private int failedLoginAttempts;

	private Instant lockedUntil;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false)
	private Set<Role> roles = new HashSet<>();

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(nullable = false)
	private Instant updatedAt;

	@Version
	private long version;

	protected UserAccount() {
	}

	public static UserAccount register(String email, String username, String displayName, String passwordHash,
			String termsVersion, Instant now) {
		UserAccount user = new UserAccount();
		user.id = UUID.randomUUID();
		user.email = email;
		user.username = username;
		user.displayName = displayName;
		user.passwordHash = passwordHash;
		user.status = AccountStatus.PENDING_VERIFICATION;
		user.termsVersion = termsVersion;
		user.termsAcceptedAt = now;
		user.roles.add(Role.MEMBER);
		return user;
	}

	public void verifyEmail(Instant now) {
		if (status == AccountStatus.PENDING_VERIFICATION) {
			status = AccountStatus.ACTIVE;
		}
		if (emailVerifiedAt == null) {
			emailVerifiedAt = now;
		}
	}

	public boolean isLocked(Instant now) {
		return lockedUntil != null && lockedUntil.isAfter(now);
	}

	/** Verrouille temporairement le compte après {@code maxAttempts} échecs consécutifs. */
	public void recordFailedLogin(Instant now, int maxAttempts, Duration lockDuration) {
		failedLoginAttempts++;
		if (failedLoginAttempts >= maxAttempts) {
			lockedUntil = now.plus(lockDuration);
			failedLoginAttempts = 0;
		}
	}

	public void recordSuccessfulLogin() {
		failedLoginAttempts = 0;
		lockedUntil = null;
	}

	public void changePassword(String newPasswordHash) {
		passwordHash = newPasswordHash;
		failedLoginAttempts = 0;
		lockedUntil = null;
	}

	public UUID getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getUsername() {
		return username;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public AccountStatus getStatus() {
		return status;
	}

	public Instant getEmailVerifiedAt() {
		return emailVerifiedAt;
	}

	public Set<Role> getRoles() {
		return Collections.unmodifiableSet(roles);
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

}
