package com.novacore.project.domain;

/** Rôle contextuel au sein d'un projet (distinct des rôles globaux du module identity). */
public enum MemberRole {

	/** Responsable du projet : contrôle total, un seul à la fois. */
	OWNER,

	/** Peut gérer les candidatures, les membres et le contenu du projet. */
	MAINTAINER,

	/** Membre de l'équipe sans droits d'administration. */
	CONTRIBUTOR;

	public boolean canManage() {
		return this == OWNER || this == MAINTAINER;
	}

}
