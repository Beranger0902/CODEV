package com.novacore.profile.domain;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProfileTest {

	private final UUID owner = UUID.randomUUID();

	private final UUID otherMember = UUID.randomUUID();

	@Test
	void publicProfileVisibleToEveryoneIncludingAnonymous() {
		Profile profile = Profile.createFor(owner);
		profile.updateVisibility(Visibility.PUBLIC, true);

		assertThat(profile.isVisibleTo(null)).isTrue();
		assertThat(profile.isVisibleTo(otherMember)).isTrue();
	}

	@Test
	void membersOnlyProfileHiddenFromAnonymous() {
		Profile profile = Profile.createFor(owner);
		profile.updateVisibility(Visibility.MEMBERS_ONLY, true);

		assertThat(profile.isVisibleTo(null)).isFalse();
		assertThat(profile.isVisibleTo(otherMember)).isTrue();
	}

	@Test
	void privateProfileVisibleOnlyToOwner() {
		Profile profile = Profile.createFor(owner);
		profile.updateVisibility(Visibility.PRIVATE, true);

		assertThat(profile.isVisibleTo(owner)).isTrue();
		assertThat(profile.isVisibleTo(otherMember)).isFalse();
		assertThat(profile.isVisibleTo(null)).isFalse();
	}

	@Test
	void replaceSkillsKeepsLatestEntryPerTechnology() {
		Profile profile = Profile.createFor(owner);
		UUID java = UUID.randomUUID();

		profile.replaceSkills(List.of(new ProfileSkill(java, SkillLevel.BEGINNER)));
		profile.replaceSkills(List.of(new ProfileSkill(java, SkillLevel.EXPERT)));

		assertThat(profile.getSkills()).hasSize(1);
		assertThat(profile.getSkills().iterator().next().getLevel()).isEqualTo(SkillLevel.EXPERT);
	}

}
