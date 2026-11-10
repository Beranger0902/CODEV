package com.novacore.project.api;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

final class ProjectBoardDtos {

	private ProjectBoardDtos() {
	}

	record MilestoneRequest(@NotBlank(message = "Le titre est obligatoire.") @Size(max = 120) String title,
			@Size(max = 2000) String description, LocalDate dueDate) {
	}

	record MilestoneStatusRequest(@NotBlank(message = "Le statut est obligatoire.") String status) {
	}

	record TaskRequest(@NotBlank(message = "Le titre est obligatoire.") @Size(max = 200) String title,
			@Size(max = 5000) String description, String priority, UUID assigneeId, LocalDate dueDate,
			UUID milestoneId) {
	}

	record TaskMoveRequest(@NotBlank(message = "Le statut cible est obligatoire.") String status, Integer boardOrder) {
	}

	record DependenciesRequest(List<UUID> dependsOn) {
	}

}
