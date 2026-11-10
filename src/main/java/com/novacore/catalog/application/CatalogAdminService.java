package com.novacore.catalog.application;

import java.util.UUID;

import com.novacore.catalog.DomainView;
import com.novacore.catalog.TechnologyView;
import com.novacore.catalog.domain.Domain;
import com.novacore.catalog.domain.DomainRepository;
import com.novacore.catalog.domain.Technology;
import com.novacore.catalog.domain.TechnologyKind;
import com.novacore.catalog.domain.TechnologyRepository;
import com.novacore.shared.audit.AuditLogger;
import com.novacore.shared.error.ApiException;
import com.novacore.shared.web.ClientInfo;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Administration du référentiel. Réservé aux administrateurs ; chaque modification est auditée.
 * Le slug est dérivé du nom à la création et n'est jamais modifié ensuite (références stables).
 */
@Service
@Transactional
@PreAuthorize("hasRole('ADMIN')")
public class CatalogAdminService {

	private final TechnologyRepository technologies;

	private final DomainRepository domains;

	private final AuditLogger audit;

	CatalogAdminService(TechnologyRepository technologies, DomainRepository domains, AuditLogger audit) {
		this.technologies = technologies;
		this.domains = domains;
		this.audit = audit;
	}

	public TechnologyView createTechnology(UUID actorId, String name, String kind, ClientInfo client) {
		String slug = Slugs.from(name);
		if (slug.isBlank()) {
			throw ApiException.badRequest("INVALID_NAME", "Le nom ne permet pas de générer un identifiant.");
		}
		if (technologies.existsBySlug(slug)) {
			throw ApiException.conflict("TECHNOLOGY_EXISTS", "Une technologie « " + slug + " » existe déjà.");
		}
		TechnologyKind parsedKind = CatalogService.parseKind(kind);
		Technology technology = technologies.save(Technology.create(slug, name.trim(), parsedKind));
		audit.record(actorId, "TECHNOLOGY_CREATED", "TECHNOLOGY", technology.getId(), client, slug);
		return CatalogService.toView(technology);
	}

	public TechnologyView updateTechnology(UUID actorId, UUID id, String name, String kind, ClientInfo client) {
		Technology technology = technologies.findById(id)
			.orElseThrow(() -> ApiException.notFound("TECHNOLOGY_NOT_FOUND", "Technologie introuvable."));
		technology.rename(name.trim());
		technology.changeKind(CatalogService.parseKind(kind));
		audit.record(actorId, "TECHNOLOGY_UPDATED", "TECHNOLOGY", id, client);
		return CatalogService.toView(technology);
	}

	public void setTechnologyActive(UUID actorId, UUID id, boolean active, ClientInfo client) {
		Technology technology = technologies.findById(id)
			.orElseThrow(() -> ApiException.notFound("TECHNOLOGY_NOT_FOUND", "Technologie introuvable."));
		if (active) {
			technology.activate();
		}
		else {
			technology.deactivate();
		}
		audit.record(actorId, active ? "TECHNOLOGY_ACTIVATED" : "TECHNOLOGY_DEACTIVATED", "TECHNOLOGY", id, client);
	}

	public DomainView createDomain(UUID actorId, String name, String description, int displayOrder,
			ClientInfo client) {
		String slug = Slugs.from(name);
		if (slug.isBlank()) {
			throw ApiException.badRequest("INVALID_NAME", "Le nom ne permet pas de générer un identifiant.");
		}
		if (domains.existsBySlug(slug)) {
			throw ApiException.conflict("DOMAIN_EXISTS", "Un domaine « " + slug + " » existe déjà.");
		}
		Domain domain = domains.save(Domain.create(slug, name.trim(), description, displayOrder));
		audit.record(actorId, "DOMAIN_CREATED", "DOMAIN", domain.getId(), client, slug);
		return CatalogService.toView(domain);
	}

	public DomainView updateDomain(UUID actorId, UUID id, String name, String description, int displayOrder,
			ClientInfo client) {
		Domain domain = domains.findById(id)
			.orElseThrow(() -> ApiException.notFound("DOMAIN_NOT_FOUND", "Domaine introuvable."));
		domain.update(name.trim(), description, displayOrder);
		audit.record(actorId, "DOMAIN_UPDATED", "DOMAIN", id, client);
		return CatalogService.toView(domain);
	}

	public void setDomainActive(UUID actorId, UUID id, boolean active, ClientInfo client) {
		Domain domain = domains.findById(id)
			.orElseThrow(() -> ApiException.notFound("DOMAIN_NOT_FOUND", "Domaine introuvable."));
		if (active) {
			domain.activate();
		}
		else {
			domain.deactivate();
		}
		audit.record(actorId, active ? "DOMAIN_ACTIVATED" : "DOMAIN_DEACTIVATED", "DOMAIN", id, client);
	}

}
