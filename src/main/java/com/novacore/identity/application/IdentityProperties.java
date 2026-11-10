package com.novacore.identity.application;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param termsVersion version des CGU en vigueur, enregistrée avec chaque acceptation
 * @param verificationTokenTtl validité du lien de vérification d'e-mail
 * @param passwordResetTokenTtl validité du lien de réinitialisation du mot de passe
 * @param maxFailedLogins échecs consécutifs avant verrouillage temporaire
 * @param lockDuration durée du verrouillage
 * @param resendCooldown délai minimal entre deux e-mails du même type (anti-spam)
 */
@ConfigurationProperties("novacore.identity")
public record IdentityProperties(String termsVersion, Duration verificationTokenTtl, Duration passwordResetTokenTtl,
		int maxFailedLogins, Duration lockDuration, Duration resendCooldown) {
}
