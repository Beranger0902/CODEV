package com.novacore.project.domain;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

	/**
	 * Projets publics pour la découverte, avec filtres optionnels (statut, domaine, technologie, texte).
	 * Un paramètre nul désactive le filtre correspondant.
	 */
	@Query("""
			select distinct p from Project p
			where p.visibility = com.novacore.project.domain.ProjectVisibility.PUBLIC
			  and (:status is null or p.status = :status)
			  and (:domainId is null or :domainId member of p.domainIds)
			  and (:technologyId is null or :technologyId member of p.technologyIds)
			  and (:term is null or lower(p.name) like lower(concat('%', :term, '%'))
			       or lower(p.description) like lower(concat('%', :term, '%')))""")
	Page<Project> discover(ProjectStatus status, UUID domainId, UUID technologyId, String term, Pageable pageable);

	@Query("select p from Project p join p.members m where m.userId = :userId")
	Page<Project> findByMember(UUID userId, Pageable pageable);

}
