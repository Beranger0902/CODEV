package com.novacore.shared.audit;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "audit_log")
class AuditEntry {

	@Id
	private UUID id;

	@Column(nullable = false)
	private Instant occurredAt;

	private UUID actorId;

	@Column(nullable = false)
	private String action;

	private String targetType;

	private UUID targetId;

	private String ipAddress;

	private String details;

	protected AuditEntry() {
	}

	AuditEntry(Instant occurredAt, UUID actorId, String action, String targetType, UUID targetId, String ipAddress,
			String details) {
		this.id = UUID.randomUUID();
		this.occurredAt = occurredAt;
		this.actorId = actorId;
		this.action = action;
		this.targetType = targetType;
		this.targetId = targetId;
		this.ipAddress = ipAddress;
		this.details = details;
	}

}
