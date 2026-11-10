package com.novacore.project.api;

import java.util.UUID;

import com.novacore.project.api.ProjectBoardDtos.DependenciesRequest;
import com.novacore.project.api.ProjectBoardDtos.TaskMoveRequest;
import com.novacore.project.api.ProjectBoardDtos.TaskRequest;
import com.novacore.project.application.ProjectBoardInputs.DependenciesUpdate;
import com.novacore.project.application.ProjectBoardInputs.TaskCreate;
import com.novacore.project.application.ProjectBoardInputs.TaskMove;
import com.novacore.project.application.ProjectBoardInputs.TaskUpdate;
import com.novacore.project.application.ProjectBoardViews.BoardView;
import com.novacore.project.application.ProjectBoardViews.TaskView;
import com.novacore.project.application.TaskService;
import com.novacore.shared.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Projets — tâches et Kanban")
class ProjectTaskController {

	private final TaskService tasks;

	ProjectTaskController(TaskService tasks) {
		this.tasks = tasks;
	}

	@Operation(summary = "Tableau Kanban du projet (membres)")
	@GetMapping("/projects/{id}/board")
	BoardView board(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
		return tasks.board(id, CurrentUser.id(jwt));
	}

	@Operation(summary = "Créer une tâche")
	@PostMapping("/projects/{id}/tasks")
	@ResponseStatus(HttpStatus.CREATED)
	TaskView create(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @Valid @RequestBody TaskRequest r) {
		return tasks.create(id, CurrentUser.id(jwt),
				new TaskCreate(r.title(), r.description(), r.priority(), r.assigneeId(), r.dueDate(), r.milestoneId()));
	}

	@Operation(summary = "Consulter une tâche")
	@GetMapping("/tasks/{taskId}")
	TaskView get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID taskId) {
		return tasks.get(taskId, CurrentUser.id(jwt));
	}

	@Operation(summary = "Modifier une tâche")
	@PutMapping("/tasks/{taskId}")
	TaskView update(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID taskId, @Valid @RequestBody TaskRequest r) {
		return tasks.update(taskId, CurrentUser.id(jwt),
				new TaskUpdate(r.title(), r.description(), r.priority(), r.assigneeId(), r.dueDate(), r.milestoneId()));
	}

	@Operation(summary = "Déplacer une tâche dans le Kanban (colonne et position)")
	@PutMapping("/tasks/{taskId}/move")
	TaskView move(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID taskId, @Valid @RequestBody TaskMoveRequest r) {
		return tasks.move(taskId, CurrentUser.id(jwt), new TaskMove(r.status(), r.boardOrder()));
	}

	@Operation(summary = "Définir les dépendances d'une tâche (détecte les cycles)")
	@PutMapping("/tasks/{taskId}/dependencies")
	TaskView setDependencies(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID taskId,
			@RequestBody DependenciesRequest r) {
		return tasks.setDependencies(taskId, CurrentUser.id(jwt), new DependenciesUpdate(r.dependsOn()));
	}

	@Operation(summary = "Supprimer une tâche (auteur ou responsable)")
	@DeleteMapping("/tasks/{taskId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID taskId) {
		tasks.delete(taskId, CurrentUser.id(jwt));
	}

}
