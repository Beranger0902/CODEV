package com.novacore.identity.application;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import com.novacore.identity.domain.AccountStatus;
import com.novacore.identity.domain.UserAccount;
import com.novacore.identity.domain.UserAccountRepository;
import com.novacore.identity.domain.UserSession;
import com.novacore.identity.domain.UserSessionRepository;
import com.novacore.shared.audit.AuditLogger;
import com.novacore.shared.error.ApiException;
import com.novacore.shared.security.SecurityProperties;
import com.novacore.shared.web.ClientInfo;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Connexion, rafraîchissement et déconnexion.
 * <p>
 * {@code noRollbackFor = ApiException.class} : un échec de connexion lève une erreur
 * mais le compteur d'échecs et l'audit doivent quand même être enregistrés.
 */
@Service
@Transactional(noRollbackFor = ApiException.class)
public class AuthenticationService {

	private static ApiException invalidCredentials() {
		return ApiException.unauthorized("INVALID_CREDENTIALS",
				"Adresse e-mail ou mot de passe incorrect.");
	}

	private static ApiException invalidRefreshToken() {
		return ApiException.unauthorized("INVALID_REFRESH_TOKEN",
				"Session expirée. Veuillez vous reconnecter.");
	}

	private final UserAccountRepository users;

	private final UserSessionRepository sessions;

	private final PasswordEncoder passwordEncoder;

	private final AccessTokenIssuer accessTokenIssuer;

	private final AuditLogger audit;

	private final IdentityProperties identityProperties;

	private final SecurityProperties.Jwt jwtProperties;

	private final Clock clock;

	/** Comparé quand l'e-mail est inconnu, pour que le temps de réponse ne révèle rien. */
	private final String dummyPasswordHash;

	AuthenticationService(UserAccountRepository users, UserSessionRepository sessions, PasswordEncoder passwordEncoder,
			AccessTokenIssuer accessTokenIssuer, AuditLogger audit, IdentityProperties identityProperties,
			SecurityProperties securityProperties, Clock clock) {
		this.users = users;
		this.sessions = sessions;
		this.passwordEncoder = passwordEncoder;
		this.accessTokenIssuer = accessTokenIssuer;
		this.audit = audit;
		this.identityProperties = identityProperties;
		this.jwtProperties = securityProperties.jwt();
		this.clock = clock;
		this.dummyPasswordHash = passwordEncoder.encode(SecureTokens.generate());
	}

	public IssuedTokens login(String email, String password, ClientInfo client) {
		Instant now = clock.instant();
		Optional<UserAccount> found = users.findByEmail(Credentials.normalizeEmail(email));
		if (found.isEmpty()) {
			passwordEncoder.matches(password, dummyPasswordHash);
			throw invalidCredentials();
		}
		UserAccount user = found.get();

		if (user.isLocked(now)) {
			throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "ACCOUNT_TEMPORARILY_LOCKED",
					"Trop de tentatives. Réessayez dans quelques minutes.");
		}
		if (!passwordEncoder.matches(password, user.getPasswordHash())) {
			user.recordFailedLogin(now, identityProperties.maxFailedLogins(), identityProperties.lockDuration());
			audit.record(user.getId(), "LOGIN_FAILED", "USER", user.getId(), client);
			throw invalidCredentials();
		}

		switch (user.getStatus()) {
			case ACTIVE -> {
			}
			case PENDING_VERIFICATION -> throw ApiException.forbidden("EMAIL_NOT_VERIFIED",
					"Confirmez votre adresse e-mail avant de vous connecter.");
			case SUSPENDED -> throw ApiException.forbidden("ACCOUNT_SUSPENDED", "Ce compte est suspendu.");
			case DEACTIVATED -> throw ApiException.forbidden("ACCOUNT_DEACTIVATED", "Ce compte est désactivé.");
		}

		user.recordSuccessfulLogin();
		if (passwordEncoder.upgradeEncoding(user.getPasswordHash())) {
			user.changePassword(passwordEncoder.encode(password));
		}

		String refreshToken = SecureTokens.generate();
		UserSession session = sessions.save(UserSession.open(user.getId(), SecureTokens.hash(refreshToken), now,
				jwtProperties.refreshTokenTtl(), client));
		audit.record(user.getId(), "LOGIN_SUCCEEDED", "SESSION", session.getId(), client);
		return issue(user, session, refreshToken, now);
	}

	/**
	 * Échange un refresh token contre une nouvelle paire de jetons (rotation).
	 * Si un jeton déjà remplacé est présenté, il a probablement été volé : la session est révoquée.
	 */
	public IssuedTokens refresh(String refreshToken, ClientInfo client) {
		Instant now = clock.instant();
		String hash = SecureTokens.hash(refreshToken);

		Optional<UserSession> found = sessions.findByRefreshTokenHash(hash);
		if (found.isEmpty()) {
			sessions.findByPreviousTokenHash(hash).ifPresent(reused -> {
				reused.revoke(now);
				audit.record(reused.getUserId(), "REFRESH_TOKEN_REUSED", "SESSION", reused.getId(), client);
			});
			throw invalidRefreshToken();
		}

		UserSession session = found.get();
		if (!session.isActive(now)) {
			throw invalidRefreshToken();
		}
		UserAccount user = users.findById(session.getUserId()).orElseThrow(AuthenticationService::invalidRefreshToken);
		if (user.getStatus() != AccountStatus.ACTIVE) {
			session.revoke(now);
			throw invalidRefreshToken();
		}

		String newRefreshToken = SecureTokens.generate();
		session.rotate(SecureTokens.hash(newRefreshToken), now, client);
		return issue(user, session, newRefreshToken, now);
	}

	/** Déconnexion de l'appareil courant. Silencieuse si le jeton est inconnu. */
	public void logout(String refreshToken, ClientInfo client) {
		sessions.findByRefreshTokenHash(SecureTokens.hash(refreshToken)).ifPresent(session -> {
			session.revoke(clock.instant());
			audit.record(session.getUserId(), "LOGOUT", "SESSION", session.getId(), client);
		});
	}

	private IssuedTokens issue(UserAccount user, UserSession session, String refreshToken, Instant now) {
		Instant accessTokenExpiresAt = now.plus(jwtProperties.accessTokenTtl());
		String accessToken = accessTokenIssuer.issue(user, session.getId(), now, accessTokenExpiresAt);
		return new IssuedTokens(accessToken, accessTokenExpiresAt, refreshToken, session.getExpiresAt());
	}

}
