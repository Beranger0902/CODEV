package com.novacore.catalog.api;

import java.util.List;

import com.novacore.catalog.DomainView;
import com.novacore.catalog.TechnologyView;
import com.novacore.catalog.application.CatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lecture du référentiel. Public : alimente les formulaires et les filtres de recherche,
 * y compris avant connexion (inscription, exploration).
 */
@RestController
@RequestMapping("/api/v1/catalog")
@Tag(name = "Référentiel")
@SecurityRequirements
class CatalogController {

	private final CatalogService catalog;

	CatalogController(CatalogService catalog) {
		this.catalog = catalog;
	}

	@Operation(summary = "Lister les technologies (filtres : kind, search)")
	@GetMapping("/technologies")
	Page<TechnologyView> technologies(@RequestParam(required = false) String kind,
			@RequestParam(required = false) String search,
			@PageableDefault(size = 50, sort = "name") Pageable pageable) {
		return catalog.listTechnologies(kind, search, pageable);
	}

	@Operation(summary = "Lister les domaines d'activité")
	@GetMapping("/domains")
	List<DomainView> domains() {
		return catalog.listDomains();
	}

}
