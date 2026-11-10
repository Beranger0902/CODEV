package com.novacore.project.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Agrégat « projet » : fiche, rôles recherchés et équipe. Le créateur en est le premier membre
 * avec le rôle {@link MemberRole#OWNER}. Les candidatures sont gérées à part (hors agrégat).
 */
@Entity
@Table(name = "projects")
public class Project {

	@Id
	private UUID id;

	@Column(nullable = false)
	private UUID ownerId;

	@Column(nullable = false)
	private String name;

	private String problem;

	private String description;

	private String objectives;

	private String features;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ProjectStatus status = ProjectStatus.DRAFT;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ProjectVisibility visibility = ProjectVisibility.PUBLIC;

	private Integer teamSizeLimit;

	private String repoUrl;

	private String demoUrl;

	@Column(nullable = false)
	private boolean applicationsOpen = true;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "project_domains", joinColumns = @JoinColumn(name = "project_id"))
	@Column(name = "domain_id", nullable = false)
	private Set<UUID> domainIds = new LinkedHashSet<>();

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "project_technologies", joinColumns = @JoinColumn(name = "project_id"))
	@Column(name = "technology_id", nullable = false)
	private Set<UUID> technologyIds = new LinkedHashSet<>();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "project_id", nullable = false)
	@OrderBy("displayOrder asc")
	private List<ProjectRole> roles = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "project_id", nullable = false)
	@OrderBy("joinedAt asc")
	private List<ProjectMember> members = new ArrayList<>();

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(nullable = false)
	private Instant updatedAt;

	@Version
	private long version;

	protected Project() {
	}

	public static Project create(UUID ownerId, String name, Instant now) {
		Project project = new Project();
		project.id = UUID.randomUUID();
		project.ownerId = ownerId;
		project.name = name;
		project.members.add(new ProjectMember(ownerId, MemberRole.OWNER, now));
		return project;
	}

	public void updateDetails(String name, String problem, String description, String objectives, String features,
			Integer teamSizeLimit, String repoUrl, String demoUrl) {
		this.name = name;
		this.problem = problem;
		this.description = description;
		this.objectives = objectives;
		this.features = features;
		this.teamSizeLimit = teamSizeLimit;
		this.repoUrl = repoUrl;
		this.demoUrl = demoUrl;
	}

	public void changeStatus(ProjectStatus status) {
		this.status = status;
	}

	public void updateVisibility(ProjectVisibility visibility) {
		this.visibility = visibility;
	}

	public void setApplicationsOpen(boolean open) {
		this.applicationsOpen = open;
	}

	public void replaceDomains(Collection<UUID> ids) {
		domainIds.clear();
		domainIds.addAll(ids);
	}

	public void replaceTechnologies(Collection<UUID> ids) {
		technologyIds.clear();
		technologyIds.addAll(ids);
	}

	public void replaceRoles(List<ProjectRole> newRoles) {
		roles.clear();
		roles.addAll(newRoles);
	}

	public ProjectRole newRole(String title, String description, int headcount, int displayOrder,
			Collection<UUID> requiredTechnologyIds) {
		return new ProjectRole(title, description, headcount, displayOrder, requiredTechnologyIds);
	}

	// --- équipe ---

	public Optional<ProjectMember> memberOf(UUID userId) {
		return members.stream().filter(m -> m.getUserId().equals(userId)).findFirst();
	}

	public boolean isMember(UUID userId) {
		return userId != null && memberOf(userId).isPresent();
	}

	public boolean canManage(UUID userId) {
		return memberOf(userId).map(m -> m.getRole().canManage()).orElse(false);
	}

	public boolean isOwner(UUID userId) {
		return ownerId.equals(userId);
	}

	public int memberCount() {
		return members.size();
	}

	public boolean isFull() {
		return teamSizeLimit != null && members.size() >= teamSizeLimit;
	}

	public ProjectMember addMember(UUID userId, MemberRole role, Instant now) {
		ProjectMember member = new ProjectMember(userId, role, now);
		members.add(member);
		return member;
	}

	public void removeMember(UUID userId) {
		members.removeIf(m -> m.getUserId().equals(userId));
	}

	public void changeMemberRole(UUID userId, MemberRole role) {
		memberOf(userId).ifPresent(m -> m.changeRole(role));
	}

	/** Transfère la responsabilité : l'ancien propriétaire devient MAINTAINER, le nouveau devient OWNER. */
	public void transferOwnership(UUID newOwnerId) {
		changeMemberRole(ownerId, MemberRole.MAINTAINER);
		changeMemberRole(newOwnerId, MemberRole.OWNER);
		this.ownerId = newOwnerId;
	}

	public boolean isVisibleTo(UUID viewerId) {
		return switch (visibility) {
			case PUBLIC -> true;
			case MEMBERS_ONLY -> viewerId != null;
			case PRIVATE -> isMember(viewerId);
		};
	}

	public boolean acceptsApplications() {
		return applicationsOpen && (status == ProjectStatus.RECRUITING || status == ProjectStatus.IN_PROGRESS);
	}

	public UUID getId() {
		return id;
	}

	public UUID getOwnerId() {
		return ownerId;
	}

	public String getName() {
		return name;
	}

	public String getProblem() {
		return problem;
	}

	public String getDescription() {
		return description;
	}

	public String getObjectives() {
		return objectives;
	}

	public String getFeatures() {
		return features;
	}

	public ProjectStatus getStatus() {
		return status;
	}

	public ProjectVisibility getVisibility() {
		return visibility;
	}

	public Integer getTeamSizeLimit() {
		return teamSizeLimit;
	}

	public String getRepoUrl() {
		return repoUrl;
	}

	public String getDemoUrl() {
		return demoUrl;
	}

	public boolean isApplicationsOpen() {
		return applicationsOpen;
	}

	public Set<UUID> getDomainIds() {
		return domainIds;
	}

	public Set<UUID> getTechnologyIds() {
		return technologyIds;
	}

	public List<ProjectRole> getRoles() {
		return roles;
	}

	public List<ProjectMember> getMembers() {
		return members;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

}
