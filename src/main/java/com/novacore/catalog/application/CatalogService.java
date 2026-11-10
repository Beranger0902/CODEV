package com.novacore.catalog.application;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.novacore.catalog.Catalog;
import com.novacore.catalog.DomainView;
import com.novacore.catalog.TechnologyView;
import com.novacore.catalog.domain.Domain;
import com.novacore.catalog.domain.DomainRepository;
import com.novacore.catalog.domain.Technology;
import com.novacore.catalog.domain.TechnologyKind;
import com.novacore.catalog.domain.TechnologyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Lecture du référentiel : requêtes publiques (API) et implémentation de {@link Catalog}
 * pour les autres modules.
 */
@Service
@Transactional(readOnly = true)
public class CatalogService implements Catalog {

	private final TechnologyRepository technologies;

	private final DomainRepository domains;

	CatalogService(TechnologyRepository technologies, DomainRepository domains) {
		this.technologies = technologies;
		this.domains = domains;
	}

	public Page<TechnologyView> listTechnologies(String kind, String search, Pageable pageable) {
		Page<Technology> page;
		if (StringUtils.hasText(search)) {
			page = technologies.search(search.trim(), pageable);
		}
		else if (StringUtils.hasText(kind)) {
			page = technologies.findByActiveTrueAndKind(parseKind(kind), pageable);
		}
		else {
			page = technologies.findByActiveTrue(pageable);
		}
		return page.map(CatalogService::toView);
	}

	public List<DomainView> listDomains() {
		return domains.findByActiveTrueOrderByDisplayOrderAscNameAsc()
			.stream()
			.map(CatalogService::toView)
			.toList();
	}

	@Override
	public Set<UUID> existingTechnologyIds(Collection<UUID> candidateIds) {
		if (candidateIds == null || candidateIds.isEmpty()) {
			return Set.of();
		}
		return technologies.findByIdInAndActiveTrue(candidateIds)
			.stream()
			.map(Technology::getId)
			.collect(Collectors.toSet());
	}

	@Override
	public Set<UUID> existingDomainIds(Collection<UUID> candidateIds) {
		if (candidateIds == null || candidateIds.isEmpty()) {
			return Set.of();
		}
		return domains.findByIdInAndActiveTrue(candidateIds).stream().map(Domain::getId).collect(Collectors.toSet());
	}

	@Override
	public List<TechnologyView> technologies(Collection<UUID> ids) {
		if (ids == null || ids.isEmpty()) {
			return List.of();
		}
		return technologies.findByIdInAndActiveTrue(ids).stream().map(CatalogService::toView).toList();
	}

	@Override
	public List<DomainView> domains(Collection<UUID> ids) {
		if (ids == null || ids.isEmpty()) {
			return List.of();
		}
		return domains.findByIdInAndActiveTrue(ids).stream().map(CatalogService::toView).toList();
	}

	static TechnologyKind parseKind(String kind) {
		try {
			return TechnologyKind.valueOf(kind.trim().toUpperCase(java.util.Locale.ROOT));
		}
		catch (IllegalArgumentException ex) {
			throw com.novacore.shared.error.ApiException.badRequest("INVALID_TECHNOLOGY_KIND",
					"Catégorie de technologie inconnue : " + kind);
		}
	}

	static TechnologyView toView(Technology technology) {
		return new TechnologyView(technology.getId(), technology.getSlug(), technology.getName(),
				technology.getKind().name());
	}

	static DomainView toView(Domain domain) {
		return new DomainView(domain.getId(), domain.getSlug(), domain.getName(), domain.getDescription());
	}

}
