package com.itsmarsss.callerphone.identity;

import com.itsmarsss.callerphone.match.model.MatchProfile;
import com.itsmarsss.callerphone.match.model.ProfileState;
import com.itsmarsss.callerphone.match.repository.MatchProfileRepository;
import com.itsmarsss.callerphone.match.service.MatchLimits;
import com.itsmarsss.callerphone.safety.SafetyService;

import java.time.Instant;
import java.util.Optional;

public final class EnrollmentService {
    private final MatchUserRepository users;
    private final ConsentRepository consents;
    private final MatchProfileRepository profiles;
    private final SafetyService safety;

    public EnrollmentService(
            MatchUserRepository users,
            ConsentRepository consents,
            MatchProfileRepository profiles,
            SafetyService safety
    ) {
        this.users = users;
        this.consents = consents;
        this.profiles = profiles;
        this.safety = safety;
    }

    public MatchUser getOrCreate(String userId) {
        return users.findById(userId).orElseGet(() -> {
            MatchUser created = new MatchUser(userId);
            users.save(created);
            return created;
        });
    }

    public ServiceResult beginJoin(String userId) {
        if (safety.isMatchSuspended(userId)) {
            return ServiceResult.fail("Your Match access is currently restricted.");
        }
        MatchUser user = getOrCreate(userId);
        if (user.isEnrolled() && user.getAgeCohort() != null) {
            return ServiceResult.ok("You are already enrolled. Use `/match profile` or `/match browse`.");
        }
        return ServiceResult.ok("START_ONBOARDING");
    }

    public ServiceResult acceptPolicies(String userId) {
        MatchUser user = getOrCreate(userId);
        user.setTermsVersionAccepted(MatchLimits.TERMS_VERSION);
        user.setPrivacyVersionAccepted(MatchLimits.PRIVACY_VERSION);
        user.touch();
        users.save(user);
        consents.append(ConsentEvent.of(userId, ConsentType.TERMS, MatchLimits.TERMS_VERSION, true, null));
        consents.append(ConsentEvent.of(userId, ConsentType.PRIVACY, MatchLimits.PRIVACY_VERSION, true, null));
        consents.append(ConsentEvent.of(userId, ConsentType.YOUTH_SAFETY, "v1", true, null));
        return ServiceResult.ok("Policies accepted. Next: choose your age group.");
    }

    public ServiceResult selectAgeCohort(String userId, AgeCohort cohort) {
        if (cohort == null) {
            return ServiceResult.fail("Choose a valid age group.");
        }
        MatchUser user = getOrCreate(userId);
        if (user.getTermsVersionAccepted() == null || user.getTermsVersionAccepted().isBlank()) {
            return ServiceResult.fail("Accept terms and privacy first.");
        }
        user.setAgeCohort(cohort);
        user.setAgeSelectedAt(Instant.now());
        user.setEnrolled(true);
        user.setEnrolledAt(Instant.now());
        user.setLeftAt(null);
        user.touch();
        users.save(user);
        consents.append(ConsentEvent.of(userId, ConsentType.AGE_COHORT, cohort.code(), true, null));
        consents.append(ConsentEvent.of(userId, ConsentType.ENROLLMENT, "v1", true, null));

        MatchProfile profile = profiles.findByUserId(userId).orElseGet(() -> new MatchProfile(userId));
        profile.setAgeCohort(cohort);
        if (profile.getState() == null || profile.getState() == ProfileState.DELETED) {
            profile.setState(ProfileState.DRAFT);
        }
        profile.touch();
        profiles.save(profile);
        return ServiceResult.ok("Age group set to " + cohort.label() + ". Complete your profile next.");
    }

    public ServiceResult leave(String userId) {
        MatchUser user = getOrCreate(userId);
        user.setEnrolled(false);
        user.setLeftAt(Instant.now());
        user.setSelectedConversationId(null);
        user.setConversationSelectedAt(null);
        user.touch();
        users.save(user);
        consents.append(ConsentEvent.of(userId, ConsentType.ENROLLMENT, "v1", false, "leave"));
        profiles.findByUserId(userId).ifPresent(profile -> {
            profile.setState(ProfileState.PAUSED);
            profile.touch();
            profiles.save(profile);
        });
        return ServiceResult.ok("You left Match. Your profile is paused. Use `/match join` to return.");
    }

    public ServiceResult setNotifications(String userId, boolean enabled) {
        MatchUser user = getOrCreate(userId);
        user.setNotificationsEnabled(enabled);
        user.touch();
        users.save(user);
        consents.append(ConsentEvent.of(userId, ConsentType.NOTIFICATIONS, "v1", enabled, null));
        return ServiceResult.ok(enabled ? "Match notifications enabled." : "Match notifications disabled.");
    }

    public Optional<AgeCohort> requireCohort(String userId) {
        return users.findById(userId)
                .filter(MatchUser::isEnrolled)
                .map(MatchUser::getAgeCohort);
    }

    public record ServiceResult(boolean success, String message) {
        public static ServiceResult ok(String message) {
            return new ServiceResult(true, message);
        }

        public static ServiceResult fail(String message) {
            return new ServiceResult(false, message);
        }
    }
}
