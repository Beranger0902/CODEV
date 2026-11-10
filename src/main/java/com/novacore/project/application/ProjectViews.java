package com.novacore.project.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.novacore.catalog.DomainView;
import com.novacore.catalog.TechnologyView;

public final class ProjectViews {

	private ProjectViews() {
	}

	public record ProjectSummary(UUID id, String name, String problem, String status, String ownerUsername,
			List<DomainView> domains, List<TechnologyView> technologies, int memberCount, int openRoleCount) {
	}

	public record RoleView(UUID id, String title, String description, int headcount,
			List<TechnologyView> requiredTechnologies) {
	}

	public record MemberView(UUID userId, String username, String displayName, String role, Instant joinedAt) {
	}

	/**
	 * @param viewerRole rôle du demandeur dans ce projet (null s'il n'est pas membre)
	 * @param canManage true si le demandeur peut gérer le projet (OWNER ou MAINTAINER)
	 */
	public record ProjectDetail(UUID id, String name, String problem, String description, String objectives,
			String features, String status, String visibility, Integer teamSizeLimit, String repoUrl, String demoUrl,
			boolean applicationsOpen, UUID ownerId, List<DomainView> domains, List<TechnologyView> technologies,
			List<RoleView> roles, List<MemberView> members, String viewerRole, boolean canManage) {
	}

	public record ApplicationView(UUID id, UUID projectId, UUID applicantId, String applicantUsername,
			String applicantDisplayName, UUID roleId, String roleTitle, String message, String status,
			Instant createdAt) {
	}

}
