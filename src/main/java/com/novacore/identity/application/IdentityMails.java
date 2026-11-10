package com.novacore.identity.application;

import com.novacore.identity.domain.UserAccount;
import com.novacore.notification.MailService;
import com.novacore.notification.OutgoingMail;
import com.novacore.shared.NovacoreProperties;
import org.springframework.stereotype.Component;

/**
 * Contenu des e-mails liés au compte. Les liens pointent vers le frontend,
 * qui appelle ensuite l'API avec le jeton.
 */
@Component
class IdentityMails {

	private final MailService mailService;

	private final String frontendUrl;

	private final IdentityProperties identityProperties;

	IdentityMails(MailService mailService, NovacoreProperties properties, IdentityProperties identityProperties) {
		this.mailService = mailService;
		this.frontendUrl = properties.frontendUrl();
		this.identityProperties = identityProperties;
	}

	void emailVerification(UserAccount user, String token) {
		send(user, "Confirmez votre adresse e-mail", """
				Bonjour %s,

				Bienvenue sur NovaCore ! Confirmez votre adresse e-mail en ouvrant ce lien :
				%s/verify-email?token=%s

				Ce lien expire dans %d heures. Si vous n'êtes pas à l'origine de cette inscription, ignorez cet e-mail.
				""".formatted(user.getDisplayName(), frontendUrl, token,
				identityProperties.verificationTokenTtl().toHours()));
	}

	void accountAlreadyExists(UserAccount user) {
		send(user, "Tentative d'inscription avec votre adresse", """
				Bonjour %s,

				Quelqu'un a tenté de créer un compte NovaCore avec cette adresse e-mail, qui est déjà inscrite.
				Si c'était vous, connectez-vous ou réinitialisez votre mot de passe :
				%s/forgot-password

				Sinon, vous pouvez ignorer cet e-mail.
				""".formatted(user.getDisplayName(), frontendUrl));
	}

	void passwordReset(UserAccount user, String token) {
		send(user, "Réinitialisation de votre mot de passe", """
				Bonjour %s,

				Pour choisir un nouveau mot de passe, ouvrez ce lien :
				%s/reset-password?token=%s

				Ce lien expire dans %d minutes et ne peut être utilisé qu'une fois.
				Si vous n'avez rien demandé, ignorez cet e-mail : votre mot de passe reste inchangé.
				""".formatted(user.getDisplayName(), frontendUrl, token,
				identityProperties.passwordResetTokenTtl().toMinutes()));
	}

	void passwordChanged(UserAccount user) {
		send(user, "Votre mot de passe a été modifié", """
				Bonjour %s,

				Le mot de passe de votre compte NovaCore vient d'être modifié. Les autres sessions ont été déconnectées.
				Si vous n'êtes pas à l'origine de ce changement, réinitialisez immédiatement votre mot de passe :
				%s/forgot-password
				""".formatted(user.getDisplayName(), frontendUrl));
	}

	private void send(UserAccount user, String subject, String body) {
		mailService.send(new OutgoingMail(user.getEmail(), "NovaCore - " + subject, body));
	}

}
