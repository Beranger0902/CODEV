package com.novacore.shared.security;

import java.util.UUID;

import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Lecture des informations de l'utilisateur connecté depuis son jeton d'accès.
 */
public final class CurrentUser {

	public static final String SESSION_CLAIM = "sid";

	private CurrentUser() {
	}

	public static UUID id(Jwt jwt) {
		return UUID.fromString(jwt.getSubject());
	}

	public static UUID sessionId(Jwt jwt) {
		return UUID.fromString(jwt.getClaimAsString(SESSION_CLAIM));
	}

}
