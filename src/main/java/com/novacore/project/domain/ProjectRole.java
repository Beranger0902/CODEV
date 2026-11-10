package com.novacore.project.domain;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

/** Rôle recherché par le projet, avec compétences (technologies) requises. */
@Entity
@Table(name = "project_roles")
public class ProjectRole {

	@Id
	private UUID id;

	@Column(nullable = false)
	private String title;

	private String description;

	@Column(nullable = false)
	private int headcount;

	@Column(nullable = false)
	private int displayOrder;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "project_role_skills", joinColumns = @JoinColumn(name = "role_id"))
	@Column(name = "technology_id", nullable = false)
	private Set<UUID> requiredTechnologyIds = new LinkedHashSet<>();

	protected ProjectRole() {
	}

	ProjectRole(String title, String description, int headcount, int displayOrder,
			Collection<UUID> requiredTechnologyIds) {
		this.id = UUID.randomUUID();
		this.title = title;
		this.description = description;
		this.headcount = headcount;
		this.displayOrder = displayOrder;
		this.requiredTechnologyIds.addAll(requiredTechnologyIds);
	}

	public UUID getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public int getHeadcount() {
		return headcount;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

	public Set<UUID> getRequiredTechnologyIds() {
		return requiredTechnologyIds;
	}

}
