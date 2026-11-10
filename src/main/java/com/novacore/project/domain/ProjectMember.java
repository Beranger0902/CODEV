package com.novacore.project.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "project_members")
public class ProjectMember {

	@Id
	private UUID id;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MemberRole role;

	@Column(nullable = false)
	private Instant joinedAt;

	protected ProjectMember() {
	}

	ProjectMember(UUID userId, MemberRole role, Instant joinedAt) {
		this.id = UUID.randomUUID();
		this.userId = userId;
		this.role = role;
		this.joinedAt = joinedAt;
	}

	void changeRole(MemberRole role) {
		this.role = role;
	}

	public UUID getUserId() {
		return userId;
	}

	public MemberRole getRole() {
		return role;
	}

	public Instant getJoinedAt() {
		return joinedAt;
	}

}
