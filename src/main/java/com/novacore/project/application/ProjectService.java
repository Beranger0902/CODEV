package com.novacore.project.application;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.novacore.catalog.Catalog;
import com.novacore.project.application.ProjectInputs.ProjectDetails;
import com.novacore.project.application.ProjectInputs.RoleInput;
import com.novacore.project.application.ProjectInputs.RolesUpdate;
import com.novacore.project.application.ProjectViews.ProjectDetail;
import com.novacore.project.application.ProjectViews.ProjectSummary;
import com.novacore.project.domain.Project;
import com.novacore.project.domain.ProjectRepository;
import com.novacore.project.domain.ProjectRole;
import com.novacore.project.domain.ProjectStatus;
import com.novacore.project.domain.ProjectVisibility;
import com.novacore.shared.audit.AuditLogger;
import com.novacore.shared.error.ApiException;
import com.novacore.shared.web.ClientInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class ProjectService {

	private static final int MAX_ROLES = 20;

	private final ProjectRepository projects;

	private final Catalog catalog;

	private final ProjectAssembler assembler;

	private final AuditLogger audit;

	private final Clock clock;

	ProjectService(ProjectRepository projects, Catalog catalog, ProjectAssembler assembler, AuditLogger audit,
			Clock clock) {
		this.projects = projects;
		this.catalog = catalog;
		this.assembler = assembler;
		this.audit = audit;
		this.clock = clock;
	}

	public ProjectDetail create(UUID ownerId, ProjectDetails details, ClientInfo client) {
		requireName(details.name());
		Project project = Project.create(ownerId, details.name().trim(), clock.instant());
		applyDetails(project, details);
		projects.save(project);
		audit.record(ownerId, "PROJECT_CREATED", "PROJECT", project.getId(), client);
		return assembler.toDetail(project, ownerId);
	}

	@Transactional(readOnly = true)
	public ProjectDetail get(UUID projectId, UUID viewerId) {
		return assembler.toDetail(loadVisible(projectId, viewerId), viewerId);
	}

	@Transactional(readOnly = true)
	public Page<ProjectSummary> discover(String status, UUID domainId, UUID technologyId, String search,
			Pageable pageable) {
		ProjectStatus parsedStatus = StringUtils.hasText(status)
				? parse(ProjectStatus.class, status, "INVALID_STATUS") : null;
		String term = StringUtils.hasText(search) ? search.trim() : null;
		return projects.discover(parsedStatus, domainId, technologyId, term, pageable).map(assembler::toSummary);
	}

	@Transactional(readOnly = true)
	public Page<ProjectSummary> myProjects(UUID userId, Pageable pageable) {
		return projects.findByMember(userId, pageable).map(assembler::toSummary);
	}

	public ProjectDetail updateDetails(UUID projectId, UUID actorId, ProjectDetails details, ClientInfo client) {
		requireName(details.name());
		Project project = loadManageable(projectId, actorId);
		applyDetails(project, details);
		audit.record(actorId, "PROJECT_UPDATED", "PROJECT", projectId, client);
		return assembler.toDetail(project, actorId);
	}

	public ProjectDetail changeStatus(UUID projectId, UUID actorId, String status, ClientInfo client) {
		Project project = loadManageable(projectId, actorId);
		ProjectStatus target = parse(ProjectStatus.class, status, "INVALID_STATUS");
		project.changeStatus(target);
		audit.record(actorId, "PROJECT_STATUS_CHANGED", "PROJECT", projectId, client, target.name());
		return assembler.toDetail(project, actorId);
	}

	public ProjectDetail updateVisibility(UUID projectId, UUID actorId, String visibility, boolean applicationsOpen,
			ClientInfo client) {
		Project project = loadManageable(projectId, actorId);
		project.updateVisibility(parse(ProjectVisibility.class, visibility, "INVALID_VISIBILITY"));
		project.setApplicationsOpen(applicationsOpen);
		return assembler.toDetail(project, actorId);
	}

	public ProjectDetail replaceRoles(UUID projectId, UUID actorId, RolesUpdate update, ClientInfo client) {
		List<RoleInput> inputs = update.roles() != null ? update.roles() : List.of();
		if (inputs.size() > MAX_ROLES) {
			throw ApiException.badRequest("TOO_MANY_ROLES", "Pas plus de " + MAX_ROLES + " rôles.");
		}
		Project project = loadManageable(projectId, actorId);
		List<ProjectRole> roles = new ArrayList<>();
		int order = 0;
		for (RoleInput input : inputs) {
			if (!StringUtils.hasText(input.title())) {
				throw ApiException.badRequest("INVALID_ROLE", "Chaque rôle doit avoir un intitulé.");
			}
			List<UUID> techIds = input.requiredTechnologyIds() != null ? input.requiredTechnologyIds() : List.of();
			validateTechnologies(techIds);
			int headcount = input.headcount() != null && input.headcount() > 0 ? input.headcount() : 1;
			roles.add(project.newRole(input.title().trim(), trimToNull(input.description()), headcount, order++,
					techIds));
		}
		project.replaceRoles(roles);
		return assembler.toDetail(project, actorId);
	}

	// --- helpers de chargement et d'autorisation ---

	Project loadVisible(UUID projectId, UUID viewerId) {
		Project project = projects.findById(projectId)
			.orElseThrow(() -> ApiException.notFound("PROJECT_NOT_FOUND", "Projet introuvable."));
		if (!project.isVisibleTo(viewerId)) {
			throw ApiException.notFound("PROJECT_NOT_FOUND", "Projet introuvable.");
		}
		return project;
	}

	Project loadManageable(UUID projectId, UUID actorId) {
		Project project = loadVisible(projectId, actorId);
		if (!project.canManage(actorId)) {
			throw ApiException.forbidden("NOT_PROJECT_MANAGER",
					"Seuls le responsable et les mainteneurs peuvent effectuer cette action.");
		}
		return project;
	}

	private void applyDetails(Project project, ProjectDetails d) {
		project.updateDetails(d.name().trim(), trimToNull(d.problem()), trimToNull(d.description()),
				trimToNull(d.objectives()), trimToNull(d.features()), d.teamSizeLimit(), trimToNull(d.repoUrl()),
				trimToNull(d.demoUrl()));
		List<UUID> domainIds = d.domainIds() != null ? d.domainIds() : List.of();
		List<UUID> technologyIds = d.technologyIds() != null ? d.technologyIds() : List.of();
		validateDomains(domainIds);
		validateTechnologies(technologyIds);
		project.replaceDomains(domainIds);
		project.replaceTechnologies(technologyIds);
	}

	private void validateDomains(Collection<UUID> ids) {
		if (!ids.isEmpty() && catalog.existingDomainIds(ids).size() != distinctCount(ids)) {
			throw ApiException.badRequest("UNKNOWN_DOMAIN", "Un ou plusieurs domaines sont inconnus.");
		}
	}

	private void validateTechnologies(Collection<UUID> ids) {
		if (!ids.isEmpty() && catalog.existingTechnologyIds(ids).size() != distinctCount(ids)) {
			throw ApiException.badRequest("UNKNOWN_TECHNOLOGY", "Une ou plusieurs technologies sont inconnues.");
		}
	}

	private static long distinctCount(Collection<UUID> ids) {
		return ids.stream().distinct().count();
	}

	private static void requireName(String name) {
		if (!StringUtils.hasText(name)) {
			throw ApiException.badRequest("INVALID_NAME", "Le nom du projet est obligatoire.");
		}
	}

	private static String trimToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}

	private static <E extends Enum<E>> E parse(Class<E> type, String value, String code) {
		try {
			return Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException | NullPointerException ex) {
			throw ApiException.badRequest(code, "Valeur invalide : " + value);
		}
	}

}
