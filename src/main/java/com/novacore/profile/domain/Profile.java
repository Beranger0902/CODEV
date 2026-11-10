package com.novacore.profile.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
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
 * Agrégat « profil » : informations professionnelles, compétences déclarées, domaines et liens.
 * Un profil est toujours rattaché à un compte ({@code userId}) ; il est créé à la première
 * consultation du profil par son titulaire.
 */
@Entity
@Table(name = "profiles")
public class Profile {

	@Id
	private UUID id;

	@Column(nullable = false, updatable = false)
	private UUID userId;

	private String headline;

	private String bio;

	private String countryCode;

	private String city;

	private String timezone;

	@Enumerated(EnumType.STRING)
	private ExperienceLevel experienceLevel;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Availability availability = Availability.NOT_SPECIFIED;

	@Column(nullable = false)
	private boolean openToMentoring;

	private String learningGoals;

	private String collaborationPreferences;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Visibility visibility = Visibility.PUBLIC;

	@Column(nullable = false)
	private boolean locationVisible = true;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "profile_skills", joinColumns = @JoinColumn(name = "profile_id"))
	private Set<ProfileSkill> skills = new LinkedHashSet<>();

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "profile_domains", joinColumns = @JoinColumn(name = "profile_id"))
	@Column(name = "domain_id", nullable = false)
	private Set<UUID> domainIds = new LinkedHashSet<>();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "profile_id", nullable = false)
	@OrderBy("displayOrder asc")
	private List<ProfileLink> links = new ArrayList<>();

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(nullable = false)
	private Instant updatedAt;

	@Version
	private long version;

	protected Profile() {
	}

	public static Profile createFor(UUID userId) {
		Profile profile = new Profile();
		profile.id = UUID.randomUUID();
		profile.userId = userId;
		return profile;
	}

	public void updateCore(String headline, String bio, String countryCode, String city, String timezone,
			ExperienceLevel experienceLevel, Availability availability, boolean openToMentoring, String learningGoals,
			String collaborationPreferences) {
		this.headline = headline;
		this.bio = bio;
		this.countryCode = countryCode;
		this.city = city;
		this.timezone = timezone;
		this.experienceLevel = experienceLevel;
		this.availability = availability != null ? availability : Availability.NOT_SPECIFIED;
		this.openToMentoring = openToMentoring;
		this.learningGoals = learningGoals;
		this.collaborationPreferences = collaborationPreferences;
	}

	public void updateVisibility(Visibility visibility, boolean locationVisible) {
		this.visibility = visibility;
		this.locationVisible = locationVisible;
	}

	/** Remplace l'ensemble des compétences. Les identifiants de technologies doivent être validés en amont. */
	public void replaceSkills(Collection<ProfileSkill> newSkills) {
		skills.clear();
		skills.addAll(newSkills);
	}

	public void replaceDomains(Collection<UUID> newDomainIds) {
		domainIds.clear();
		domainIds.addAll(newDomainIds);
	}

	public void replaceLinks(List<ProfileLink> newLinks) {
		links.clear();
		links.addAll(newLinks);
	}

	public boolean isVisibleTo(UUID viewerId) {
		return switch (visibility) {
			case PUBLIC -> true;
			case MEMBERS_ONLY -> viewerId != null;
			case PRIVATE -> userId.equals(viewerId);
		};
	}

	public UUID getId() {
		return id;
	}

	public UUID getUserId() {
		return userId;
	}

	public String getHeadline() {
		return headline;
	}

	public String getBio() {
		return bio;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public String getCity() {
		return city;
	}

	public String getTimezone() {
		return timezone;
	}

	public ExperienceLevel getExperienceLevel() {
		return experienceLevel;
	}

	public Availability getAvailability() {
		return availability;
	}

	public boolean isOpenToMentoring() {
		return openToMentoring;
	}

	public String getLearningGoals() {
		return learningGoals;
	}

	public String getCollaborationPreferences() {
		return collaborationPreferences;
	}

	public Visibility getVisibility() {
		return visibility;
	}

	public boolean isLocationVisible() {
		return locationVisible;
	}

	public Set<ProfileSkill> getSkills() {
		return skills;
	}

	public Set<UUID> getDomainIds() {
		return domainIds;
	}

	public List<ProfileLink> getLinks() {
		return links.stream().sorted(Comparator.comparingInt(ProfileLink::getDisplayOrder)).toList();
	}

}
