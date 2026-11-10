package com.novacore.profile.domain;

import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/** Compétence déclarée : une technologie du référentiel associée à un niveau auto-évalué. */
@Embeddable
public class ProfileSkill {

	@Column(name = "technology_id", nullable = false)
	private UUID technologyId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SkillLevel level;

	protected ProfileSkill() {
	}

	public ProfileSkill(UUID technologyId, SkillLevel level) {
		this.technologyId = technologyId;
		this.level = level;
	}

	public UUID getTechnologyId() {
		return technologyId;
	}

	public SkillLevel getLevel() {
		return level;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ProfileSkill other)) {
			return false;
		}
		return Objects.equals(technologyId, other.technologyId);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(technologyId);
	}

}
