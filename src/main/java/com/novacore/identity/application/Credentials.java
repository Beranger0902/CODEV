package com.novacore.identity.application;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

import com.novacore.shared.error.ApiException;

/**
 * Règles de normalisation et de validité des identifiants.
 */
final class Credentials {

	static final int MIN_PASSWORD_LENGTH = 12;

	/** BCrypt ignore tout au-delà de 72 octets : on refuse plutôt que de tronquer silencieusement. */
	static final int MAX_PASSWORD_BYTES = 72;

	private static final Set<String> RESERVED_USERNAMES = Set.of("admin", "administrator", "root", "system",
			"support", "help", "api", "novacore", "moderator", "moderation", "security", "me", "null", "undefined");

	private Credentials() {
	}

	static String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}

	static String normalizeUsername(String username) {
		return username.trim().toLowerCase(Locale.ROOT);
	}

	static boolean isReservedUsername(String normalizedUsername) {
		return RESERVED_USERNAMES.contains(normalizedUsername);
	}

	static void checkPassword(String password) {
		if (password.length() < MIN_PASSWORD_LENGTH) {
			throw ApiException.badRequest("WEAK_PASSWORD",
					"Le mot de passe doit contenir au moins " + MIN_PASSWORD_LENGTH + " caractères.");
		}
		if (password.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
			throw ApiException.badRequest("PASSWORD_TOO_LONG", "Le mot de passe est trop long.");
		}
		if (password.isBlank()) {
			throw ApiException.badRequest("WEAK_PASSWORD", "Le mot de passe ne peut pas être vide.");
		}
	}

}
