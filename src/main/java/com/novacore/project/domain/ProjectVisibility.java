package com.novacore.project.domain;

public enum ProjectVisibility {

	/** Fiche visible par tous, y compris sans connexion. */
	PUBLIC,

	/** Fiche visible par les membres connectés. */
	MEMBERS_ONLY,

	/** Fiche visible uniquement par l'équipe du projet. */
	PRIVATE

}
