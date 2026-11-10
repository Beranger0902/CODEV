package com.novacore.catalog.domain;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TechnologyRepository extends JpaRepository<Technology, UUID> {

	boolean existsBySlug(String slug);

	Optional<Technology> findBySlug(String slug);

	List<Technology> findByIdInAndActiveTrue(Collection<UUID> ids);

	Page<Technology> findByActiveTrue(Pageable pageable);

	Page<Technology> findByActiveTrueAndKind(TechnologyKind kind, Pageable pageable);

	@Query("""
			select t from Technology t
			where t.active = true and lower(t.name) like lower(concat('%', :term, '%'))""")
	Page<Technology> search(String term, Pageable pageable);

}
