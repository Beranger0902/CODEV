package com.novacore.project.domain;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MilestoneRepository extends JpaRepository<Milestone, UUID> {

	List<Milestone> findByProjectIdOrderByDisplayOrderAscCreatedAtAsc(UUID projectId);

}
