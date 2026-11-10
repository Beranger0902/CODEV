package com.novacore.shared.audit;

import java.time.Clock;
import java.util.UUID;

import com.novacore.shared.web.ClientInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Trace les opérations sensibles. S'exécute dans la transaction de l'appelant :
 * l'entrée d'audit est enregistrée si et seulement si l'opération l'est.
 */
@Service
public class AuditLogger {

	private static final int MAX_DETAILS_LENGTH = 500;

	private final AuditEntryRepository entries;

	private final Clock clock;

	AuditLogger(AuditEntryRepository entries, Clock clock) {
		this.entries = entries;
		this.clock = clock;
	}

	@Transactional
	public void record(UUID actorId, String action, String targetType, UUID targetId, ClientInfo client) {
		record(actorId, action, targetType, targetId, client, null);
	}

	@Transactional
	public void record(UUID actorId, String action, String targetType, UUID targetId, ClientInfo client,
			String details) {
		if (details != null && details.length() > MAX_DETAILS_LENGTH) {
			details = details.substring(0, MAX_DETAILS_LENGTH);
		}
		entries.save(new AuditEntry(clock.instant(), actorId, action, targetType, targetId, client.ipAddress(),
				details));
	}

}
