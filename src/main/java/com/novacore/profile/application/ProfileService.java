package com.novacore.profile.application;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import com.novacore.catalog.Catalog;
import com.novacore.identity.AccountSummary;
import com.novacore.identity.Accounts;
import com.novacore.profile.application.ProfileInputs.CoreUpdate;
import com.novacore.profile.application.ProfileInputs.DomainsUpdate;
import com.novacore.profile.application.ProfileInputs.LinkInput;
import com.novacore.profile.application.ProfileInputs.LinksUpdate;
import com.novacore.profile.application.ProfileInputs.SkillInput;
import com.novacore.profile.application.ProfileInputs.SkillsUpdate;
import com.novacore.profile.application.ProfileInputs.VisibilityUpdate;
import com.novacore.profile.application.ProfileViews.ProfileDetail;
import com.novacore.profile.domain.Availability;
import com.novacore.profile.domain.ExperienceLevel;
import com.novacore.profile.domain.LinkType;
import com.novacore.profile.domain.Profile;
import com.novacore.profile.domain.ProfileLink;
import com.novacore.profile.domain.ProfileRepository;
import com.novacore.profile.domain.ProfileSkill;
import com.novacore.profile.domain.SkillLevel;
import com.novacore.profile.domain.Visibility;
import com.novacore.shared.error.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class ProfileService {

	private static final int MAX_SKILLS = 60;

	private static final int MAX_DOMAINS = 20;

	private static final int MAX_LINKS = 15;

	private final ProfileRepository profiles;

	private final Catalog catalog;

	private final Accounts accounts;

	private final ProfileAssembler assembler;

	ProfileService(ProfileRepository profiles, Catalog catalog, Accounts accounts, ProfileAssembler assembler) {
		this.profiles = profiles;
		this.catalog = catalog;
		this.accounts = accounts;
		this.assembler = assembler;
	}

	@Transactional(readOnly = true)
	public ProfileDetail getOwn(UUID userId) {
		return detail(getOrCreate(userId), userId, true);
	}

	public ProfileDetail updateCore(UUID userId, CoreUpdate c) {
		Profile profile = getOrCreate(userId);
		profile.updateCore(trimToNull(c.headline()), trimToNull(c.bio()), normalizeCountry(c.countryCode()),
				trimToNull(c.city()), trimToNull(c.timezone()),
				parseNullable(ExperienceLevel.class, c.experienceLevel(), "INVALID_EXPERIENCE_LEVEL"),
				parseNullable(Availability.class, c.availability(), "INVALID_AVAILABILITY"), c.openToMentoring(),
				trimToNull(c.learningGoals()), trimToNull(c.collaborationPreferences()));
		return detail(profile, userId, true);
	}

	public ProfileDetail updateVisibility(UUID userId, VisibilityUpdate v) {
		Profile profile = getOrCreate(userId);
		profile.updateVisibility(parseRequired(Visibility.class, v.visibility(), "INVALID_VISIBILITY"),
				v.locationVisible());
		return detail(profile, userId, true);
	}

	public ProfileDetail replaceSkills(UUID userId, SkillsUpdate update) {
		List<SkillInput> inputs = update.skills() != null ? update.skills() : List.of();
		if (inputs.size() > MAX_SKILLS) {
			throw ApiException.badRequest("TOO_MANY_SKILLS", "Pas plus de " + MAX_SKILLS + " compétences.");
		}
		List<UUID> ids = inputs.stream().map(SkillInput::technologyId).toList();
		Set<UUID> known = catalog.existingTechnologyIds(ids);
		Set<ProfileSkill> skills = new LinkedHashSet<>();
		for (SkillInput input : inputs) {
			if (input.technologyId() == null || !known.contains(input.technologyId())) {
				throw ApiException.badRequest("UNKNOWN_TECHNOLOGY",
						"Technologie inconnue : " + input.technologyId());
			}
			skills.add(new ProfileSkill(input.technologyId(),
					parseRequired(SkillLevel.class, input.level(), "INVALID_SKILL_LEVEL")));
		}
		Profile profile = getOrCreate(userId);
		profile.replaceSkills(skills);
		return detail(profile, userId, true);
	}

	public ProfileDetail replaceDomains(UUID userId, DomainsUpdate update) {
		List<UUID> ids = update.domainIds() != null ? update.domainIds() : List.of();
		if (ids.size() > MAX_DOMAINS) {
			throw ApiException.badRequest("TOO_MANY_DOMAINS", "Pas plus de " + MAX_DOMAINS + " domaines.");
		}
		Set<UUID> known = catalog.existingDomainIds(ids);
		Set<UUID> selected = new LinkedHashSet<>();
		for (UUID id : ids) {
			if (id == null || !known.contains(id)) {
				throw ApiException.badRequest("UNKNOWN_DOMAIN", "Domaine inconnu : " + id);
			}
			selected.add(id);
		}
		Profile profile = getOrCreate(userId);
		profile.replaceDomains(selected);
		return detail(profile, userId, true);
	}

	public ProfileDetail replaceLinks(UUID userId, LinksUpdate update) {
		List<LinkInput> inputs = update.links() != null ? update.links() : List.of();
		if (inputs.size() > MAX_LINKS) {
			throw ApiException.badRequest("TOO_MANY_LINKS", "Pas plus de " + MAX_LINKS + " liens.");
		}
		List<ProfileLink> links = new ArrayList<>();
		int order = 0;
		for (LinkInput input : inputs) {
			String url = input.url() != null ? input.url().trim() : "";
			if (!isValidUrl(url)) {
				throw ApiException.badRequest("INVALID_URL", "Lien invalide : " + input.url());
			}
			LinkType type = parseRequired(LinkType.class, input.type(), "INVALID_LINK_TYPE");
			links.add(new ProfileLink(type, url, order++));
		}
		Profile profile = getOrCreate(userId);
		profile.replaceLinks(links);
		return detail(profile, userId, true);
	}

	/** Profil public d'un membre par nom d'utilisateur, en respectant sa visibilité. */
	@Transactional(readOnly = true)
	public ProfileDetail getPublic(String username, UUID viewerId) {
		AccountSummary account = accounts.findByUsername(username)
			.filter(AccountSummary::active)
			.orElseThrow(() -> ApiException.notFound("PROFILE_NOT_FOUND", "Profil introuvable."));

		Profile profile = profiles.findByUserId(account.id()).orElseGet(() -> Profile.createFor(account.id()));
		boolean own = account.id().equals(viewerId);
		if (!own && !profile.isVisibleTo(viewerId)) {
			// On ne révèle pas l'existence d'un profil masqué.
			throw ApiException.notFound("PROFILE_NOT_FOUND", "Profil introuvable.");
		}
		return assembler.toDetail(profile, account, own);
	}

	private Profile getOrCreate(UUID userId) {
		return profiles.findByUserId(userId).orElseGet(() -> profiles.save(Profile.createFor(userId)));
	}

	private ProfileDetail detail(Profile profile, UUID viewerId, boolean own) {
		AccountSummary account = accounts.findById(profile.getUserId())
			.orElseThrow(() -> ApiException.notFound("PROFILE_NOT_FOUND", "Compte introuvable."));
		return assembler.toDetail(profile, account, own);
	}

	private static boolean isValidUrl(String url) {
		return url.length() <= 300 && (url.startsWith("https://") || url.startsWith("http://")) && url.length() > 10;
	}

	private static String trimToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}

	private static String normalizeCountry(String code) {
		if (!StringUtils.hasText(code)) {
			return null;
		}
		String normalized = code.trim().toUpperCase(Locale.ROOT);
		if (normalized.length() != 2 || !normalized.chars().allMatch(Character::isLetter)) {
			throw ApiException.badRequest("INVALID_COUNTRY", "Le code pays doit suivre la norme ISO (2 lettres).");
		}
		return normalized;
	}

	private static <E extends Enum<E>> E parseNullable(Class<E> type, String value, String code) {
		return StringUtils.hasText(value) ? parseRequired(type, value, code) : null;
	}

	private static <E extends Enum<E>> E parseRequired(Class<E> type, String value, String code) {
		try {
			return Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException | NullPointerException ex) {
			throw ApiException.badRequest(code, "Valeur invalide : " + value);
		}
	}

}
