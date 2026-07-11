package com.itsmarsss.callerphone.match.service;

import com.itsmarsss.callerphone.identity.AgeCohort;
import com.itsmarsss.callerphone.identity.EnrollmentService;
import com.itsmarsss.callerphone.identity.MatchUserRepository;
import com.itsmarsss.callerphone.match.model.Gender;
import com.itsmarsss.callerphone.match.model.Match;
import com.itsmarsss.callerphone.match.model.MatchProfile;
import com.itsmarsss.callerphone.match.model.ProfileState;
import com.itsmarsss.callerphone.match.repository.MatchDecisionRepository;
import com.itsmarsss.callerphone.match.repository.MatchProfileRepository;
import com.itsmarsss.callerphone.match.repository.MatchRepository;
import com.itsmarsss.callerphone.safety.SafetyService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public final class DiscoveryService {
    private final MatchProfileRepository profiles;
    private final MatchDecisionRepository decisions;
    private final MatchRepository matches;
    private final MatchUserRepository users;
    private final SafetyService safety;
    private final ProfileService profileService;
    private final PremiumService premium;
    private final BrowseSessionStore sessions;

    public DiscoveryService(
            MatchProfileRepository profiles,
            MatchDecisionRepository decisions,
            MatchRepository matches,
            MatchUserRepository users,
            SafetyService safety,
            ProfileService profileService,
            PremiumService premium,
            BrowseSessionStore sessions
    ) {
        this.profiles = profiles;
        this.decisions = decisions;
        this.matches = matches;
        this.users = users;
        this.safety = safety;
        this.profileService = profileService;
        this.premium = premium;
        this.sessions = sessions;
    }

    public DiscoveryResult next(String viewerId) {
        if (safety.isMatchSuspended(viewerId)) {
            return DiscoveryResult.fail("Your Match access is restricted.");
        }
        Optional<AgeCohort> cohort = users.findById(viewerId)
                .filter(u -> u.isEnrolled())
                .map(u -> u.getAgeCohort());
        if (cohort.isEmpty()) {
            return DiscoveryResult.fail("Join Match and choose an age group first (`/match join`).");
        }
        Optional<MatchProfile> viewerOpt = profiles.findByUserId(viewerId);
        if (viewerOpt.isEmpty() || viewerOpt.get().getState() != ProfileState.ACTIVE) {
            return DiscoveryResult.fail("Your profile must be active before browsing.");
        }
        MatchProfile viewer = viewerOpt.get();
        if (!cohort.get().equals(viewer.getAgeCohort())) {
            return DiscoveryResult.fail("Age group mismatch. Re-select your age group.");
        }
        profileService.resetDailyCountersIfNeeded(viewer);
        int discoveryLimit = premium.dailyDiscoveries(viewerId);
        if (viewer.getDiscoveryViewsToday() >= discoveryLimit) {
            return DiscoveryResult.fail(
                    "Daily discovery limit reached (" + discoveryLimit
                            + "). Come back tomorrow — Premium will raise this later.");
        }

        Set<String> exclude = new HashSet<>(decisions.findSubjectIdsForViewer(viewerId));
        exclude.add(viewerId);
        for (Match match : matches.findActiveByUserId(viewerId)) {
            exclude.addAll(match.getUserIds());
        }

        List<MatchProfile> candidates = profiles.findCandidates(
                new MatchProfileRepository.CandidateQuery(viewerId, cohort.get(), exclude, 40)
        );
        List<Scored> scored = new ArrayList<>();
        for (MatchProfile candidate : candidates) {
            if (candidate.getAgeCohort() != cohort.get()) {
                continue; // hard isolation
            }
            if (safety.isBlockedEitherWay(viewerId, candidate.getUserId())) {
                continue;
            }
            if (safety.isMatchSuspended(candidate.getUserId())) {
                continue;
            }
            if (!preferencesCompatible(viewer, candidate)) {
                continue;
            }
            scored.add(new Scored(candidate, score(viewer, candidate)));
        }
        if (scored.isEmpty()) {
            return DiscoveryResult.fail("No more profiles right now. Check back later.");
        }
        scored.sort(Comparator.comparingDouble(Scored::score).reversed());
        int top = Math.min(5, scored.size());
        MatchProfile pick = scored.get(ThreadLocalRandom.current().nextInt(top)).profile();
        profileService.bumpDiscoveryView(viewer);
        BrowseSession session = sessions.create(viewerId, pick.getUserId());
        return DiscoveryResult.profile(pick, session);
    }

    public Optional<BrowseSession> session(String sessionId) {
        return sessions.find(sessionId);
    }

    public void clearSession(String sessionId) {
        sessions.delete(sessionId);
    }

    private static boolean preferencesCompatible(MatchProfile viewer, MatchProfile candidate) {
        // Optional open-to-meeting prefs apply the same way for every age group
        if (viewer.getOpenToMeeting() != null && !viewer.getOpenToMeeting().isEmpty() && candidate.getGender() != null) {
            if (!viewer.getOpenToMeeting().contains(candidate.getGender())
                    && !viewer.getOpenToMeeting().contains(Gender.OTHER)) {
                return false;
            }
        }
        if (candidate.getOpenToMeeting() != null && !candidate.getOpenToMeeting().isEmpty() && viewer.getGender() != null) {
            if (!candidate.getOpenToMeeting().contains(viewer.getGender())
                    && !candidate.getOpenToMeeting().contains(Gender.OTHER)) {
                return false;
            }
        }
        return true;
    }

    private static double score(MatchProfile viewer, MatchProfile candidate) {
        double shared = 0;
        if (viewer.getInterests() != null && candidate.getInterests() != null) {
            for (String interest : viewer.getInterests()) {
                if (candidate.getInterests().contains(interest)) {
                    shared += 1.0;
                }
            }
        }
        long recencyBoost = candidate.getLastActiveAt() == null
                ? 0
                : Math.max(0, 7 - java.time.Duration.between(candidate.getLastActiveAt(), java.time.Instant.now()).toDays());
        double noise = ThreadLocalRandom.current().nextDouble(0, 0.35);
        return shared * 2.0 + recencyBoost * 0.15 + noise;
    }

    private record Scored(MatchProfile profile, double score) {
    }

    public record DiscoveryResult(boolean success, String message, MatchProfile profile, BrowseSession session) {
        public static DiscoveryResult fail(String message) {
            return new DiscoveryResult(false, message, null, null);
        }

        public static DiscoveryResult profile(MatchProfile profile, BrowseSession session) {
            return new DiscoveryResult(true, "ok", profile, session);
        }
    }
}
