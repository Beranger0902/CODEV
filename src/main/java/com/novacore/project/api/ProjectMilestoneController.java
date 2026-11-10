package com.novacore.project.api;

import java.util.List;
import java.util.UUID;

import com.novacore.project.api.ProjectBoardDtos.MilestoneRequest;
import com.novacore.project.api.ProjectBoardDtos.MilestoneStatusRequest;
import com.novacore.project.application.MilestoneService;
import com.novacore.project.application.ProjectBoardInputs.MilestoneInput;
import com.novacore.project.application.ProjectBoardViews.MilestoneView;
import com.novacore.shared.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Projets — jalons")
class ProjectMilestoneController {

	private final MilestoneService milestones;

	ProjectMilestoneController(MilestoneService milestones) {
		this.milestones = milestones;
	}

	@Operation(summary = "Lister les jalons d'un projet (membres)")
	@GetMapping("/projects/{id}/milestones")
	List<MilestoneView> list(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
		return milestones.list(id, CurrentUser.id(jwt));
	}

	@Operation(summary = "Créer un jalon (responsables)")
	@PostMapping("/projects/{id}/milestones")
	@ResponseStatus(HttpStatus.CREATED)
	MilestoneView create(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
			@Valid @RequestBody MilestoneRequest r) {
		return milestones.create(id, CurrentUser.id(jwt), new MilestoneInput(r.title(), r.description(), r.dueDate()));
	}

	@Operation(summary = "Modifier un jalon (responsables)")
	@PutMapping("/milestones/{milestoneId}")
	MilestoneView update(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID milestoneId,
			@Valid @RequestBody MilestoneRequest r) {
		return milestones.update(milestoneId, CurrentUser.id(jwt),
				new MilestoneInput(r.title(), r.description(), r.dueDate()));
	}

	@Operation(summary = "Changer le statut d'un jalon (OPEN/COMPLETED)")
	@PutMapping("/milestones/{milestoneId}/status")
	MilestoneView changeStatus(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID milestoneId,
			@Valid @RequestBody MilestoneStatusRequest r) {
		return milestones.changeStatus(milestoneId, CurrentUser.id(jwt), r.status());
	}

	@Operation(summary = "Supprimer un jalon (responsables)")
	@DeleteMapping("/milestones/{milestoneId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID milestoneId) {
		milestones.delete(milestoneId, CurrentUser.id(jwt));
	}

}
