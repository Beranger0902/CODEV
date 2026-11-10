package com.novacore.catalog.domain;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DomainRepository extends JpaRepository<Domain, UUID> {

	boolean existsBySlug(String slug);

	Optional<Domain> findBySlug(String slug);

	List<Domain> findByActiveTrueOrderByDisplayOrderAscNameAsc();

	List<Domain> findByIdInAndActiveTrue(Collection<UUID> ids);

}
