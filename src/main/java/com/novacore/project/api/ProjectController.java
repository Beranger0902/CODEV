package com.novacore.project.api;

import java.util.UUID;

import com.novacore.project.api.ProjectDtos.ProjectRequest;
import com.novacore.project.api.ProjectDtos.RolesRequest;
import com.novacore.project.api.ProjectDtos.StatusRequest;
import com.novacore.project.api.ProjectDtos.VisibilityRequest;
import com.novacore.project.application.ProjectInputs;
import com.novacore.project.application.ProjectInputs.RoleInput;
import com.novacore.project.application.ProjectService;
import com.novacore.project.application.ProjectViews.ProjectDetail;
import com.novacore.project.application.ProjectViews.ProjectSummary;
import com.novacore.shared.security.CurrentUser;
import com.novacore.shared.web.ClientInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects")
@Tag(name = "Projets")
class ProjectController {

	private final ProjectService projects;

	ProjectController(ProjectService projects) {
		this.projects = projects;
	}

	@Operation(summary = "Découvrir les projets publics (filtres : status, domainId, technologyId, search)")
	@GetMapping
	@SecurityRequirements
	Page<ProjectSummary> discover(@RequestParam(required = false) String status,
			@RequestParam(required = false) UUID domainId, @RequestParam(required = false) UUID technologyId,
			@RequestParam(required = false) String search,
			@PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
		return projects.discover(status, domainId, technologyId, search, pageable);
	}

	@Operation(summary = "Consulter une fiche projet (visibilité respectée)")
	@GetMapping("/{id}")
	@SecurityRequirements
	ProjectDetail get(@PathVariable UUID id, @AuthenticationPrincipal @Nullable Jwt jwt) {
		return projects.get(id, jwt != null ? CurrentUser.id(jwt) : null);
	}

	@Operation(summary = "Créer un projet (le créateur en devient responsable)")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	ProjectDetail create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ProjectRequest r,
			HttpServletRequest http) {
		return projects.create(CurrentUser.id(jwt), toDetails(r), ClientInfo.from(http));
	}

	@Operation(summary = "Modifier la fiche projet")
	@PutMapping("/{id}")
	ProjectDetail update(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @Valid @RequestBody ProjectRequest r,
			HttpServletRequest http) {
		return projects.updateDetails(id, CurrentUser.id(jwt), toDetails(r), ClientInfo.from(http));
	}

	@Operation(summary = "Changer le statut du projet")
	@PutMapping("/{id}/status")
	ProjectDetail changeStatus(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
			@Valid @RequestBody StatusRequest r, HttpServletRequest http) {
		return projects.changeStatus(id, CurrentUser.id(jwt), r.status(), ClientInfo.from(http));
	}

	@Operation(summary = "Modifier la visibilité et l'ouverture aux candidatures")
	@PutMapping("/{id}/visibility")
	ProjectDetail updateVisibility(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
			@Valid @RequestBody VisibilityRequest r, HttpServletRequest http) {
		return projects.updateVisibility(id, CurrentUser.id(jwt), r.visibility(), r.applicationsOpen(),
				ClientInfo.from(http));
	}

	@Operation(summary = "Remplacer la liste des rôles recherchés")
	@PutMapping("/{id}/roles")
	ProjectDetail updateRoles(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
			@Valid @RequestBody RolesRequest r, HttpServletRequest http) {
		var roles = r.roles() == null ? java.util.List.<RoleInput>of()
				: r.roles()
					.stream()
					.map(x -> new RoleInput(x.title(), x.description(), x.headcount(), x.requiredTechnologyIds()))
					.toList();
		return projects.replaceRoles(id, CurrentUser.id(jwt), new ProjectInputs.RolesUpdate(roles),
				ClientInfo.from(http));
	}

	private static ProjectInputs.ProjectDetails toDetails(ProjectRequest r) {
		return new ProjectInputs.ProjectDetails(r.name(), r.problem(), r.description(), r.objectives(), r.features(),
				r.teamSizeLimit(), r.repoUrl(), r.demoUrl(), r.domainIds(), r.technologyIds());
	}

}
