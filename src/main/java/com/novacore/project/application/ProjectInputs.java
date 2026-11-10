package com.novacore.project.application;

import java.util.List;
import java.util.UUID;

public final class ProjectInputs {

	private ProjectInputs() {
	}

	public record ProjectDetails(String name, String problem, String description, String objectives, String features,
			Integer teamSizeLimit, String repoUrl, String demoUrl, List<UUID> domainIds, List<UUID> technologyIds) {
	}

	public record RoleInput(String title, String description, Integer headcount, List<UUID> requiredTechnologyIds) {
	}

	public record RolesUpdate(List<RoleInput> roles) {
	}

	public record ApplicationInput(UUID roleId, String message) {
	}

}
