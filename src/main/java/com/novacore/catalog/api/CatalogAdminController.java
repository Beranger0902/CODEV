package com.novacore.catalog.api;

import java.util.UUID;

import com.novacore.catalog.DomainView;
import com.novacore.catalog.TechnologyView;
import com.novacore.catalog.api.CatalogDtos.ActiveRequest;
import com.novacore.catalog.api.CatalogDtos.DomainRequest;
import com.novacore.catalog.api.CatalogDtos.TechnologyRequest;
import com.novacore.catalog.application.CatalogAdminService;
import com.novacore.shared.security.CurrentUser;
import com.novacore.shared.web.ClientInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Administration du référentiel. L'autorisation est portée par {@code CatalogAdminService}
 * (@PreAuthorize ADMIN) : la règle vit avec la logique métier, pas seulement sur la route.
 */
@RestController
@RequestMapping("/api/v1/admin/catalog")
@Tag(name = "Référentiel (administration)")
class CatalogAdminController {

	private final CatalogAdminService admin;

	CatalogAdminController(CatalogAdminService admin) {
		this.admin = admin;
	}

	@Operation(summary = "Ajouter une technologie")
	@PostMapping("/technologies")
	@ResponseStatus(HttpStatus.CREATED)
	TechnologyView createTechnology(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody TechnologyRequest request,
			HttpServletRequest http) {
		return admin.createTechnology(CurrentUser.id(jwt), request.name(), request.kind(), ClientInfo.from(http));
	}

	@Operation(summary = "Modifier une technologie")
	@PutMapping("/technologies/{id}")
	TechnologyView updateTechnology(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
			@Valid @RequestBody TechnologyRequest request, HttpServletRequest http) {
		return admin.updateTechnology(CurrentUser.id(jwt), id, request.name(), request.kind(), ClientInfo.from(http));
	}

	@Operation(summary = "Activer ou désactiver une technologie")
	@PutMapping("/technologies/{id}/active")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void setTechnologyActive(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
			@RequestBody ActiveRequest request, HttpServletRequest http) {
		admin.setTechnologyActive(CurrentUser.id(jwt), id, request.active(), ClientInfo.from(http));
	}

	@Operation(summary = "Ajouter un domaine")
	@PostMapping("/domains")
	@ResponseStatus(HttpStatus.CREATED)
	DomainView createDomain(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody DomainRequest request,
			HttpServletRequest http) {
		return admin.createDomain(CurrentUser.id(jwt), request.name(), request.description(), request.displayOrder(),
				ClientInfo.from(http));
	}

	@Operation(summary = "Modifier un domaine")
	@PutMapping("/domains/{id}")
	DomainView updateDomain(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
			@Valid @RequestBody DomainRequest request, HttpServletRequest http) {
		return admin.updateDomain(CurrentUser.id(jwt), id, request.name(), request.description(),
				request.displayOrder(), ClientInfo.from(http));
	}

	@Operation(summary = "Activer ou désactiver un domaine")
	@PutMapping("/domains/{id}/active")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void setDomainActive(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @RequestBody ActiveRequest request,
			HttpServletRequest http) {
		admin.setDomainActive(CurrentUser.id(jwt), id, request.active(), ClientInfo.from(http));
	}

}
