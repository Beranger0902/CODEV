package com.novacore.profile.application;

import java.util.List;
import java.util.UUID;

import com.novacore.catalog.DomainView;
import com.novacore.catalog.TechnologyView;

/**
 * Vues renvoyées par l'API. Les identifiants de technologies et de domaines sont résolus
 * en noms lisibles via le référentiel.
 */
public final class ProfileViews {

	private ProfileViews() {
	}

	public record SkillView(TechnologyView technology, String level) {
	}

	public record LinkView(UUID id, String type, String url, int displayOrder) {
	}

	/**
	 * @param own true si le demandeur consulte son propre profil (champs de localisation toujours inclus)
	 * @param location null quand la localisation est masquée pour ce spectateur
	 */
	public record ProfileDetail(UUID userId, String username, String displayName, String headline, String bio,
			Location location, String experienceLevel, String availability, boolean openToMentoring,
			String learningGoals, String collaborationPreferences, String visibility, boolean locationVisible,
			List<SkillView> skills, List<DomainView> domains, List<LinkView> links, boolean own) {
	}

	public record Location(String countryCode, String city, String timezone) {
	}

}
