package com.novacore.project.api;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

final class ProjectDtos {

	private ProjectDtos() {
	}

	record ProjectRequest(
			@NotBlank(message = "Le nom du projet est obligatoire.") @Size(max = 120) String name,
			@Size(max = 500) String problem,
			@Size(max = 5000) String description,
			@Size(max = 2000) String objectives,
			@Size(max = 2000) String features,
			@Positive(message = "La taille d'équipe doit être positive.") Integer teamSizeLimit,
			@Size(max = 300) String repoUrl,
			@Size(max = 300) String demoUrl,
			List<UUID> domainIds,
			List<UUID> technologyIds) {
	}

	record StatusRequest(@NotBlank(message = "Le statut est obligatoire.") String status) {
	}

	record VisibilityRequest(@NotBlank(message = "La visibilité est obligatoire.") String visibility,
			boolean applicationsOpen) {
	}

	record RoleRequest(@NotBlank(message = "L'intitulé du rôle est obligatoire.") @Size(max = 80) String title,
			@Size(max = 1000) String description, @Positive Integer headcount, List<UUID> requiredTechnologyIds) {
	}

	record RolesRequest(@Valid List<RoleRequest> roles) {
	}

	record ApplyRequest(UUID roleId, @Size(max = 1000) String message) {
	}

	record MemberRoleRequest(@NotBlank(message = "Le rôle est obligatoire.") String role) {
	}

	record TransferRequest(@NotNull(message = "Le nouveau responsable est obligatoire.") UUID newOwnerId) {
	}

}
