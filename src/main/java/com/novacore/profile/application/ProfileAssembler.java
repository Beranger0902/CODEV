package com.novacore.profile.application;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.novacore.catalog.Catalog;
import com.novacore.catalog.DomainView;
import com.novacore.catalog.TechnologyView;
import com.novacore.identity.AccountSummary;
import com.novacore.profile.application.ProfileViews.LinkView;
import com.novacore.profile.application.ProfileViews.Location;
import com.novacore.profile.application.ProfileViews.ProfileDetail;
import com.novacore.profile.application.ProfileViews.SkillView;
import com.novacore.profile.domain.Profile;
import com.novacore.profile.domain.ProfileSkill;
import org.springframework.stereotype.Component;

/**
 * Transforme un {@link Profile} en vue API, en résolvant technologies et domaines via le référentiel
 * et en masquant la localisation lorsque le profil ne l'autorise pas pour le spectateur.
 */
@Component
class ProfileAssembler {

	private final Catalog catalog;

	ProfileAssembler(Catalog catalog) {
		this.catalog = catalog;
	}

	ProfileDetail toDetail(Profile profile, AccountSummary account, boolean own) {
		Map<UUID, TechnologyView> techById = catalog
			.technologies(profile.getSkills().stream().map(ProfileSkill::getTechnologyId).toList())
			.stream()
			.collect(Collectors.toMap(TechnologyView::id, Function.identity()));

		List<SkillView> skills = profile.getSkills()
			.stream()
			.map(skill -> {
				TechnologyView tech = techById.get(skill.getTechnologyId());
				return tech == null ? null : new SkillView(tech, skill.getLevel().name());
			})
			.filter(Objects::nonNull)
			.toList();

		List<DomainView> domains = catalog.domains(profile.getDomainIds());

		List<LinkView> links = profile.getLinks()
			.stream()
			.map(link -> new LinkView(link.getId(), link.getType().name(), link.getUrl(), link.getDisplayOrder()))
			.toList();

		// La localisation n'est incluse que pour le propriétaire, ou si elle est marquée visible.
		Location location = (own || profile.isLocationVisible())
				? new Location(profile.getCountryCode(), profile.getCity(), profile.getTimezone()) : null;

		return new ProfileDetail(profile.getUserId(), account.username(), account.displayName(), profile.getHeadline(),
				profile.getBio(), location, name(profile.getExperienceLevel()), profile.getAvailability().name(),
				profile.isOpenToMentoring(), profile.getLearningGoals(), profile.getCollaborationPreferences(),
				profile.getVisibility().name(), profile.isLocationVisible(), skills, domains, links, own);
	}

	private static String name(Enum<?> value) {
		return value == null ? null : value.name();
	}

}
