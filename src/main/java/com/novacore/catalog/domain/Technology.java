package com.novacore.catalog.domain;

import java.time.Instant;
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
@Table(name = "technologies")
public class Technology {

	@Id
	private UUID id;

	@Column(nullable = false, updatable = false)
	private String slug;

	@Column(nullable = false)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TechnologyKind kind;

	@Column(nullable = false)
	private boolean active;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(nullable = false)
	private Instant updatedAt;

	protected Technology() {
	}

	public static Technology create(String slug, String name, TechnologyKind kind) {
		Technology technology = new Technology();
		technology.id = UUID.randomUUID();
		technology.slug = slug;
		technology.name = name;
		technology.kind = kind;
		technology.active = true;
		return technology;
	}

	public void rename(String name) {
		this.name = name;
	}

	public void changeKind(TechnologyKind kind) {
		this.kind = kind;
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

	public TechnologyKind getKind() {
		return kind;
	}

	public boolean isActive() {
		return active;
	}

}
