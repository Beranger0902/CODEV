package com.novacore.profile.api;

import java.util.List;

import com.novacore.profile.api.ProfileDtos.CoreRequest;
import com.novacore.profile.api.ProfileDtos.DomainsRequest;
import com.novacore.profile.api.ProfileDtos.LinksRequest;
import com.novacore.profile.api.ProfileDtos.SkillsRequest;
import com.novacore.profile.api.ProfileDtos.VisibilityRequest;
import com.novacore.profile.application.ProfileInputs;
import com.novacore.profile.application.ProfileInputs.LinkInput;
import com.novacore.profile.application.ProfileInputs.SkillInput;
import com.novacore.profile.application.ProfileService;
import com.novacore.profile.application.ProfileViews.ProfileDetail;
import com.novacore.shared.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Gestion de son propre profil (utilisateur connecté). */
@RestController
@RequestMapping("/api/v1/me/profile")
@Tag(name = "Mon profil")
class MyProfileController {

	private final ProfileService profiles;

	MyProfileController(ProfileService profiles) {
		this.profiles = profiles;
	}

	@Operation(summary = "Consulter son profil")
	@GetMapping
	ProfileDetail myProfile(@AuthenticationPrincipal Jwt jwt) {
		return profiles.getOwn(CurrentUser.id(jwt));
	}

	@Operation(summary = "Mettre à jour les informations principales")
	@PutMapping
	ProfileDetail updateCore(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CoreRequest r) {
		return profiles.updateCore(CurrentUser.id(jwt),
				new ProfileInputs.CoreUpdate(r.headline(), r.bio(), r.countryCode(), r.city(), r.timezone(),
						r.experienceLevel(), r.availability(), r.openToMentoring(), r.learningGoals(),
						r.collaborationPreferences()));
	}

	@Operation(summary = "Mettre à jour la visibilité du profil")
	@PutMapping("/visibility")
	ProfileDetail updateVisibility(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody VisibilityRequest r) {
		return profiles.updateVisibility(CurrentUser.id(jwt),
				new ProfileInputs.VisibilityUpdate(r.visibility(), r.locationVisible()));
	}

	@Operation(summary = "Remplacer la liste des compétences")
	@PutMapping("/skills")
	ProfileDetail updateSkills(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody SkillsRequest r) {
		List<SkillInput> skills = r.skills() == null ? List.of()
				: r.skills().stream().map(s -> new SkillInput(s.technologyId(), s.level())).toList();
		return profiles.replaceSkills(CurrentUser.id(jwt), new ProfileInputs.SkillsUpdate(skills));
	}

	@Operation(summary = "Remplacer la liste des domaines")
	@PutMapping("/domains")
	ProfileDetail updateDomains(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody DomainsRequest r) {
		return profiles.replaceDomains(CurrentUser.id(jwt), new ProfileInputs.DomainsUpdate(r.domainIds()));
	}

	@Operation(summary = "Remplacer la liste des liens externes")
	@PutMapping("/links")
	ProfileDetail updateLinks(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody LinksRequest r) {
		List<LinkInput> links = r.links() == null ? List.of()
				: r.links().stream().map(l -> new LinkInput(l.type(), l.url(), l.displayOrder())).toList();
		return profiles.replaceLinks(CurrentUser.id(jwt), new ProfileInputs.LinksUpdate(links));
	}

}
