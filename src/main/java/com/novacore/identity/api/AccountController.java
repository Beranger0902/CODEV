package com.novacore.identity.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.novacore.identity.api.AuthDtos.ChangePasswordRequest;
import com.novacore.identity.api.AuthDtos.MeResponse;
import com.novacore.identity.api.AuthDtos.SessionResponse;
import com.novacore.identity.application.AccountService;
import com.novacore.identity.application.PasswordService;
import com.novacore.identity.domain.UserAccount;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
@Tag(name = "Mon compte")
class AccountController {

	private final AccountService accounts;

	private final PasswordService passwords;

	AccountController(AccountService accounts, PasswordService passwords) {
		this.accounts = accounts;
		this.passwords = passwords;
	}

	@Operation(summary = "Informations du compte connecté")
	@GetMapping
	MeResponse me(@AuthenticationPrincipal Jwt jwt) {
		UserAccount user = accounts.get(CurrentUser.id(jwt));
		return new MeResponse(user.getId(), user.getEmail(), user.getUsername(), user.getDisplayName(),
				user.getStatus().name(), user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()),
				user.getEmailVerifiedAt(), user.getCreatedAt());
	}

	@Operation(summary = "Changer son mot de passe (déconnecte les autres appareils)")
	@PutMapping("/password")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void changePassword(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ChangePasswordRequest request,
			HttpServletRequest http) {
		passwords.change(CurrentUser.id(jwt), CurrentUser.sessionId(jwt), request.currentPassword(),
				request.newPassword(), ClientInfo.from(http));
	}

	@Operation(summary = "Appareils connectés")
	@GetMapping("/sessions")
	List<SessionResponse> sessions(@AuthenticationPrincipal Jwt jwt) {
		UUID current = CurrentUser.sessionId(jwt);
		return accounts.activeSessions(CurrentUser.id(jwt))
			.stream()
			.map(s -> new SessionResponse(s.getId(), s.getUserAgent(), s.getIpAddress(), s.getCreatedAt(),
					s.getLastUsedAt(), s.getExpiresAt(), s.getId().equals(current)))
			.toList();
	}

	@Operation(summary = "Déconnecter un appareil")
	@DeleteMapping("/sessions/{sessionId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void revokeSession(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID sessionId, HttpServletRequest http) {
		accounts.revokeSession(CurrentUser.id(jwt), sessionId, ClientInfo.from(http));
	}

}
