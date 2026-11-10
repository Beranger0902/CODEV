package com.novacore.project.application;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class ProjectBoardInputs {

	private ProjectBoardInputs() {
	}

	public record MilestoneInput(String title, String description, LocalDate dueDate) {
	}

	public record TaskCreate(String title, String description, String priority, UUID assigneeId, LocalDate dueDate,
			UUID milestoneId) {
	}

	public record TaskUpdate(String title, String description, String priority, UUID assigneeId, LocalDate dueDate,
			UUID milestoneId) {
	}

	public record TaskMove(String status, Integer boardOrder) {
	}

	public record DependenciesUpdate(List<UUID> dependsOn) {
	}

}
