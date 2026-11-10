package com.novacore.identity.api;

import java.time.Clock;
import java.time.Duration;

import com.novacore.identity.api.AuthDtos.EmailRequest;
import com.novacore.identity.api.AuthDtos.LoginRequest;
import com.novacore.identity.api.AuthDtos.MessageResponse;
import com.novacore.identity.api.AuthDtos.RefreshTokenRequest;
import com.novacore.identity.api.AuthDtos.RegisterRequest;
import com.novacore.identity.api.AuthDtos.ResetPasswordRequest;
import com.novacore.identity.api.AuthDtos.TokenRequest;
import com.novacore.identity.api.AuthDtos.TokenResponse;
import com.novacore.identity.application.AuthenticationService;
import com.novacore.identity.application.IssuedTokens;
import com.novacore.identity.application.PasswordService;
import com.novacore.identity.application.RegistrationService;
import com.novacore.shared.web.ClientInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentification")
@SecurityRequirements // routes publiques
class AuthController {

	private static final String CHECK_INBOX = "Si cette adresse peut recevoir un e-mail, un message vient d'être envoyé.";

	private final RegistrationService registration;

	private final AuthenticationService authentication;

	private final PasswordService passwords;

	private final Clock clock;

	AuthController(RegistrationService registration, AuthenticationService authentication, PasswordService passwords,
			Clock clock) {
		this.registration = registration;
		this.authentication = authentication;
		this.passwords = passwords;
		this.clock = clock;
	}

	@Operation(summary = "Créer un compte (vérification par e-mail requise)")
	@PostMapping("/register")
	@ResponseStatus(HttpStatus.ACCEPTED)
	MessageResponse register(@Valid @RequestBody RegisterRequest request, HttpServletRequest http) {
		registration.register(request.email(), request.username(), request.displayName(), request.password(),
				ClientInfo.from(http));
		return new MessageResponse("Inscription enregistrée. Consultez votre boîte e-mail pour confirmer votre adresse.");
	}

	@Operation(summary = "Confirmer l'adresse e-mail")
	@PostMapping("/verify-email")
	MessageResponse verifyEmail(@Valid @RequestBody TokenRequest request, HttpServletRequest http) {
		registration.verifyEmail(request.token(), ClientInfo.from(http));
		return new MessageResponse("Adresse e-mail confirmée. Vous pouvez vous connecter.");
	}

	@Operation(summary = "Renvoyer l'e-mail de vérification")
	@PostMapping("/verify-email/resend")
	@ResponseStatus(HttpStatus.ACCEPTED)
	MessageResponse resendVerification(@Valid @RequestBody EmailRequest request) {
		registration.resendVerification(request.email());
		return new MessageResponse(CHECK_INBOX);
	}

	@Operation(summary = "Se connecter")
	@PostMapping("/login")
	TokenResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
		return toResponse(authentication.login(request.email(), request.password(), ClientInfo.from(http)));
	}

	@Operation(summary = "Obtenir de nouveaux jetons (rotation du refresh token)")
	@PostMapping("/refresh")
	TokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request, HttpServletRequest http) {
		return toResponse(authentication.refresh(request.refreshToken(), ClientInfo.from(http)));
	}

	@Operation(summary = "Se déconnecter de cet appareil")
	@PostMapping("/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void logout(@Valid @RequestBody RefreshTokenRequest request, HttpServletRequest http) {
		authentication.logout(request.refreshToken(), ClientInfo.from(http));
	}

	@Operation(summary = "Demander un lien de réinitialisation du mot de passe")
	@PostMapping("/password/forgot")
	@ResponseStatus(HttpStatus.ACCEPTED)
	MessageResponse forgotPassword(@Valid @RequestBody EmailRequest request, HttpServletRequest http) {
		passwords.requestReset(request.email(), ClientInfo.from(http));
		return new MessageResponse(CHECK_INBOX);
	}

	@Operation(summary = "Choisir un nouveau mot de passe à partir du lien reçu")
	@PostMapping("/password/reset")
	MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request, HttpServletRequest http) {
		passwords.reset(request.token(), request.newPassword(), ClientInfo.from(http));
		return new MessageResponse("Mot de passe modifié. Vous pouvez vous connecter.");
	}

	private TokenResponse toResponse(IssuedTokens tokens) {
		long expiresIn = Duration.between(clock.instant(), tokens.accessTokenExpiresAt()).toSeconds();
		return new TokenResponse(tokens.accessToken(), "Bearer", expiresIn, tokens.refreshToken(),
				tokens.refreshTokenExpiresAt());
	}

}
