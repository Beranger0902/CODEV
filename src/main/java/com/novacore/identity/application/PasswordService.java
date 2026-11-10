package com.novacore.identity.application;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import com.novacore.identity.domain.AccountStatus;
import com.novacore.identity.domain.OneTimeToken;
import com.novacore.identity.domain.OneTimeTokenRepository;
import com.novacore.identity.domain.TokenPurpose;
import com.novacore.identity.domain.UserAccount;
import com.novacore.identity.domain.UserAccountRepository;
import com.novacore.identity.domain.UserSessionRepository;
import com.novacore.shared.audit.AuditLogger;
import com.novacore.shared.error.ApiException;
import com.novacore.shared.web.ClientInfo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PasswordService {

	private final UserAccountRepository users;

	private final UserSessionRepository sessions;

	private final OneTimeTokenRepository tokens;

	private final PasswordEncoder passwordEncoder;

	private final IdentityMails mails;

	private final AuditLogger audit;

	private final IdentityProperties properties;

	private final Clock clock;

	PasswordService(UserAccountRepository users, UserSessionRepository sessions, OneTimeTokenRepository tokens,
			PasswordEncoder passwordEncoder, IdentityMails mails, AuditLogger audit, IdentityProperties properties,
			Clock clock) {
		this.users = users;
		this.sessions = sessions;
		this.tokens = tokens;
		this.passwordEncoder = passwordEncoder;
		this.mails = mails;
		this.audit = audit;
		this.properties = properties;
		this.clock = clock;
	}

	/** Toujours silencieux pour ne pas révéler l'existence d'un compte. */
	public void requestReset(String email, ClientInfo client) {
		Instant now = clock.instant();
		users.findByEmail(Credentials.normalizeEmail(email))
			.filter(user -> user.getStatus() == AccountStatus.ACTIVE)
			.filter(user -> !tokens.existsByUserIdAndPurposeAndCreatedAtAfter(user.getId(), TokenPurpose.PASSWORD_RESET,
					now.minus(properties.resendCooldown())))
			.ifPresent(user -> {
				tokens.invalidateAll(user.getId(), TokenPurpose.PASSWORD_RESET, now);
				String token = SecureTokens.generate();
				tokens.save(OneTimeToken.issue(user.getId(), TokenPurpose.PASSWORD_RESET, SecureTokens.hash(token), now,
						properties.passwordResetTokenTtl()));
				mails.passwordReset(user, token);
				audit.record(user.getId(), "PASSWORD_RESET_REQUESTED", "USER", user.getId(), client);
			});
	}

	/** Réinitialise le mot de passe et déconnecte toutes les sessions. */
	public void reset(String token, String newPassword, ClientInfo client) {
		Credentials.checkPassword(newPassword);
		Instant now = clock.instant();
		OneTimeToken stored = tokens.findByTokenHashAndPurpose(SecureTokens.hash(token), TokenPurpose.PASSWORD_RESET)
			.filter(t -> t.isUsable(now))
			.orElseThrow(() -> ApiException.badRequest("INVALID_TOKEN", "Ce lien de réinitialisation est invalide ou a expiré."));
		stored.consume(now);

		UserAccount user = users.findById(stored.getUserId()).orElseThrow();
		user.changePassword(passwordEncoder.encode(newPassword));
		sessions.revokeAll(user.getId(), now);
		mails.passwordChanged(user);
		audit.record(user.getId(), "PASSWORD_RESET", "USER", user.getId(), client);
	}

	/** Changement par l'utilisateur connecté : la session courante est conservée, les autres sont révoquées. */
	public void change(UUID userId, UUID currentSessionId, String currentPassword, String newPassword,
			ClientInfo client) {
		UserAccount user = users.findById(userId).orElseThrow();
		if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
			throw ApiException.badRequest("INVALID_CURRENT_PASSWORD", "Le mot de passe actuel est incorrect.");
		}
		Credentials.checkPassword(newPassword);

		user.changePassword(passwordEncoder.encode(newPassword));
		sessions.revokeAllExcept(userId, currentSessionId, clock.instant());
		mails.passwordChanged(user);
		audit.record(userId, "PASSWORD_CHANGED", "USER", userId, client);
	}

}
