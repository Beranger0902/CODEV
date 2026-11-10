package com.novacore.catalog;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Point d'entrée du référentiel pour les autres modules (profils, questions, projets…).
 * Permet de valider et de résoudre des références de technologies et de domaines
 * sans dépendre des entités internes du catalogue.
 */
public interface Catalog {

	/** Sous-ensemble des identifiants fournis qui correspondent à des technologies actives. */
	Set<UUID> existingTechnologyIds(Collection<UUID> candidateIds);

	/** Sous-ensemble des identifiants fournis qui correspondent à des domaines actifs. */
	Set<UUID> existingDomainIds(Collection<UUID> candidateIds);

	List<TechnologyView> technologies(Collection<UUID> ids);

	List<DomainView> domains(Collection<UUID> ids);

}
