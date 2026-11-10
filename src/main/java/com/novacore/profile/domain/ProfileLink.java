package com.novacore.profile.domain;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "profile_links")
public class ProfileLink {

	@Id
	private UUID id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private LinkType type;

	@Column(nullable = false)
	private String url;

	@Column(nullable = false)
	private int displayOrder;

	protected ProfileLink() {
	}

	public ProfileLink(LinkType type, String url, int displayOrder) {
		this.id = UUID.randomUUID();
		this.type = type;
		this.url = url;
		this.displayOrder = displayOrder;
	}

	public UUID getId() {
		return id;
	}

	public LinkType getType() {
		return type;
	}

	public String getUrl() {
		return url;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

}
