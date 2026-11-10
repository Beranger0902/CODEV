package com.novacore.identity.domain;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserAccountTest {

	private final Instant now = Instant.parse("2026-11-10T10:00:00Z");

	private UserAccount newAccount() {
		return UserAccount.register("ada@example.com", "ada", "Ada", "{bcrypt}hash", "2026-10", now);
	}

	@Test
	void newAccountIsPendingMember() {
		UserAccount user = newAccount();

		assertThat(user.getStatus()).isEqualTo(AccountStatus.PENDING_VERIFICATION);
		assertThat(user.getRoles()).containsExactly(Role.MEMBER);
	}

	@Test
	void verifyingEmailActivatesAccount() {
		UserAccount user = newAccount();

		user.verifyEmail(now);

		assertThat(user.getStatus()).isEqualTo(AccountStatus.ACTIVE);
		assertThat(user.getEmailVerifiedAt()).isEqualTo(now);
	}

	@Test
	void locksAfterMaxFailedAttemptsThenUnlocksAfterDuration() {
		UserAccount user = newAccount();
		Duration lock = Duration.ofMinutes(15);

		for (int i = 0; i < 4; i++) {
			user.recordFailedLogin(now, 5, lock);
		}
		assertThat(user.isLocked(now)).isFalse();

		user.recordFailedLogin(now, 5, lock);
		assertThat(user.isLocked(now)).isTrue();
		assertThat(user.isLocked(now.plus(lock).plusSeconds(1))).isFalse();
	}

	@Test
	void successfulLoginResetsFailures() {
		UserAccount user = newAccount();
		Duration lock = Duration.ofMinutes(15);
		for (int i = 0; i < 4; i++) {
			user.recordFailedLogin(now, 5, lock);
		}

		user.recordSuccessfulLogin();
		user.recordFailedLogin(now, 5, lock);

		assertThat(user.isLocked(now)).isFalse();
	}

}
