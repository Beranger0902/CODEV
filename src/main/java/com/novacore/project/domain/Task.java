package com.novacore.project.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "tasks")
public class Task {

	@Id
	private UUID id;

	@Column(nullable = false, updatable = false)
	private UUID projectId;

	private UUID milestoneId;

	@Column(nullable = false)
	private String title;

	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TaskStatus status = TaskStatus.TODO;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TaskPriority priority = TaskPriority.MEDIUM;

	private UUID assigneeId;

	private LocalDate dueDate;

	@Column(nullable = false)
	private int boardOrder;

	@Column(nullable = false, updatable = false)
	private UUID createdBy;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "task_dependencies", joinColumns = @JoinColumn(name = "task_id"))
	@Column(name = "depends_on_task_id", nullable = false)
	private Set<UUID> dependsOn = new LinkedHashSet<>();

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(nullable = false)
	private Instant updatedAt;

	@Version
	private long version;

	protected Task() {
	}

	public static Task create(UUID projectId, String title, UUID createdBy, int boardOrder) {
		Task task = new Task();
		task.id = UUID.randomUUID();
		task.projectId = projectId;
		task.title = title;
		task.createdBy = createdBy;
		task.boardOrder = boardOrder;
		return task;
	}

	public void updateDetails(String title, String description, TaskPriority priority, UUID assigneeId,
			LocalDate dueDate, UUID milestoneId) {
		this.title = title;
		this.description = description;
		this.priority = priority;
		this.assigneeId = assigneeId;
		this.dueDate = dueDate;
		this.milestoneId = milestoneId;
	}

	public void moveTo(TaskStatus status, int boardOrder) {
		this.status = status;
		this.boardOrder = boardOrder;
	}

	public void replaceDependencies(Collection<UUID> dependencies) {
		dependsOn.clear();
		dependsOn.addAll(dependencies);
	}

	public UUID getId() {
		return id;
	}

	public UUID getProjectId() {
		return projectId;
	}

	public UUID getMilestoneId() {
		return milestoneId;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public TaskStatus getStatus() {
		return status;
	}

	public TaskPriority getPriority() {
		return priority;
	}

	public UUID getAssigneeId() {
		return assigneeId;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public int getBoardOrder() {
		return boardOrder;
	}

	public UUID getCreatedBy() {
		return createdBy;
	}

	public Set<UUID> getDependsOn() {
		return dependsOn;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

}
