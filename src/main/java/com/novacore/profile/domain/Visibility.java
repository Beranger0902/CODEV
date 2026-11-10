package com.novacore.profile.domain;

public enum Visibility {

	/** Visible par tout le monde, y compris sans connexion. */
	PUBLIC,

	/** Visible uniquement par les membres connectés. */
	MEMBERS_ONLY,

	/** Visible uniquement par son propriétaire. */
	PRIVATE

}
