package com.novacore.project.domain;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, UUID> {

	List<Task> findByProjectIdOrderByBoardOrderAscCreatedAtAsc(UUID projectId);

	List<Task> findByProjectIdAndStatus(UUID projectId, TaskStatus status);

	List<Task> findByMilestoneId(UUID milestoneId);

	long countByProjectIdAndStatus(UUID projectId, TaskStatus status);

	boolean existsByProjectIdAndAssigneeId(UUID projectId, UUID assigneeId);

}
