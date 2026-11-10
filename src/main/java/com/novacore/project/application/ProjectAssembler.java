package com.novacore.project.application;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.novacore.catalog.Catalog;
import com.novacore.catalog.TechnologyView;
import com.novacore.identity.AccountSummary;
import com.novacore.identity.Accounts;
import com.novacore.project.application.ProjectViews.ApplicationView;
import com.novacore.project.application.ProjectViews.MemberView;
import com.novacore.project.application.ProjectViews.ProjectDetail;
import com.novacore.project.application.ProjectViews.ProjectSummary;
import com.novacore.project.application.ProjectViews.RoleView;
import com.novacore.project.domain.Project;
import com.novacore.project.domain.ProjectApplication;
import com.novacore.project.domain.ProjectMember;
import com.novacore.project.domain.ProjectRole;
import org.springframework.stereotype.Component;

/** Construit les vues API en résolvant comptes (identity) et technologies/domaines (catalog). */
@Component
class ProjectAssembler {

	private final Catalog catalog;

	private final Accounts accounts;

	ProjectAssembler(Catalog catalog, Accounts accounts) {
		this.catalog = catalog;
		this.accounts = accounts;
	}

	ProjectSummary toSummary(Project project) {
		String ownerUsername = accounts.findById(project.getOwnerId()).map(AccountSummary::username).orElse(null);
		return new ProjectSummary(project.getId(), project.getName(), project.getProblem(), project.getStatus().name(),
				ownerUsername, catalog.domains(project.getDomainIds()),
				catalog.technologies(project.getTechnologyIds()), project.memberCount(), project.getRoles().size());
	}

	ProjectDetail toDetail(Project project, UUID viewerId) {
		Map<UUID, TechnologyView> techById = technologyIndex(project);

		List<RoleView> roles = project.getRoles()
			.stream()
			.map(role -> new RoleView(role.getId(), role.getTitle(), role.getDescription(), role.getHeadcount(),
					resolve(role.getRequiredTechnologyIds(), techById)))
			.toList();

		List<MemberView> members = project.getMembers().stream().map(this::toMemberView).toList();

		String viewerRole = project.memberOf(viewerId).map(m -> m.getRole().name()).orElse(null);

		return new ProjectDetail(project.getId(), project.getName(), project.getProblem(), project.getDescription(),
				project.getObjectives(), project.getFeatures(), project.getStatus().name(),
				project.getVisibility().name(), project.getTeamSizeLimit(), project.getRepoUrl(), project.getDemoUrl(),
				project.isApplicationsOpen(), project.getOwnerId(), catalog.domains(project.getDomainIds()),
				catalog.technologies(project.getTechnologyIds()), roles, members, viewerRole,
				project.canManage(viewerId));
	}

	MemberView toMemberView(ProjectMember member) {
		AccountSummary account = accounts.findById(member.getUserId()).orElse(null);
		return new MemberView(member.getUserId(), account != null ? account.username() : null,
				account != null ? account.displayName() : null, member.getRole().name(), member.getJoinedAt());
	}

	ApplicationView toApplicationView(ProjectApplication application, Project project) {
		AccountSummary applicant = accounts.findById(application.getApplicantId()).orElse(null);
		String roleTitle = application.getRoleId() == null ? null
				: project.getRoles()
					.stream()
					.filter(r -> r.getId().equals(application.getRoleId()))
					.map(ProjectRole::getTitle)
					.findFirst()
					.orElse(null);
		return new ApplicationView(application.getId(), application.getProjectId(), application.getApplicantId(),
				applicant != null ? applicant.username() : null, applicant != null ? applicant.displayName() : null,
				application.getRoleId(), roleTitle, application.getMessage(), application.getStatus().name(),
				application.getCreatedAt());
	}

	private Map<UUID, TechnologyView> technologyIndex(Project project) {
		List<UUID> ids = Stream
			.concat(project.getTechnologyIds().stream(),
					project.getRoles().stream().flatMap(r -> r.getRequiredTechnologyIds().stream()))
			.distinct()
			.toList();
		return catalog.technologies(ids).stream().collect(Collectors.toMap(TechnologyView::id, Function.identity()));
	}

	private static List<TechnologyView> resolve(Collection<UUID> ids, Map<UUID, TechnologyView> index) {
		return ids.stream().map(index::get).filter(java.util.Objects::nonNull).toList();
	}

}
