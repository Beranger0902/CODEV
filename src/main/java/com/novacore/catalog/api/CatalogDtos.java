package com.novacore.catalog.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

final class CatalogDtos {

	private CatalogDtos() {
	}

	record TechnologyRequest(
			@NotBlank(message = "Le nom est obligatoire.") @Size(max = 80) String name,
			@NotBlank(message = "La catégorie est obligatoire.") String kind) {
	}

	record DomainRequest(
			@NotBlank(message = "Le nom est obligatoire.") @Size(max = 80) String name,
			@Size(max = 300) String description,
			@PositiveOrZero(message = "L'ordre d'affichage doit être positif ou nul.") int displayOrder) {
	}

	record ActiveRequest(boolean active) {
	}

}
