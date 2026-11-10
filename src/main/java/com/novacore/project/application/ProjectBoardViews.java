package com.novacore.project.application;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class ProjectBoardViews {

	private ProjectBoardViews() {
	}

	public record MilestoneView(UUID id, String title, String description, LocalDate dueDate, String status,
			int displayOrder, long taskCount, long doneCount) {
	}

	public record TaskView(UUID id, String title, String description, String status, String priority, UUID assigneeId,
			String assigneeUsername, String assigneeDisplayName, LocalDate dueDate, UUID milestoneId, int boardOrder,
			List<UUID> dependsOn, UUID createdBy, Instant createdAt) {
	}

	/** Une colonne du tableau Kanban. */
	public record BoardColumn(String status, List<TaskView> tasks) {
	}

	public record BoardView(List<BoardColumn> columns) {
	}

}
