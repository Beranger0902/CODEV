package com.novacore.project.domain;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectTest {

	private final Instant now = Instant.parse("2026-11-10T10:00:00Z");

	private final UUID owner = UUID.randomUUID();

	private final UUID member = UUID.randomUUID();

	@Test
	void creatorBecomesOwnerMember() {
		Project project = Project.create(owner, "NovaCore", now);

		assertThat(project.isOwner(owner)).isTrue();
		assertThat(project.memberOf(owner)).get().extracting(ProjectMember::getRole).isEqualTo(MemberRole.OWNER);
		assertThat(project.canManage(owner)).isTrue();
	}

	@Test
	void teamSizeLimitIsRespected() {
		Project project = Project.create(owner, "NovaCore", now);
		project.updateDetails("NovaCore", null, null, null, null, 1, null, null);

		assertThat(project.isFull()).isTrue();
	}

	@Test
	void contributorCannotManage() {
		Project project = Project.create(owner, "NovaCore", now);
		project.addMember(member, MemberRole.CONTRIBUTOR, now);

		assertThat(project.canManage(member)).isFalse();
		assertThat(project.isMember(member)).isTrue();
	}

	@Test
	void transferOwnershipSwapsRoles() {
		Project project = Project.create(owner, "NovaCore", now);
		project.addMember(member, MemberRole.CONTRIBUTOR, now);

		project.transferOwnership(member);

		assertThat(project.isOwner(member)).isTrue();
		assertThat(project.memberOf(member)).get().extracting(ProjectMember::getRole).isEqualTo(MemberRole.OWNER);
		assertThat(project.memberOf(owner)).get().extracting(ProjectMember::getRole).isEqualTo(MemberRole.MAINTAINER);
	}

	@Test
	void privateProjectVisibleOnlyToTeam() {
		Project project = Project.create(owner, "NovaCore", now);
		project.updateVisibility(ProjectVisibility.PRIVATE);

		assertThat(project.isVisibleTo(owner)).isTrue();
		assertThat(project.isVisibleTo(member)).isFalse();
		assertThat(project.isVisibleTo(null)).isFalse();
	}

	@Test
	void acceptsApplicationsOnlyWhenOpenAndRecruiting() {
		Project project = Project.create(owner, "NovaCore", now);
		assertThat(project.acceptsApplications()).isFalse(); // DRAFT

		project.changeStatus(ProjectStatus.RECRUITING);
		assertThat(project.acceptsApplications()).isTrue();

		project.setApplicationsOpen(false);
		assertThat(project.acceptsApplications()).isFalse();
	}

}
