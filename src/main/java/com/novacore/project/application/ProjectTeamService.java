package com.novacore.project.application;

import java.time.Clock;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.novacore.project.application.ProjectInputs.ApplicationInput;
import com.novacore.project.application.ProjectViews.ApplicationView;
import com.novacore.project.application.ProjectViews.MemberView;
import com.novacore.project.domain.ApplicationStatus;
import com.novacore.project.domain.MemberRole;
import com.novacore.project.domain.Project;
import com.novacore.project.domain.ProjectApplication;
import com.novacore.project.domain.ProjectApplicationRepository;
import com.novacore.shared.audit.AuditLogger;
import com.novacore.shared.error.ApiException;
import com.novacore.shared.web.ClientInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** Candidatures et gestion des membres d'un projet. */
@Service
@Transactional
public class ProjectTeamService {

	private final ProjectService projects;

	private final ProjectApplicationRepository applications;

	private final ProjectAssembler assembler;

	private final AuditLogger audit;

	private final Clock clock;

	ProjectTeamService(ProjectService projects, ProjectApplicationRepository applications, ProjectAssembler assembler,
			AuditLogger audit, Clock clock) {
		this.projects = projects;
		this.applications = applications;
		this.assembler = assembler;
		this.audit = audit;
		this.clock = clock;
	}

	// --- candidatures ---

	public ApplicationView apply(UUID projectId, UUID applicantId, ApplicationInput input, ClientInfo client) {
		Project project = projects.loadVisible(projectId, applicantId);
		if (!project.acceptsApplications()) {
			throw ApiException.conflict("APPLICATIONS_CLOSED", "Ce projet n'accepte pas de candidatures.");
		}
		if (project.isMember(applicantId)) {
			throw ApiException.conflict("ALREADY_MEMBER", "Vous êtes déjà membre de ce projet.");
		}
		applications.findByProjectIdAndApplicantIdAndStatus(projectId, applicantId, ApplicationStatus.PENDING)
			.ifPresent(a -> {
				throw ApiException.conflict("ALREADY_APPLIED", "Vous avez déjà une candidature en attente.");
			});
		if (input.roleId() != null && project.getRoles().stream().noneMatch(r -> r.getId().equals(input.roleId()))) {
			throw ApiException.badRequest("UNKNOWN_ROLE", "Le rôle visé n'existe pas dans ce projet.");
		}
		ProjectApplication application = applications.save(ProjectApplication.submit(projectId, applicantId,
				input.roleId(), trimToNull(input.message()), clock.instant()));
		audit.record(applicantId, "PROJECT_APPLICATION_SUBMITTED", "PROJECT", projectId, client);
		return assembler.toApplicationView(application, project);
	}

	@Transactional(readOnly = true)
	public List<ApplicationView> listApplications(UUID projectId, UUID actorId) {
		Project project = projects.loadManageable(projectId, actorId);
		return applications.findByProjectIdOrderByCreatedAtDesc(projectId)
			.stream()
			.map(a -> assembler.toApplicationView(a, project))
			.toList();
	}

	public void accept(UUID applicationId, UUID actorId, ClientInfo client) {
		ProjectApplication application = pendingApplication(applicationId);
		Project project = projects.loadManageable(application.getProjectId(), actorId);
		if (project.isMember(application.getApplicantId())) {
			application.reject(actorId, clock.instant()); // déjà membre : on clôt proprement
			return;
		}
		if (project.isFull()) {
			throw ApiException.conflict("TEAM_FULL", "L'équipe a atteint sa taille maximale.");
		}
		project.addMember(application.getApplicantId(), MemberRole.CONTRIBUTOR, clock.instant());
		application.accept(actorId, clock.instant());
		audit.record(actorId, "PROJECT_APPLICATION_ACCEPTED", "PROJECT", project.getId(), client,
				"applicant=" + application.getApplicantId());
	}

	public void reject(UUID applicationId, UUID actorId, ClientInfo client) {
		ProjectApplication application = pendingApplication(applicationId);
		projects.loadManageable(application.getProjectId(), actorId);
		application.reject(actorId, clock.instant());
		audit.record(actorId, "PROJECT_APPLICATION_REJECTED", "PROJECT", application.getProjectId(), client);
	}

