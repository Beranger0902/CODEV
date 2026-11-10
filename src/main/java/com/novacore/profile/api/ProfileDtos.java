package com.novacore.profile.api;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

final class ProfileDtos {

	private ProfileDtos() {
	}

	record CoreRequest(
			@Size(max = 120, message = "Le titre est trop long (120 caractères maximum).") String headline,
			@Size(max = 2000, message = "La biographie est trop longue (2000 caractères maximum).") String bio,
			@Size(max = 2) String countryCode,
			@Size(max = 80) String city,
			@Size(max = 40) String timezone,
			String experienceLevel,
			String availability,
			boolean openToMentoring,
			@Size(max = 1000) String learningGoals,
			@Size(max = 1000) String collaborationPreferences) {
	}

	record VisibilityRequest(@NotBlank(message = "La visibilité est obligatoire.") String visibility,
			boolean locationVisible) {
	}

	record SkillRequest(@NotNull(message = "L'identifiant de technologie est obligatoire.") UUID technologyId,
			@NotBlank(message = "Le niveau est obligatoire.") String level) {
	}

	record SkillsRequest(@Valid List<SkillRequest> skills) {
	}

	record DomainsRequest(List<UUID> domainIds) {
	}

	record LinkRequest(@NotBlank(message = "Le type de lien est obligatoire.") String type,
			@NotBlank(message = "L'URL est obligatoire.") @Size(max = 300) String url,
			@PositiveOrZero int displayOrder) {
	}

	record LinksRequest(@Valid List<LinkRequest> links) {
	}

}
