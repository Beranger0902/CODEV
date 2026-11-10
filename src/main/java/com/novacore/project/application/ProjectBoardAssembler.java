package com.novacore.project.application;

import java.util.List;

import com.novacore.identity.AccountSummary;
import com.novacore.identity.Accounts;
import com.novacore.project.application.ProjectBoardViews.MilestoneView;
import com.novacore.project.application.ProjectBoardViews.TaskView;
import com.novacore.project.domain.Milestone;
import com.novacore.project.domain.Task;
import org.springframework.stereotype.Component;

@Component
class ProjectBoardAssembler {

	private final Accounts accounts;

	ProjectBoardAssembler(Accounts accounts) {
		this.accounts = accounts;
	}

	TaskView toTaskView(Task task) {
		AccountSummary assignee = task.getAssigneeId() == null ? null
				: accounts.findById(task.getAssigneeId()).orElse(null);
		return new TaskView(task.getId(), task.getTitle(), task.getDescription(), task.getStatus().name(),
				task.getPriority().name(), task.getAssigneeId(), assignee != null ? assignee.username() : null,
				assignee != null ? assignee.displayName() : null, task.getDueDate(), task.getMilestoneId(),
				task.getBoardOrder(), List.copyOf(task.getDependsOn()), task.getCreatedBy(), task.getCreatedAt());
	}

	MilestoneView toMilestoneView(Milestone milestone, long taskCount, long doneCount) {
		return new MilestoneView(milestone.getId(), milestone.getTitle(), milestone.getDescription(),
				milestone.getDueDate(), milestone.getStatus().name(), milestone.getDisplayOrder(), taskCount,
				doneCount);
	}

}
