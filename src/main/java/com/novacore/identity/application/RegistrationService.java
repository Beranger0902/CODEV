package com.novacore.identity.application;

import java.time.Clock;
import java.time.Instant;

import com.novacore.identity.domain.AccountStatus;
import com.novacore.identity.domain.OneTimeToken;
import com.novacore.identity.domain.OneTimeTokenRepository;
import com.novacore.identity.domain.TokenPurpose;
import com.novacore.identity.domain.UserAccount;
import com.novacore.identity.domain.UserAccountRepository;
import com.novacore.shared.audit.AuditLogger;
import com.novacore.shared.error.ApiException;
import com.novacore.shared.web.ClientInfo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RegistrationService {

	private final UserAccountRepository users;

	private final OneTimeTokenRepository tokens;

	private final PasswordEncoder passwordEncoder;

	private final IdentityMails mails;

	private final AuditLogger audit;

	private final IdentityProperties properties;

	private final Clock clock;

	RegistrationService(UserAccountRepository users, OneTimeTokenRepository tokens, PasswordEncoder passwordEncoder,
			IdentityMails mails, AuditLogger audit, IdentityProperties properties, Clock clock) {
		this.users = users;
		this.tokens = tokens;
		this.passwordEncoder = passwordEncoder;
		this.mails = mails;
		this.audit = audit;
		this.properties = properties;
		this.clock = clock;
	}

	/**
	 * Crée un compte en attente de vérification. Si l'e-mail est déjà inscrit, la réponse est
	 * identique et le titulaire est prévenu par e-mail : l'API ne révèle pas quelles adresses existent.
	 */
	public void register(String email, String username, String displayName, String password, ClientInfo client) {
		String normalizedEmail = Credentials.normalizeEmail(email);
		String normalizedUsername = Credentials.normalizeUsername(username);
		Credentials.checkPassword(password);

		if (Credentials.isReservedUsername(normalizedUsername) || users.existsByUsername(normalizedUsername)) {
			throw ApiException.conflict("USERNAME_UNAVAILABLE", "Ce nom d'utilisateur n'est pas disponible.");
		}

		// Hachage systématique : même temps de réponse que l'e-mail existe ou non.
		String passwordHash = passwordEncoder.encode(password);

		var existing = users.findByEmail(normalizedEmail);
		if (existing.isPresent()) {
			mails.accountAlreadyExists(existing.get());
			return;
		}

		Instant now = clock.instant();
		UserAccount user = UserAccount.register(normalizedEmail, normalizedUsername, displayName.trim(), passwordHash,
				properties.termsVersion(), now);
		users.save(user);
		sendVerification(user, now);
		audit.record(user.getId(), "ACCOUNT_REGISTERED", "USER", user.getId(), client,
				"terms=" + properties.termsVersion());
	}

	public void verifyEmail(String token, ClientInfo client) {
		Instant now = clock.instant();
		OneTimeToken stored = tokens.findByTokenHashAndPurpose(SecureTokens.hash(token), TokenPurpose.EMAIL_VERIFICATION)
			.filter(t -> t.isUsable(now))
			.orElseThrow(() -> ApiException.badRequest("INVALID_TOKEN", "Ce lien de vérification est invalide ou a expiré."));
		stored.consume(now);

		UserAccount user = users.findById(stored.getUserId()).orElseThrow();
		user.verifyEmail(now);
		audit.record(user.getId(), "EMAIL_VERIFIED", "USER", user.getId(), client);
	}

	/** Toujours silencieux pour ne pas révéler l'existence d'un compte. */
	public void resendVerification(String email) {
		Instant now = clock.instant();
		users.findByEmail(Credentials.normalizeEmail(email))
			.filter(user -> user.getStatus() == AccountStatus.PENDING_VERIFICATION)
			.filter(user -> !tokens.existsByUserIdAndPurposeAndCreatedAtAfter(user.getId(),
					TokenPurpose.EMAIL_VERIFICATION, now.minus(properties.resendCooldown())))
			.ifPresent(user -> sendVerification(user, now));
	}

	private void sendVerification(UserAccount user, Instant now) {
		tokens.invalidateAll(user.getId(), TokenPurpose.EMAIL_VERIFICATION, now);
		String token = SecureTokens.generate();
		tokens.save(OneTimeToken.issue(user.getId(), TokenPurpose.EMAIL_VERIFICATION, SecureTokens.hash(token), now,
				properties.verificationTokenTtl()));
		mails.emailVerification(user, token);
	}

}
