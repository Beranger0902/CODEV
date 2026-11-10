package com.novacore.identity.domain;

/**
 * Rôles globaux de la plateforme.
 * <p>
 * Les rôles du catalogue « responsable de communauté », « responsable de projet » et
 * « organisation » sont contextuels : on est responsable d'UNE communauté ou d'UN projet.
 * Ils seront portés par les modules communauté, projet et organisation, pas par ce rôle global.
 */
public enum Role {

	MEMBER,

	MODERATOR,

	ADMIN

}
