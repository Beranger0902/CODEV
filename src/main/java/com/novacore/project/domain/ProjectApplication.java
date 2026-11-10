package com.novacore.project.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Candidature à un projet. Tant qu'elle n'est pas acceptée, elle ne donne aucun accès :
 * l'acceptation crée le membre (complément §24).
 */
@Entity
@Table(name = "project_applications")
public class ProjectApplication {

	@Id
	private UUID id;

	@Column(nullable = false)
	private UUID projectId;

	@Column(nullable = false)
	private UUID applicantId;

	private UUID roleId;

	private String message;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ApplicationStatus status = ApplicationStatus.PENDING;

	@Column(nullable = false)
	private Instant createdAt;

	private UUID decidedBy;

	private Instant decidedAt;

	protected ProjectApplication() {
	}

	public static ProjectApplication submit(UUID projectId, UUID applicantId, UUID roleId, String message,
			Instant now) {
		ProjectApplication application = new ProjectApplication();
		application.id = UUID.randomUUID();
		application.projectId = projectId;
		application.applicantId = applicantId;
		application.roleId = roleId;
		application.message = message;
		application.createdAt = now;
		return application;
	}

	public boolean isPending() {
		return status == ApplicationStatus.PENDING;
	}

	public void accept(UUID deciderId, Instant now) {
		transition(ApplicationStatus.ACCEPTED, deciderId, now);
	}

	public void reject(UUID deciderId, Instant now) {
		transition(ApplicationStatus.REJECTED, deciderId, now);
	}

	public void withdraw(Instant now) {
		transition(ApplicationStatus.WITHDRAWN, applicantId, now);
	}

	private void transition(ApplicationStatus target, UUID deciderId, Instant now) {
		this.status = target;
		this.decidedBy = deciderId;
		this.decidedAt = now;
	}

	public UUID getId() {
		return id;
	}

	public UUID getProjectId() {
		return projectId;
	}

	public UUID getApplicantId() {
		return applicantId;
	}

	public UUID getRoleId() {
		return roleId;
	}

	public String getMessage() {
		return message;
	}

	public ApplicationStatus getStatus() {
		return status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

}
