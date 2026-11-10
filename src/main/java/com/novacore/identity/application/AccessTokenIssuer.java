package com.novacore.identity.application;

import java.time.Instant;
import java.util.UUID;

import com.novacore.identity.domain.UserAccount;
import com.novacore.shared.security.CurrentUser;
import com.novacore.shared.security.SecurityProperties;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

@Component
class AccessTokenIssuer {

	private final JwtEncoder encoder;

	private final SecurityProperties.Jwt properties;

	AccessTokenIssuer(JwtEncoder encoder, SecurityProperties properties) {
		this.encoder = encoder;
		this.properties = properties.jwt();
	}

	/** Le jeton contient l'identifiant (sub), la session (sid) et les rôles globaux. */
	String issue(UserAccount user, UUID sessionId, Instant now, Instant expiresAt) {
		JwtClaimsSet claims = JwtClaimsSet.builder()
			.issuer(properties.issuer())
			.subject(user.getId().toString())
			.issuedAt(now)
			.expiresAt(expiresAt)
			.claim(CurrentUser.SESSION_CLAIM, sessionId.toString())
			.claim("username", user.getUsername())
			.claim("roles", user.getRoles().stream().map(Enum::name).toList())
			.build();
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
	}

}
