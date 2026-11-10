package com.novacore.project.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "milestones")
public class Milestone {

	@Id
	private UUID id;

	@Column(nullable = false, updatable = false)
	private UUID projectId;

	@Column(nullable = false)
	private String title;

	private String description;

	private LocalDate dueDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MilestoneStatus status = MilestoneStatus.OPEN;

	@Column(nullable = false)
	private int displayOrder;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(nullable = false)
	private Instant updatedAt;

	protected Milestone() {
	}

	public static Milestone create(UUID projectId, String title, String description, LocalDate dueDate,
			int displayOrder) {
		Milestone milestone = new Milestone();
		milestone.id = UUID.randomUUID();
		milestone.projectId = projectId;
		milestone.title = title;
		milestone.description = description;
		milestone.dueDate = dueDate;
		milestone.displayOrder = displayOrder;
		return milestone;
	}

	public void update(String title, String description, LocalDate dueDate) {
		this.title = title;
		this.description = description;
		this.dueDate = dueDate;
	}

	public void changeStatus(MilestoneStatus status) {
		this.status = status;
	}

	public UUID getId() {
		return id;
	}

	public UUID getProjectId() {
		return projectId;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public MilestoneStatus getStatus() {
		return status;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

}
