package com.novacore.project.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectApplicationRepository extends JpaRepository<ProjectApplication, UUID> {

	List<ProjectApplication> findByProjectIdOrderByCreatedAtDesc(UUID projectId);

	List<ProjectApplication> findByApplicantIdOrderByCreatedAtDesc(UUID applicantId);

	Optional<ProjectApplication> findByProjectIdAndApplicantIdAndStatus(UUID projectId, UUID applicantId,
			ApplicationStatus status);

}
