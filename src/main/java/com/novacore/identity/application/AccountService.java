package com.novacore.identity.application;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

import com.novacore.identity.domain.UserAccount;
import com.novacore.identity.domain.UserAccountRepository;
import com.novacore.identity.domain.UserSession;
import com.novacore.identity.domain.UserSessionRepository;
import com.novacore.shared.audit.AuditLogger;
import com.novacore.shared.error.ApiException;
import com.novacore.shared.web.ClientInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AccountService {

	private final UserAccountRepository users;

	private final UserSessionRepository sessions;

	private final AuditLogger audit;

	private final Clock clock;

	AccountService(UserAccountRepository users, UserSessionRepository sessions, AuditLogger audit, Clock clock) {
		this.users = users;
		this.sessions = sessions;
		this.audit = audit;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public UserAccount get(UUID userId) {
		return users.findById(userId)
			.orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "Compte introuvable."));
	}

	@Transactional(readOnly = true)
	public List<UserSession> activeSessions(UUID userId) {
		return sessions.findByUserIdAndRevokedAtIsNullAndExpiresAtAfterOrderByLastUsedAtDesc(userId, clock.instant());
	}

	/** Révoque une session de l'utilisateur. Une session d'un autre compte est traitée comme inexistante. */
	public void revokeSession(UUID userId, UUID sessionId, ClientInfo client) {
		UserSession session = sessions.findById(sessionId)
			.filter(s -> s.getUserId().equals(userId))
			.orElseThrow(() -> ApiException.notFound("SESSION_NOT_FOUND", "Session introuvable."));
		session.revoke(clock.instant());
		audit.record(userId, "SESSION_REVOKED", "SESSION", sessionId, client);
	}

}
