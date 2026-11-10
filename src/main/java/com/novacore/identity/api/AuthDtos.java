package com.novacore.identity.api;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Contrats d'entrée/sortie du module identité. Les entités ne sont jamais exposées directement.
 */
final class AuthDtos {

	private AuthDtos() {
	}

	record RegisterRequest(
			@NotBlank(message = "L'adresse e-mail est obligatoire.")
			@Email(message = "L'adresse e-mail est invalide.")
			@Size(max = 254, message = "L'adresse e-mail est trop longue.")
			String email,

			@NotBlank(message = "Le nom d'utilisateur est obligatoire.")
			@Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9_-]{1,28}[A-Za-z0-9]$",
					message = "3 à 30 caractères : lettres, chiffres, « _ » ou « - », sans les placer au début ni à la fin.")
			String username,

			@NotBlank(message = "Le nom d'affichage est obligatoire.")
			@Size(max = 80, message = "Le nom d'affichage est trop long (80 caractères maximum).")
			String displayName,

			@NotBlank(message = "Le mot de passe est obligatoire.")
			@Size(min = 12, max = 72, message = "Le mot de passe doit contenir entre 12 et 72 caractères.")
			String password,

			@AssertTrue(message = "Vous devez accepter les conditions d'utilisation et la politique de confidentialité.")
			boolean acceptTerms) {
	}

	record LoginRequest(
			@NotBlank(message = "L'adresse e-mail est obligatoire.") String email,
			@NotBlank(message = "Le mot de passe est obligatoire.") @Size(max = 200) String password) {
	}

	record RefreshTokenRequest(@NotBlank(message = "Le refresh token est obligatoire.") String refreshToken) {
	}

	record TokenRequest(@NotBlank(message = "Le jeton est obligatoire.") String token) {
	}

	record EmailRequest(
			@NotBlank(message = "L'adresse e-mail est obligatoire.")
			@Email(message = "L'adresse e-mail est invalide.")
			String email) {
	}

	record ResetPasswordRequest(
			@NotBlank(message = "Le jeton est obligatoire.") String token,
			@NotBlank(message = "Le nouveau mot de passe est obligatoire.")
			@Size(min = 12, max = 72, message = "Le mot de passe doit contenir entre 12 et 72 caractères.")
			String newPassword) {
	}

	record ChangePasswordRequest(
			@NotBlank(message = "Le mot de passe actuel est obligatoire.") String currentPassword,
			@NotBlank(message = "Le nouveau mot de passe est obligatoire.")
			@Size(min = 12, max = 72, message = "Le mot de passe doit contenir entre 12 et 72 caractères.")
			String newPassword) {
	}

	record TokenResponse(String accessToken, String tokenType, long expiresIn, String refreshToken,
			Instant refreshTokenExpiresAt) {
	}

	record MessageResponse(String message) {
	}

	record MeResponse(UUID id, String email, String username, String displayName, String status,
			Set<String> roles, Instant emailVerifiedAt, Instant createdAt) {
	}

	record SessionResponse(UUID id, String userAgent, String ipAddress, Instant createdAt, Instant lastUsedAt,
			Instant expiresAt, boolean current) {
	}

}
