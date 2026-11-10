package com.novacore.project.application;

import java.util.List;
import java.util.UUID;

import com.novacore.project.domain.Task;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TaskServiceCycleTest {

	private final UUID project = UUID.randomUUID();

	private Task task(UUID dependsOn) {
		Task t = Task.create(project, "t", UUID.randomUUID(), 0);
		if (dependsOn != null) {
			t.replaceDependencies(List.of(dependsOn));
		}
		return t;
	}

	@Test
	void directSelfLoopIsACycle() {
		Task a = task(null);
		assertThat(TaskService.createsCycle(a.getId(), List.of(a.getId()), List.of(a))).isTrue();
	}

	@Test
	void backEdgeCreatesCycle() {
		// a dépend de b (existant). Faire dépendre b de a fermerait le cycle.
		Task b = task(null);
		Task a = task(b.getId());

		assertThat(TaskService.createsCycle(b.getId(), List.of(a.getId()), List.of(a, b))).isTrue();
	}

	@Test
	void independentDependencyIsFine() {
		Task b = task(null);
		Task a = task(null);

		assertThat(TaskService.createsCycle(a.getId(), List.of(b.getId()), List.of(a, b))).isFalse();
	}

	@Test
	void transitiveChainWithoutCycleIsFine() {
		// c -> b -> a ; ajouter d -> c ne crée pas de cycle
		Task a = task(null);
		Task b = task(a.getId());
		Task c = task(b.getId());
		Task d = task(null);

		assertThat(TaskService.createsCycle(d.getId(), List.of(c.getId()), List.of(a, b, c, d))).isFalse();
	}

	@Test
	void transitiveBackEdgeIsACycle() {
		// c -> b -> a ; faire dépendre a de c fermerait le cycle a->c->b->a
		Task a = task(null);
		Task b = task(a.getId());
		Task c = task(b.getId());

		assertThat(TaskService.createsCycle(a.getId(), List.of(c.getId()), List.of(a, b, c))).isTrue();
	}

}
