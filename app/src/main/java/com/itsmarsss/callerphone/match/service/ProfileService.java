package com.itsmarsss.callerphone.match.service;

import com.itsmarsss.callerphone.analytics.AnalyticsService;
import com.itsmarsss.callerphone.identity.EnrollmentService;
import com.itsmarsss.callerphone.identity.MatchUser;
import com.itsmarsss.callerphone.identity.MatchUserRepository;
import com.itsmarsss.callerphone.match.model.Gender;
import com.itsmarsss.callerphone.match.model.MatchProfile;
import com.itsmarsss.callerphone.match.model.MediaRef;
import com.itsmarsss.callerphone.match.model.ProfilePrompt;
import com.itsmarsss.callerphone.match.model.ProfileState;
import com.itsmarsss.callerphone.match.repository.MatchProfileRepository;
import com.itsmarsss.callerphone.match.validation.ProfileValidator;
import com.itsmarsss.callerphone.safety.SafetyService;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class ProfileService {
    private final MatchProfileRepository profiles;
    private final MatchUserRepository users;
    private final EnrollmentService enrollment;
    private final SafetyService safety;
    private NotificationService notifications;
    private AnalyticsService analytics;

    public ProfileService(
            MatchProfileRepository profiles,
            MatchUserRepository users,
            EnrollmentService enrollment,
            SafetyService safety
    ) {
        this.profiles = profiles;
        this.users = users;
        this.enrollment = enrollment;
        this.safety = safety;
    }

    public void setNotifications(NotificationService notifications) {
        this.notifications = notifications;
    }

    public void setAnalytics(AnalyticsService analytics) {
        this.analytics = analytics;
    }

    public Optional<MatchProfile> find(String userId) {
        return profiles.findByUserId(userId);
    }

    public MatchProfile getOrCreateDraft(String userId) {
        return profiles.findByUserId(userId).orElseGet(() -> {
            MatchProfile profile = new MatchProfile(userId);
            users.findById(userId).map(MatchUser::getAgeCohort).ifPresent(profile::setAgeCohort);
            profiles.save(profile);
            return profile;
        });
    }

    public EnrollmentService.ServiceResult updateBasics(
            String userId,
            String displayName,
            Gender gender,
            String pronouns,
            List<Gender> openToMeeting
    ) {
        if (safety.isMatchSuspended(userId)) {
            return EnrollmentService.ServiceResult.fail("Your Match access is restricted.");
        }
        List<String> errors = ProfileValidator.validateDisplayName(displayName);
        if (!errors.isEmpty()) {
            return EnrollmentService.ServiceResult.fail(String.join(" ", errors));
        }
        MatchProfile profile = getOrCreateDraft(userId);
        ensureCohort(profile, userId);
        profile.setDisplayName(displayName);
        profile.setGender(gender);
        profile.setPronouns(pronouns == null ? "" : pronouns);
        profile.setOpenToMeeting(openToMeeting == null ? List.of() : openToMeeting);
        profile.setOnboardingStep(Math.max(profile.getOnboardingStep(), 3));
        profile.touch();
        profiles.save(profile);
        return EnrollmentService.ServiceResult.ok("Profile basics saved.");
    }

    public EnrollmentService.ServiceResult updateBioAndPrompt(String userId, String bio, String promptAnswer) {
        List<String> errors = ProfileValidator.validateBio(bio);
        errors.addAll(ProfileValidator.validatePromptAnswer(promptAnswer));
        if (!errors.isEmpty()) {
            return EnrollmentService.ServiceResult.fail(String.join(" ", errors));
        }
        MatchProfile profile = getOrCreateDraft(userId);
        ensureCohort(profile, userId);
        profile.setBio(bio);
        profile.setPrompts(List.of(new ProfilePrompt("ideal_sunday", promptAnswer)));
        profile.setOnboardingStep(Math.max(profile.getOnboardingStep(), 4));
        profile.touch();
        profiles.save(profile);
        return EnrollmentService.ServiceResult.ok("Bio and prompt saved.");
    }

    public EnrollmentService.ServiceResult updateInterests(String userId, List<String> interests) {
        List<String> cleaned = interests == null ? List.of() : interests.stream()
                .map(s -> s == null ? "" : s.trim().toLowerCase())
                .filter(s -> !s.isBlank())
                .distinct()
                .limit(5)
                .toList();
        List<String> errors = ProfileValidator.validateInterests(cleaned);
        if (!errors.isEmpty()) {
            return EnrollmentService.ServiceResult.fail(String.join(" ", errors));
        }
        MatchProfile profile = getOrCreateDraft(userId);
        ensureCohort(profile, userId);
        profile.setInterests(cleaned);
        profile.setOnboardingStep(Math.max(profile.getOnboardingStep(), 5));
        profile.touch();
        profiles.save(profile);
        return EnrollmentService.ServiceResult.ok("Interests saved.");
    }

    public EnrollmentService.ServiceResult setAvatar(String userId, String avatarUrl) {
        MatchProfile profile = getOrCreateDraft(userId);
        ensureCohort(profile, userId);
        // Discord avatar for everyone — age group is pairing only, not feature tiers
        profile.setMedia(List.of(MediaRef.avatar(UUID.randomUUID().toString(), avatarUrl)));
        profile.setOnboardingStep(Math.max(profile.getOnboardingStep(), 6));
        profile.touch();
        profiles.save(profile);
        return EnrollmentService.ServiceResult.ok("Avatar linked from Discord.");
    }

    /** Publishes the profile for discovery. No moderator gate — reports handle abuse. */
    public EnrollmentService.ServiceResult publish(String userId) {
        MatchProfile profile = getOrCreateDraft(userId);
        ensureCohort(profile, userId);
        if (safety.isMatchSuspended(userId)) {
            return EnrollmentService.ServiceResult.fail("Your Match access is restricted.");
        }
        if (profile.getAgeCohort() == null) {
            return EnrollmentService.ServiceResult.fail("Choose an age group first.");
        }
        if (profile.getDisplayName() == null || profile.getDisplayName().isBlank()) {
            return EnrollmentService.ServiceResult.fail("Set a display name first.");
        }
        if (profile.getBio() == null || profile.getBio().isBlank()) {
            return EnrollmentService.ServiceResult.fail("Write a short bio first.");
        }
        if (profile.getInterests() == null || profile.getInterests().isEmpty()) {
            return EnrollmentService.ServiceResult.fail("Pick interests first.");
        }
        profile.setState(ProfileState.ACTIVE);
        profile.setOnboardingStep(7);
        profile.touch();
        profiles.save(profile);
        if (analytics != null) {
            analytics.track(userId, "match_profile_live", profile.getAgeCohort().code());
        }
        if (notifications != null) {
            notifications.notifyProfileLive(userId);
        }
        return EnrollmentService.ServiceResult.ok(
                "You're live! Use `/match browse` to discover people in your age group.");
    }

    /** @deprecated use {@link #publish(String)} */
    @Deprecated
    public EnrollmentService.ServiceResult submitForReview(String userId) {
        return publish(userId);
    }

    public EnrollmentService.ServiceResult pause(String userId) {
        MatchProfile profile = getOrCreateDraft(userId);
        if (profile.getState() == ProfileState.ACTIVE) {
            profile.setState(ProfileState.PAUSED);
            profile.touch();
            profiles.save(profile);
            return EnrollmentService.ServiceResult.ok("Profile paused. Use `/match resume` when you want discovery again.");
        }
        return EnrollmentService.ServiceResult.fail("Nothing to pause.");
    }

    public EnrollmentService.ServiceResult resume(String userId) {
        MatchProfile profile = getOrCreateDraft(userId);
        if (profile.getState() != ProfileState.PAUSED) {
            return EnrollmentService.ServiceResult.fail("Profile is not paused.");
        }
        if (!ProfileChecklist.readyToSubmit(profile)) {
            profile.setState(ProfileState.DRAFT);
            profile.touch();
            profiles.save(profile);
            return EnrollmentService.ServiceResult.fail("Finish your profile, then use `/match submit` to go live.");
        }
        if (safety.isMatchSuspended(userId)) {
            return EnrollmentService.ServiceResult.fail("Your Match access is restricted.");
        }
        profile.setState(ProfileState.ACTIVE);
        profile.touch();
        profiles.save(profile);
        return EnrollmentService.ServiceResult.ok("You're live again. `/match browse` when ready.");
    }

    /** Staff force-activate (rare). Normal users publish themselves. */
    public EnrollmentService.ServiceResult forceActive(String moderatorId, String userId) {
        MatchProfile profile = profiles.findByUserId(userId).orElse(null);
        if (profile == null) {
            return EnrollmentService.ServiceResult.fail("No profile found.");
        }
        profile.setState(ProfileState.ACTIVE);
        profile.touch();
        profiles.save(profile);
        return EnrollmentService.ServiceResult.ok("Forced active for " + userId);
    }

    /** Staff force-pause after a report (not a pre-publish review). */
    public EnrollmentService.ServiceResult forcePause(String moderatorId, String userId, String reason) {
        MatchProfile profile = profiles.findByUserId(userId).orElse(null);
        if (profile == null) {
            return EnrollmentService.ServiceResult.fail("No profile found.");
        }
        profile.setState(ProfileState.PAUSED);
        profile.touch();
        profiles.save(profile);
        if (notifications != null) {
            notifications.notifyProfileRestricted(userId, reason);
        }
        return EnrollmentService.ServiceResult.ok("Profile paused for " + userId + ": " + reason);
    }

    public EnrollmentService.ServiceResult approve(String moderatorId, String userId) {
        return forceActive(moderatorId, userId);
    }

    public EnrollmentService.ServiceResult reject(String moderatorId, String userId, String reason) {
        return forcePause(moderatorId, userId, reason);
    }

    public List<MatchProfile> pendingReview(int limit) {
        // Legacy name: list recently active/paused for staff tooling if needed
        return profiles.findByState(ProfileState.ACTIVE, limit);
    }

    public void bumpDiscoveryView(MatchProfile profile) {
        resetDailyCountersIfNeeded(profile);
        profile.setDiscoveryViewsToday(profile.getDiscoveryViewsToday() + 1);
        profile.touch();
        profiles.save(profile);
    }

    public void bumpInterest(MatchProfile profile) {
        resetDailyCountersIfNeeded(profile);
        profile.setInterestSignalsToday(profile.getInterestSignalsToday() + 1);
        profile.touch();
        profiles.save(profile);
    }

    public void resetDailyCountersIfNeeded(MatchProfile profile) {
        String today = LocalDate.now(ZoneOffset.UTC).toString();
        if (!today.equals(profile.getUsageDay())) {
            profile.setUsageDay(today);
            profile.setDiscoveryViewsToday(0);
            profile.setInterestSignalsToday(0);
        }
    }

    private void ensureCohort(MatchProfile profile, String userId) {
        if (profile.getAgeCohort() == null) {
            enrollment.requireCohort(userId).ifPresent(profile::setAgeCohort);
        }
    }
}
