package com.novacore.profile.application;

import java.util.List;
import java.util.UUID;

/**
 * Commandes de mise à jour reçues par le service (valeurs d'énumération en texte, validées ici).
 */
public final class ProfileInputs {

	private ProfileInputs() {
	}

	public record CoreUpdate(String headline, String bio, String countryCode, String city, String timezone,
			String experienceLevel, String availability, boolean openToMentoring, String learningGoals,
			String collaborationPreferences) {
	}

	public record VisibilityUpdate(String visibility, boolean locationVisible) {
	}

	public record SkillInput(UUID technologyId, String level) {
	}

	public record LinkInput(String type, String url, int displayOrder) {
	}

	public record SkillsUpdate(List<SkillInput> skills) {
	}

	public record DomainsUpdate(List<UUID> domainIds) {
	}

	public record LinksUpdate(List<LinkInput> links) {
	}

}
