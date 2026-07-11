package com.itsmarsss.callerphone.match.model;

import com.itsmarsss.callerphone.identity.AgeCohort;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class MatchProfile {
    private String userId;
    private int schemaVersion = 1;
    private ProfileState state = ProfileState.DRAFT;
    private String displayName = "";
    private AgeCohort ageCohort;
    private Gender gender;
    private String pronouns = "";
    private List<Gender> openToMeeting = new ArrayList<>();
    private String bio = "";
    private List<String> interests = new ArrayList<>();
    private List<ProfilePrompt> prompts = new ArrayList<>();
    private List<MediaRef> media = new ArrayList<>();
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();
    private Instant lastActiveAt = Instant.now();
    private int onboardingStep;
    private long discoveryViewsToday;
    private long interestSignalsToday;
    private String usageDay = "";

    public MatchProfile() {
    }

    public MatchProfile(String userId) {
        this.userId = Objects.requireNonNull(userId);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public ProfileState getState() {
        return state;
    }

    public void setState(ProfileState state) {
        this.state = state;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName == null ? "" : displayName.trim();
    }

    public AgeCohort getAgeCohort() {
        return ageCohort;
    }

    public void setAgeCohort(AgeCohort ageCohort) {
        this.ageCohort = ageCohort;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getPronouns() {
        return pronouns;
    }

    public void setPronouns(String pronouns) {
        this.pronouns = pronouns == null ? "" : pronouns.trim();
    }

    public List<Gender> getOpenToMeeting() {
        return openToMeeting;
    }

    public void setOpenToMeeting(List<Gender> openToMeeting) {
        this.openToMeeting = openToMeeting == null ? new ArrayList<>() : new ArrayList<>(openToMeeting);
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio == null ? "" : bio.trim();
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests == null ? new ArrayList<>() : new ArrayList<>(interests);
    }

    public List<ProfilePrompt> getPrompts() {
        return prompts;
    }

    public void setPrompts(List<ProfilePrompt> prompts) {
        this.prompts = prompts == null ? new ArrayList<>() : new ArrayList<>(prompts);
    }

    public List<MediaRef> getMedia() {
        return media;
    }

    public void setMedia(List<MediaRef> media) {
        this.media = media == null ? new ArrayList<>() : new ArrayList<>(media);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getLastActiveAt() {
        return lastActiveAt;
    }

    public void setLastActiveAt(Instant lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }

    public int getOnboardingStep() {
        return onboardingStep;
    }

    public void setOnboardingStep(int onboardingStep) {
        this.onboardingStep = onboardingStep;
    }

    public long getDiscoveryViewsToday() {
        return discoveryViewsToday;
    }

    public void setDiscoveryViewsToday(long discoveryViewsToday) {
        this.discoveryViewsToday = discoveryViewsToday;
    }

    public long getInterestSignalsToday() {
        return interestSignalsToday;
    }

    public void setInterestSignalsToday(long interestSignalsToday) {
        this.interestSignalsToday = interestSignalsToday;
    }

    public String getUsageDay() {
        return usageDay;
    }

    public void setUsageDay(String usageDay) {
        this.usageDay = usageDay == null ? "" : usageDay;
    }

    public void touch() {
        Instant now = Instant.now();
        this.updatedAt = now;
        this.lastActiveAt = now;
    }
}
