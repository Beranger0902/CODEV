package com.novacore.identity.domain;

public enum AccountStatus {

	/** Inscrit, adresse e-mail pas encore confirmée : connexion impossible. */
	PENDING_VERIFICATION,

	ACTIVE,

	/** Suspendu par la modération ou l'administration. */
	SUSPENDED,

	/** Désactivé à la demande de l'utilisateur. */
	DEACTIVATED

}
