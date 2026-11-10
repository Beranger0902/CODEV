package com.novacore.project.domain;

public enum ProjectStatus {

	/** En préparation, non publié. */
	DRAFT,

	/** Publié et ouvert aux candidatures. */
	RECRUITING,

	/** En cours de réalisation. */
	IN_PROGRESS,

	/** Terminé. */
	COMPLETED,

	/** Archivé (lecture seule). */
	ARCHIVED

}
