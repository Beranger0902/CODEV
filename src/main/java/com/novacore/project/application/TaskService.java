package com.novacore.project.application;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.novacore.project.application.ProjectBoardInputs.DependenciesUpdate;
import com.novacore.project.application.ProjectBoardInputs.TaskCreate;
import com.novacore.project.application.ProjectBoardInputs.TaskMove;
import com.novacore.project.application.ProjectBoardInputs.TaskUpdate;
import com.novacore.project.application.ProjectBoardViews.BoardColumn;
import com.novacore.project.application.ProjectBoardViews.BoardView;
import com.novacore.project.application.ProjectBoardViews.TaskView;
import com.novacore.project.domain.Milestone;
import com.novacore.project.domain.MilestoneRepository;
import com.novacore.project.domain.Project;
import com.novacore.project.domain.Task;
import com.novacore.project.domain.TaskPriority;
import com.novacore.project.domain.TaskRepository;
import com.novacore.project.domain.TaskStatus;
import com.novacore.shared.error.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** Tâches d'un projet : gestion par les membres, tableau Kanban et dépendances. */
@Service
@Transactional
public class TaskService {

	private final TaskRepository tasks;

	private final MilestoneRepository milestones;

	private final ProjectService projects;

	private final ProjectBoardAssembler assembler;

	TaskService(TaskRepository tasks, MilestoneRepository milestones, ProjectService projects,
			ProjectBoardAssembler assembler) {
		this.tasks = tasks;
		this.milestones = milestones;
		this.projects = projects;
		this.assembler = assembler;
	}

	@Transactional(readOnly = true)
	public BoardView board(UUID projectId, UUID actorId) {
		projects.loadForMember(projectId, actorId);
		List<Task> all = tasks.findByProjectIdOrderByBoardOrderAscCreatedAtAsc(projectId);
		List<BoardColumn> columns = new ArrayList<>();
		for (TaskStatus status : TaskStatus.values()) {
			List<TaskView> views = all.stream()
				.filter(t -> t.getStatus() == status)
				.map(assembler::toTaskView)
				.toList();
			columns.add(new BoardColumn(status.name(), views));
		}
		return new BoardView(columns);
	}

	@Transactional(readOnly = true)
	public TaskView get(UUID taskId, UUID actorId) {
		Task task = load(taskId);
		projects.loadForMember(task.getProjectId(), actorId);
		return assembler.toTaskView(task);
	}

	public TaskView create(UUID projectId, UUID actorId, TaskCreate input) {
		Project project = projects.loadForMember(projectId, actorId);
		requireTitle(input.title());
		int order = (int) tasks.countByProjectIdAndStatus(projectId, TaskStatus.TODO);
		Task task = Task.create(projectId, input.title().trim(), actorId, order);
		task.updateDetails(input.title().trim(), trimToNull(input.description()),
				parsePriority(input.priority(), TaskPriority.MEDIUM), validateAssignee(project, input.assigneeId()),
				input.dueDate(), validateMilestone(projectId, input.milestoneId()));
		return assembler.toTaskView(tasks.save(task));
	}

	public TaskView update(UUID taskId, UUID actorId, TaskUpdate input) {
		Task task = load(taskId);
		Project project = projects.loadForMember(task.getProjectId(), actorId);
		requireTitle(input.title());
		task.updateDetails(input.title().trim(), trimToNull(input.description()),
				parsePriority(input.priority(), task.getPriority()), validateAssignee(project, input.assigneeId()),
				input.dueDate(), validateMilestone(task.getProjectId(), input.milestoneId()));
		return assembler.toTaskView(task);
	}

	/** Déplacement dans le Kanban : change de colonne et/ou de position. */
	public TaskView move(UUID taskId, UUID actorId, TaskMove input) {
		Task task = load(taskId);
		projects.loadForMember(task.getProjectId(), actorId);
		TaskStatus status = parseStatus(input.status());
		int order = input.boardOrder() != null ? input.boardOrder()
				: (int) tasks.countByProjectIdAndStatus(task.getProjectId(), status);
		task.moveTo(status, order);
		return assembler.toTaskView(task);
	}

