package com.novacore.project.api;

import java.util.List;
import java.util.UUID;

import com.novacore.project.api.ProjectDtos.ApplyRequest;
import com.novacore.project.api.ProjectDtos.MemberRoleRequest;
import com.novacore.project.api.ProjectDtos.TransferRequest;
import com.novacore.project.application.ProjectInputs.ApplicationInput;
import com.novacore.project.application.ProjectService;
import com.novacore.project.application.ProjectTeamService;
import com.novacore.project.application.ProjectViews.ApplicationView;
import com.novacore.project.application.ProjectViews.MemberView;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Candidatures et gestion des membres. Les contrôles d'accès fins sont dans ProjectTeamService. */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Projets — équipe et candidatures")
class ProjectTeamController {

	private final ProjectTeamService team;

	private final ProjectService projects;

	ProjectTeamController(ProjectTeamService team, ProjectService projects) {
		this.team = team;
		this.projects = projects;
	}

	// --- projets dont je suis membre ---

	@Operation(summary = "Mes projets (ceux dont je suis membre)")
	@GetMapping("/me/projects")
	org.springframework.data.domain.Page<com.novacore.project.application.ProjectViews.ProjectSummary> myProjects(
			@AuthenticationPrincipal Jwt jwt,
			@org.springframework.data.web.PageableDefault(size = 20) org.springframework.data.domain.Pageable pageable) {
		return projects.myProjects(CurrentUser.id(jwt), pageable);
	}

	// --- candidatures ---

	@Operation(summary = "Postuler à un projet")
	@PostMapping("/projects/{id}/applications")
	@ResponseStatus(HttpStatus.CREATED)
	ApplicationView apply(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
			@Valid @RequestBody ApplyRequest r, HttpServletRequest http) {
		return team.apply(id, CurrentUser.id(jwt), new ApplicationInput(r.roleId(), r.message()),
				ClientInfo.from(http));
	}

	@Operation(summary = "Lister les candidatures d'un projet (responsables)")
	@GetMapping("/projects/{id}/applications")
	List<ApplicationView> listApplications(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
		return team.listApplications(id, CurrentUser.id(jwt));
	}

	@Operation(summary = "Accepter une candidature (crée le membre)")
	@PostMapping("/project-applications/{applicationId}/accept")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void accept(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID applicationId, HttpServletRequest http) {
		team.accept(applicationId, CurrentUser.id(jwt), ClientInfo.from(http));
	}

	@Operation(summary = "Refuser une candidature")
	@PostMapping("/project-applications/{applicationId}/reject")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void reject(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID applicationId, HttpServletRequest http) {
		team.reject(applicationId, CurrentUser.id(jwt), ClientInfo.from(http));
	}

	@Operation(summary = "Retirer sa candidature")
	@PostMapping("/project-applications/{applicationId}/withdraw")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void withdraw(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID applicationId, HttpServletRequest http) {
		team.withdraw(applicationId, CurrentUser.id(jwt), ClientInfo.from(http));
	}

	// --- membres ---

	@Operation(summary = "Lister les membres de l'équipe")
	@GetMapping("/projects/{id}/members")
	List<MemberView> members(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
		return team.listMembers(id, CurrentUser.id(jwt));
	}

	@Operation(summary = "Retirer un membre (ou quitter le projet)")
	@DeleteMapping("/projects/{id}/members/{userId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void removeMember(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @PathVariable UUID userId,
			HttpServletRequest http) {
		team.removeMember(id, CurrentUser.id(jwt), userId, ClientInfo.from(http));
	}

	@Operation(summary = "Changer le rôle d'un membre (responsable)")
	@PutMapping("/projects/{id}/members/{userId}/role")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void changeMemberRole(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @PathVariable UUID userId,
			@Valid @RequestBody MemberRoleRequest r, HttpServletRequest http) {
		team.changeMemberRole(id, CurrentUser.id(jwt), userId, r.role(), ClientInfo.from(http));
	}

	@Operation(summary = "Transférer la responsabilité du projet")
	@PostMapping("/projects/{id}/ownership")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void transferOwnership(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
			@Valid @RequestBody TransferRequest r, HttpServletRequest http) {
		team.transferOwnership(id, CurrentUser.id(jwt), r.newOwnerId(), ClientInfo.from(http));
	}

}
