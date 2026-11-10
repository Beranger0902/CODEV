package com.novacore.catalog.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/** Domaine d'activité (web, mobile, IA…). Entité JPA, à ne pas confondre avec un paquet « domaine ». */
@Entity
@Table(name = "domains")
public class Domain {

	@Id
	private UUID id;

	@Column(nullable = false, updatable = false)
	private String slug;

	@Column(nullable = false)
	private String name;

	private String description;

	@Column(nullable = false)
	private int displayOrder;

	@Column(nullable = false)
	private boolean active;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(nullable = false)
	private Instant updatedAt;

	protected Domain() {
	}

	public static Domain create(String slug, String name, String description, int displayOrder) {
		Domain domain = new Domain();
		domain.id = UUID.randomUUID();
		domain.slug = slug;
		domain.name = name;
		domain.description = description;
		domain.displayOrder = displayOrder;
		domain.active = true;
		return domain;
	}

	public void update(String name, String description, int displayOrder) {
		this.name = name;
		this.description = description;
		this.displayOrder = displayOrder;
	}

	public void activate() {
		this.active = true;
	}

	public void deactivate() {
		this.active = false;
	}

	public UUID getId() {
		return id;
	}

	public String getSlug() {
		return slug;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

	public boolean isActive() {
		return active;
	}

}