	public TaskView setDependencies(UUID taskId, UUID actorId, DependenciesUpdate input) {
		Task task = load(taskId);
		projects.loadForMember(task.getProjectId(), actorId);
		List<UUID> deps = input.dependsOn() != null ? input.dependsOn().stream().distinct().toList() : List.of();

		List<Task> projectTasks = tasks.findByProjectIdOrderByBoardOrderAscCreatedAtAsc(task.getProjectId());
		Set<UUID> projectTaskIds = new HashSet<>();
		projectTasks.forEach(t -> projectTaskIds.add(t.getId()));
		for (UUID dep : deps) {
			if (dep.equals(taskId)) {
				throw ApiException.badRequest("SELF_DEPENDENCY", "Une tâche ne peut pas dépendre d'elle-même.");
			}
			if (!projectTaskIds.contains(dep)) {
				throw ApiException.badRequest("UNKNOWN_DEPENDENCY", "Dépendance inconnue dans ce projet : " + dep);
			}
		}
		if (createsCycle(taskId, deps, projectTasks)) {
			throw ApiException.badRequest("DEPENDENCY_CYCLE", "Ces dépendances créeraient un cycle.");
		}
		task.replaceDependencies(deps);
		return assembler.toTaskView(task);
	}

	public void delete(UUID taskId, UUID actorId) {
		Task task = load(taskId);
		Project project = projects.loadForMember(task.getProjectId(), actorId);
		if (!task.getCreatedBy().equals(actorId) && !project.canManage(actorId)) {
			throw ApiException.forbidden("CANNOT_DELETE_TASK",
					"Seul l'auteur de la tâche ou un responsable peut la supprimer.");
		}
		tasks.delete(task);
	}

	// --- validations ---

	private UUID validateAssignee(Project project, UUID assigneeId) {
		if (assigneeId != null && !project.isMember(assigneeId)) {
			throw ApiException.badRequest("ASSIGNEE_NOT_MEMBER",
					"La tâche ne peut être attribuée qu'à un membre du projet.");
		}
		return assigneeId;
	}

	private UUID validateMilestone(UUID projectId, UUID milestoneId) {
		if (milestoneId == null) {
			return null;
		}
		Milestone milestone = milestones.findById(milestoneId)
			.orElseThrow(() -> ApiException.badRequest("UNKNOWN_MILESTONE", "Jalon inconnu."));
		if (!milestone.getProjectId().equals(projectId)) {
			throw ApiException.badRequest("UNKNOWN_MILESTONE", "Ce jalon n'appartient pas au projet.");
		}
		return milestoneId;
	}

	/** Détecte un cycle si l'on affecte {@code newDeps} comme dépendances de {@code taskId}. */
	static boolean createsCycle(UUID taskId, List<UUID> newDeps, List<Task> projectTasks) {
		Map<UUID, Set<UUID>> graph = new HashMap<>();
		for (Task t : projectTasks) {
			graph.put(t.getId(), new HashSet<>(t.getDependsOn()));
		}
		graph.put(taskId, new HashSet<>(newDeps));

		// Depuis taskId, en suivant les dépendances, peut-on revenir à taskId ?
		Deque<UUID> stack = new ArrayDeque<>(newDeps);
		Set<UUID> visited = new HashSet<>();
		while (!stack.isEmpty()) {
			UUID current = stack.pop();
			if (current.equals(taskId)) {
				return true;
			}
			if (visited.add(current)) {
				stack.addAll(graph.getOrDefault(current, Set.of()));
			}
		}
		return false;
	}

	private Task load(UUID taskId) {
		return tasks.findById(taskId)
			.orElseThrow(() -> ApiException.notFound("TASK_NOT_FOUND", "Tâche introuvable."));
	}

	private static TaskStatus parseStatus(String status) {
		try {
			return TaskStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException | NullPointerException ex) {
			throw ApiException.badRequest("INVALID_STATUS", "Statut de tâche invalide : " + status);
		}
	}

	private static TaskPriority parsePriority(String priority, TaskPriority fallback) {
		if (!StringUtils.hasText(priority)) {
			return fallback;
		}
		try {
			return TaskPriority.valueOf(priority.trim().toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException ex) {
			throw ApiException.badRequest("INVALID_PRIORITY", "Priorité invalide : " + priority);
		}
	}

	private static void requireTitle(String title) {
		if (!StringUtils.hasText(title)) {
			throw ApiException.badRequest("INVALID_TITLE", "Le titre de la tâche est obligatoire.");
		}
	}

	private static String trimToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}

}
