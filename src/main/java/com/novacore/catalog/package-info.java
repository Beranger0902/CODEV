/**
 * Référentiel partagé : domaines d'activité et technologies (catalogue §2, §4, §6, §8).
 * <p>
 * API publique (racine du module) : {@link com.novacore.catalog.Catalog},
 * {@link com.novacore.catalog.TechnologyView}, {@link com.novacore.catalog.DomainView}.
 * Les autres modules valident et résolvent leurs références via {@code Catalog}, sans
 * accéder aux entités internes.
 */
@ApplicationModule(displayName = "Référentiel (technologies et domaines)")
package com.novacore.catalog;

import org.springframework.modulith.ApplicationModule;