	public void withdraw(UUID applicationId, UUID applicantId, ClientInfo client) {
		ProjectApplication application = pendingApplication(applicationId);
		if (!application.getApplicantId().equals(applicantId)) {
			throw ApiException.notFound("APPLICATION_NOT_FOUND", "Candidature introuvable.");
		}
		application.withdraw(clock.instant());
		audit.record(applicantId, "PROJECT_APPLICATION_WITHDRAWN", "PROJECT", application.getProjectId(), client);
	}

	// --- membres ---

	@Transactional(readOnly = true)
	public List<MemberView> listMembers(UUID projectId, UUID viewerId) {
		return projects.loadVisible(projectId, viewerId).getMembers().stream().map(assembler::toMemberView).toList();
	}

	/** Un membre peut quitter le projet ; un responsable peut retirer un autre membre. Le OWNER ne peut pas partir sans transfert. */
	public void removeMember(UUID projectId, UUID actorId, UUID targetUserId, ClientInfo client) {
		Project project = projects.loadVisible(projectId, actorId);
		boolean leaving = actorId.equals(targetUserId);
		if (!leaving && !project.canManage(actorId)) {
			throw ApiException.forbidden("NOT_PROJECT_MANAGER", "Action réservée aux responsables du projet.");
		}
		if (!project.isMember(targetUserId)) {
			throw ApiException.notFound("MEMBER_NOT_FOUND", "Ce membre n'existe pas.");
		}
		if (project.isOwner(targetUserId)) {
			throw ApiException.conflict("OWNER_CANNOT_LEAVE",
					"Le responsable doit transférer la responsabilité avant de quitter le projet.");
		}
		project.removeMember(targetUserId);
		audit.record(actorId, leaving ? "PROJECT_MEMBER_LEFT" : "PROJECT_MEMBER_REMOVED", "PROJECT", projectId, client,
				"member=" + targetUserId);
	}

	/** Seul le responsable modifie les rôles ; le rôle OWNER se transmet via le transfert de responsabilité. */
	public void changeMemberRole(UUID projectId, UUID actorId, UUID targetUserId, String role, ClientInfo client) {
		Project project = projects.loadVisible(projectId, actorId);
		if (!project.isOwner(actorId)) {
			throw ApiException.forbidden("NOT_PROJECT_OWNER", "Seul le responsable peut modifier les rôles.");
		}
		MemberRole target = parseRole(role);
		if (target == MemberRole.OWNER) {
			throw ApiException.badRequest("USE_TRANSFER",
					"Pour désigner un nouveau responsable, utilisez le transfert de responsabilité.");
		}
		if (!project.isMember(targetUserId) || project.isOwner(targetUserId)) {
			throw ApiException.badRequest("INVALID_TARGET", "Membre invalide pour ce changement de rôle.");
		}
		project.changeMemberRole(targetUserId, target);
		audit.record(actorId, "PROJECT_MEMBER_ROLE_CHANGED", "PROJECT", projectId, client,
				"member=" + targetUserId + " role=" + target);
	}

	public void transferOwnership(UUID projectId, UUID actorId, UUID newOwnerId, ClientInfo client) {
		Project project = projects.loadVisible(projectId, actorId);
		if (!project.isOwner(actorId)) {
			throw ApiException.forbidden("NOT_PROJECT_OWNER", "Seul le responsable peut transférer la responsabilité.");
		}
		if (!project.isMember(newOwnerId)) {
			throw ApiException.badRequest("NOT_A_MEMBER", "Le nouveau responsable doit déjà être membre du projet.");
		}
		if (actorId.equals(newOwnerId)) {
			throw ApiException.badRequest("ALREADY_OWNER", "Vous êtes déjà le responsable.");
		}
		project.transferOwnership(newOwnerId);
		audit.record(actorId, "PROJECT_OWNERSHIP_TRANSFERRED", "PROJECT", projectId, client, "to=" + newOwnerId);
	}

	private ProjectApplication pendingApplication(UUID applicationId) {
		ProjectApplication application = applications.findById(applicationId)
			.orElseThrow(() -> ApiException.notFound("APPLICATION_NOT_FOUND", "Candidature introuvable."));
		if (!application.isPending()) {
			throw ApiException.conflict("APPLICATION_NOT_PENDING", "Cette candidature a déjà été traitée.");
		}
		return application;
	}

	private static MemberRole parseRole(String role) {
		try {
			return MemberRole.valueOf(role.trim().toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException | NullPointerException ex) {
			throw ApiException.badRequest("INVALID_ROLE", "Rôle invalide : " + role);
		}
	}

	private static String trimToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}

}
