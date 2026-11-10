package com.novacore.shared.security;

import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("novacore.security")
public record SecurityProperties(Jwt jwt, Cors cors) {

	/**
	 * @param secret clé HMAC-SHA256 (32 octets minimum), fournie par variable d'environnement
	 * @param issuer valeur du claim {@code iss}, vérifiée à chaque requête
	 * @param accessTokenTtl durée de vie du jeton d'accès (courte : il n'est pas révocable)
	 * @param refreshTokenTtl durée de vie maximale d'une session
	 */
	public record Jwt(String secret, String issuer, Duration accessTokenTtl, Duration refreshTokenTtl) {
	}

	public record Cors(List<String> allowedOrigins) {
	}

}
