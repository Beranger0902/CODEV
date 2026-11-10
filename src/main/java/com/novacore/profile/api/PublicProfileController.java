package com.novacore.profile.api;

import com.novacore.profile.application.ProfileService;
import com.novacore.profile.application.ProfileViews.ProfileDetail;
import com.novacore.shared.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.lang.Nullable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Profil public par nom d'utilisateur. Accessible sans connexion ; la visibilité du profil
 * décide de ce qui est renvoyé (le jeton, optionnel, identifie le spectateur).
 */
@RestController
@RequestMapping("/api/v1/profiles")
@Tag(name = "Profils publics")
@SecurityRequirements
class PublicProfileController {

	private final ProfileService profiles;

	PublicProfileController(ProfileService profiles) {
		this.profiles = profiles;
	}

	@Operation(summary = "Consulter le profil public d'un membre")
	@GetMapping("/{username}")
	ProfileDetail profile(@PathVariable String username, @AuthenticationPrincipal @Nullable Jwt jwt) {
		return profiles.getPublic(username, jwt != null ? CurrentUser.id(jwt) : null);
	}

}
