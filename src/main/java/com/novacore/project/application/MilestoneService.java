package com.novacore.project.application;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.novacore.project.application.ProjectBoardInputs.MilestoneInput;
import com.novacore.project.application.ProjectBoardViews.MilestoneView;
import com.novacore.project.domain.Milestone;
import com.novacore.project.domain.MilestoneRepository;
import com.novacore.project.domain.MilestoneStatus;
import com.novacore.project.domain.Task;
import com.novacore.project.domain.TaskRepository;
import com.novacore.project.domain.TaskStatus;
import com.novacore.shared.error.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** Jalons d'un projet. Les membres consultent ; les responsables gèrent. */
@Service
@Transactional
public class MilestoneService {

	private final MilestoneRepository milestones;

	private final TaskRepository tasks;

	private final ProjectService projects;

	private final ProjectBoardAssembler assembler;

	MilestoneService(MilestoneRepository milestones, TaskRepository tasks, ProjectService projects,
			ProjectBoardAssembler assembler) {
		this.milestones = milestones;
		this.tasks = tasks;
		this.projects = projects;
		this.assembler = assembler;
	}

	@Transactional(readOnly = true)
	public List<MilestoneView> list(UUID projectId, UUID actorId) {
		projects.loadForMember(projectId, actorId);
		List<Task> projectTasks = tasks.findByProjectIdOrderByBoardOrderAscCreatedAtAsc(projectId);
		return milestones.findByProjectIdOrderByDisplayOrderAscCreatedAtAsc(projectId).stream().map(milestone -> {
			long total = projectTasks.stream().filter(t -> milestone.getId().equals(t.getMilestoneId())).count();
			long done = projectTasks.stream()
				.filter(t -> milestone.getId().equals(t.getMilestoneId()) && t.getStatus() == TaskStatus.DONE)
				.count();
			return assembler.toMilestoneView(milestone, total, done);
		}).toList();
	}

	public MilestoneView create(UUID projectId, UUID actorId, MilestoneInput input) {
		projects.loadManageable(projectId, actorId);
		requireTitle(input.title());
		int order = milestones.findByProjectIdOrderByDisplayOrderAscCreatedAtAsc(projectId).size();
		Milestone milestone = milestones
			.save(Milestone.create(projectId, input.title().trim(), trimToNull(input.description()), input.dueDate(),
					order));
		return assembler.toMilestoneView(milestone, 0, 0);
	}

	public MilestoneView update(UUID milestoneId, UUID actorId, MilestoneInput input) {
		Milestone milestone = load(milestoneId);
		projects.loadManageable(milestone.getProjectId(), actorId);
		requireTitle(input.title());
		milestone.update(input.title().trim(), trimToNull(input.description()), input.dueDate());
		return view(milestone);
	}

	public MilestoneView changeStatus(UUID milestoneId, UUID actorId, String status) {
		Milestone milestone = load(milestoneId);
		projects.loadManageable(milestone.getProjectId(), actorId);
		milestone.changeStatus(parseStatus(status));
		return view(milestone);
	}

	public void delete(UUID milestoneId, UUID actorId) {
		Milestone milestone = load(milestoneId);
		projects.loadManageable(milestone.getProjectId(), actorId);
		milestones.delete(milestone); // les tâches liées voient leur milestone_id remis à null (FK)
	}

	private MilestoneView view(Milestone milestone) {
		long total = tasks.findByMilestoneId(milestone.getId()).size();
		long done = tasks.findByMilestoneId(milestone.getId())
			.stream()
			.filter(t -> t.getStatus() == TaskStatus.DONE)
			.count();
		return assembler.toMilestoneView(milestone, total, done);
	}

	private Milestone load(UUID milestoneId) {
		return milestones.findById(milestoneId)
			.orElseThrow(() -> ApiException.notFound("MILESTONE_NOT_FOUND", "Jalon introuvable."));
	}

	private static MilestoneStatus parseStatus(String status) {
		try {
			return MilestoneStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException | NullPointerException ex) {
			throw ApiException.badRequest("INVALID_STATUS", "Statut de jalon invalide : " + status);
		}
	}

	private static void requireTitle(String title) {
		if (!StringUtils.hasText(title)) {
			throw ApiException.badRequest("INVALID_TITLE", "Le titre du jalon est obligatoire.");
		}
	}

	private static String trimToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}

}
