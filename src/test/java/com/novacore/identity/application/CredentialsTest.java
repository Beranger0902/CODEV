package com.novacore.identity.application;

import com.novacore.shared.error.ApiException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CredentialsTest {

	@Test
	void normalizesEmailAndUsername() {
		assertThat(Credentials.normalizeEmail("  Ada@Example.COM ")).isEqualTo("ada@example.com");
		assertThat(Credentials.normalizeUsername("Ada_Dev")).isEqualTo("ada_dev");
	}

	@Test
	void rejectsShortPassword() {
		assertThatThrownBy(() -> Credentials.checkPassword("court"))
			.isInstanceOf(ApiException.class)
			.extracting("code")
			.isEqualTo("WEAK_PASSWORD");
	}

	@Test
	void rejectsPasswordBeyondBcryptLimit() {
		// 40 caractères accentués = 80 octets UTF-8 > 72
		assertThatThrownBy(() -> Credentials.checkPassword("é".repeat(40)))
			.isInstanceOf(ApiException.class)
			.extracting("code")
			.isEqualTo("PASSWORD_TOO_LONG");
	}

	@Test
	void acceptsLongEnoughPassword() {
		assertThatCode(() -> Credentials.checkPassword("une-phrase-de-passe-solide")).doesNotThrowAnyException();
	}

	@Test
	void reservesSensitiveUsernames() {
		assertThat(Credentials.isReservedUsername("admin")).isTrue();
		assertThat(Credentials.isReservedUsername("ada")).isFalse();
	}

	@Test
	void hashesTokensDeterministically() {
		String token = SecureTokens.generate();

		assertThat(SecureTokens.hash(token)).isEqualTo(SecureTokens.hash(token)).hasSize(64);
		assertThat(SecureTokens.generate()).isNotEqualTo(token);
	}

}
