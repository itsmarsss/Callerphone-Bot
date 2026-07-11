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
            return EnrollmentService.ServiceResult.fail("Match is paused on your account.");
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
        return EnrollmentService.ServiceResult.ok("Saved.");
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
        return EnrollmentService.ServiceResult.ok("Saved.");
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
        return EnrollmentService.ServiceResult.ok("Saved.");
    }

    /**
     * One-shot setup from the guided join flow. Saves name/bio/prompt/interests and goes live.
     * Gender/open-to stay optional (edit later if wanted).
     */
    public EnrollmentService.ServiceResult completeQuickSetup(
            String userId,
            String displayName,
            String pronouns,
            String bio,
            String promptAnswer,
            List<String> interests,
            String avatarUrl
    ) {
        if (safety.isMatchSuspended(userId)) {
            return EnrollmentService.ServiceResult.fail("Match is paused on your account.");
        }
        List<String> errors = new java.util.ArrayList<>();
        errors.addAll(ProfileValidator.validateDisplayName(displayName));
        errors.addAll(ProfileValidator.validateBio(bio));
        errors.addAll(ProfileValidator.validatePromptAnswer(promptAnswer));
        List<String> cleaned = interests == null ? List.of() : interests.stream()
                .map(s -> s == null ? "" : s.trim().toLowerCase())
                .filter(s -> !s.isBlank())
                .distinct()
                .limit(5)
                .toList();
        errors.addAll(ProfileValidator.validateInterests(cleaned));
        if (!errors.isEmpty()) {
            return EnrollmentService.ServiceResult.fail(String.join(" ", errors));
        }

        MatchProfile profile = getOrCreateDraft(userId);
        ensureCohort(profile, userId);
        if (profile.getAgeCohort() == null) {
            return EnrollmentService.ServiceResult.fail("Pick an age group first.");
        }
        profile.setDisplayName(displayName);
        profile.setPronouns(pronouns == null ? "" : pronouns.trim());
        profile.setBio(bio);
        profile.setPrompts(List.of(new ProfilePrompt("ideal_sunday", promptAnswer)));
        profile.setInterests(cleaned);
        profile.setOnboardingStep(7);
        if (avatarUrl != null && !avatarUrl.isBlank()) {
            List<MediaRef> media = new java.util.ArrayList<>();
            media.add(MediaRef.avatar(UUID.randomUUID().toString(), avatarUrl));
            profile.setMedia(media);
        }
        profile.setState(ProfileState.ACTIVE);
        profile.touch();
        profiles.save(profile);
        if (analytics != null) {
            analytics.track(userId, "match_profile_live", profile.getAgeCohort().code());
        }
        if (notifications != null) {
            notifications.notifyProfileLive(userId);
        }
        return EnrollmentService.ServiceResult.ok("You're live in **" + profile.getAgeCohort().label() + "**.");
    }

    public EnrollmentService.ServiceResult setAvatar(String userId, String avatarUrl) {
        MatchProfile profile = getOrCreateDraft(userId);
        ensureCohort(profile, userId);
        // Keep avatar as primary media slot 0
        List<MediaRef> media = new java.util.ArrayList<>();
        media.add(MediaRef.avatar(UUID.randomUUID().toString(), avatarUrl));
        if (profile.getMedia() != null) {
            for (MediaRef ref : profile.getMedia()) {
                if (ref != null && !"discord_avatar".equals(ref.source()) && media.size() < MatchLimits.MAX_PROFILE_PHOTOS) {
                    media.add(ref);
                }
            }
        }
        profile.setMedia(media);
        profile.setOnboardingStep(Math.max(profile.getOnboardingStep(), 6));
        profile.touch();
        profiles.save(profile);
        return EnrollmentService.ServiceResult.ok("Photo updated.");
    }

    /** Optional extra photo via Discord CDN URL (same for all age groups). */
    public EnrollmentService.ServiceResult addPhotoUrl(String userId, String url) {
        if (url == null || url.isBlank() || !url.startsWith("https://")) {
            return EnrollmentService.ServiceResult.fail("Need a valid image link.");
        }
        MatchProfile profile = getOrCreateDraft(userId);
        List<MediaRef> media = profile.getMedia() == null
                ? new java.util.ArrayList<>()
                : new java.util.ArrayList<>(profile.getMedia());
        long extras = media.stream().filter(m -> m != null && !"discord_avatar".equals(m.source())).count();
        if (extras >= MatchLimits.MAX_PROFILE_PHOTOS - 1) {
            return EnrollmentService.ServiceResult.fail("Max " + (MatchLimits.MAX_PROFILE_PHOTOS - 1) + " extra photos.");
        }
        media.add(MediaRef.discordAttachment(
                UUID.randomUUID().toString(), null, null, url, "image", media.size()));
        profile.setMedia(media);
        profile.touch();
        profiles.save(profile);
        return EnrollmentService.ServiceResult.ok("Photo added (" + media.size() + ").");
    }

    /** Publishes the profile for discovery. No moderator gate — reports handle abuse. */
    public EnrollmentService.ServiceResult publish(String userId) {
        MatchProfile profile = getOrCreateDraft(userId);
        ensureCohort(profile, userId);
        if (safety.isMatchSuspended(userId)) {
            return EnrollmentService.ServiceResult.fail("Match is paused on your account.");
        }
        if (profile.getAgeCohort() == null) {
            return EnrollmentService.ServiceResult.fail("Pick an age group first.");
        }
        if (profile.getDisplayName() == null || profile.getDisplayName().isBlank()) {
            return EnrollmentService.ServiceResult.fail("Add a display name.");
        }
        if (profile.getBio() == null || profile.getBio().isBlank()) {
            return EnrollmentService.ServiceResult.fail("Add a short bio.");
        }
        if (profile.getInterests() == null || profile.getInterests().isEmpty()) {
            return EnrollmentService.ServiceResult.fail("Add a few interests.");
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
            return EnrollmentService.ServiceResult.ok("Profile paused.");
        }
        return EnrollmentService.ServiceResult.fail("Nothing to pause.");
    }

    public EnrollmentService.ServiceResult resume(String userId) {
        MatchProfile profile = getOrCreateDraft(userId);
        if (profile.getState() != ProfileState.PAUSED) {
            return EnrollmentService.ServiceResult.fail("You're not paused.");
        }
        if (!ProfileChecklist.readyToSubmit(profile)) {
            profile.setState(ProfileState.DRAFT);
            profile.touch();
            profiles.save(profile);
            return EnrollmentService.ServiceResult.fail("Finish setup first.");
        }
        if (safety.isMatchSuspended(userId)) {
            return EnrollmentService.ServiceResult.fail("Match is paused on your account.");
        }
        profile.setState(ProfileState.ACTIVE);
        profile.touch();
        profiles.save(profile);
        return EnrollmentService.ServiceResult.ok("You're live again.");
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
