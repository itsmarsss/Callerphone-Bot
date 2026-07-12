package com.itsmarsss.callerphone.match.service;

import com.itsmarsss.callerphone.analytics.AnalyticsService;
import com.itsmarsss.callerphone.identity.MatchUser;
import com.itsmarsss.callerphone.identity.MatchUserRepository;
import com.itsmarsss.callerphone.match.model.ConversationStage;
import com.itsmarsss.callerphone.match.model.DecisionType;
import com.itsmarsss.callerphone.match.model.Match;
import com.itsmarsss.callerphone.match.model.MatchConversation;
import com.itsmarsss.callerphone.match.model.MatchDecision;
import com.itsmarsss.callerphone.match.model.MatchProfile;
import com.itsmarsss.callerphone.match.model.MatchStatus;
import com.itsmarsss.callerphone.match.model.ProfileState;
import com.itsmarsss.callerphone.match.repository.MatchConversationRepository;
import com.itsmarsss.callerphone.match.repository.MatchDecisionRepository;
import com.itsmarsss.callerphone.match.repository.MatchRepository;
import com.itsmarsss.callerphone.safety.SafetyService;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class DecisionService {
    private final MatchDecisionRepository decisions;
    private final MatchRepository matches;
    private final MatchConversationRepository conversations;
    private final DiscoveryService discovery;
    private final ProfileService profiles;
    private final PremiumService premium;
    private final SafetyService safety;
    private final NotificationService notifications;
    private final AnalyticsService analytics;
    private final MatchUserRepository users;

    public DecisionService(
            MatchDecisionRepository decisions,
            MatchRepository matches,
            MatchConversationRepository conversations,
            DiscoveryService discovery,
            ProfileService profiles,
            PremiumService premium,
            SafetyService safety,
            NotificationService notifications,
            AnalyticsService analytics,
            MatchUserRepository users
    ) {
        this.decisions = decisions;
        this.matches = matches;
        this.conversations = conversations;
        this.discovery = discovery;
        this.profiles = profiles;
        this.premium = premium;
        this.safety = safety;
        this.notifications = notifications;
        this.analytics = analytics;
        this.users = users;
    }

    public DecisionResult decide(String actorId, String sessionId, DecisionType type) {
        Optional<BrowseSession> sessionOpt = discovery.session(sessionId);
        if (sessionOpt.isEmpty() || sessionOpt.get().isExpired(Instant.now())) {
            return DecisionResult.fail("That card expired. Open a fresh profile to continue.");
        }
        BrowseSession session = sessionOpt.get();
        if (!session.viewerId().equals(actorId)) {
            return DecisionResult.fail("That card isn't yours.");
        }
        if (safety.isMatchSuspended(actorId) || safety.isBlockedEitherWay(actorId, session.subjectId())) {
            discovery.clearSession(sessionId);
            return DecisionResult.fail("You can't act on this profile.");
        }

        Optional<MatchProfile> viewerOpt = profiles.find(actorId);
        if (viewerOpt.isEmpty()) {
            return DecisionResult.fail("Finish your profile first.");
        }
        MatchProfile viewer = viewerOpt.get();
        profiles.resetDailyCountersIfNeeded(viewer);

        if (type == DecisionType.INTERESTED) {
            int limit = premium.dailyInterests(actorId);
            if (viewer.getInterestSignalsToday() >= limit) {
                return DecisionResult.fail(premium.upsellForLimit("interest"));
            }
        }

        Instant expiresAt = type == DecisionType.SKIP
                ? Instant.now().plus(Duration.ofDays(MatchLimits.SKIP_EXPIRE_DAYS))
                : null;
        decisions.upsert(new MatchDecision(actorId, session.subjectId(), type, Instant.now(), expiresAt));
        if (type == DecisionType.INTERESTED) {
            profiles.bumpInterest(viewer);
        }
        if (type == DecisionType.SKIP) {
            MatchUser user = users.findById(actorId).orElseGet(() -> new MatchUser(actorId));
            user.setLastSkipSubjectId(session.subjectId());
            user.setLastSkipAt(Instant.now());
            users.save(user);
        }
        discovery.clearSession(sessionId);

        if (type == DecisionType.INTERESTED
                && decisions.findReciprocalInterest(session.subjectId(), actorId).isPresent()) {
            int actorCap = premium.activeConversations(actorId);
            int peerCap = premium.activeConversations(session.subjectId());
            if (conversations.countActiveByUserId(actorId) >= actorCap
                    || conversations.countActiveByUserId(session.subjectId()) >= peerCap) {
                return DecisionResult.interested(premium.upsellForLimit("conversations"));
            }
            MutualCreate created = createMutualMatch(actorId, session.subjectId());
            notifications.notifyMutualMatch(actorId, session.subjectId(), created.conversationId());
            analytics.track(actorId, "match_mutual", session.subjectId());
            analytics.track(session.subjectId(), "match_mutual", actorId);
            analytics.trackSurface(actorId, "discover", "mutual", session.subjectId());
            analytics.trackSurface(session.subjectId(), "discover", "mutual", actorId);
            String peerName = profiles.find(session.subjectId())
                    .map(MatchProfile::getDisplayName)
                    .orElse("your match");
            String actorName = viewer.getDisplayName() == null || viewer.getDisplayName().isBlank()
                    ? "Someone"
                    : viewer.getDisplayName();
            String opener = Icebreakers.forPair(viewer, profiles.find(session.subjectId()).orElse(null));
            try {
                if (com.itsmarsss.callerphone.bootstrap.ApplicationContext.isReady()) {
                    var inbox = com.itsmarsss.callerphone.bootstrap.ApplicationContext.get().inbox();
                    inbox.push(actorId, SocialInboxService.EntryType.CONNECTION_MESSAGE,
                            created.conversationId(), peerName, "You connected");
                    inbox.push(session.subjectId(), SocialInboxService.EntryType.CONNECTION_MESSAGE,
                            created.conversationId(), actorName, "You connected");
                }
            } catch (Exception ignored) {
            }
            return DecisionResult.mutual(
                    "You connected with **" + peerName + "**.\n_" + opener + "_",
                    created.match(),
                    created.conversationId()
            );
        }

        if (type == DecisionType.INTERESTED) {
            analytics.track(actorId, "match_interested", session.subjectId());
            analytics.trackSurface(actorId, "discover", "interest", session.subjectId());
            String actorName = viewer.getDisplayName() == null || viewer.getDisplayName().isBlank()
                    ? "Someone"
                    : viewer.getDisplayName();
            // Inbox for subject (incoming interest) — free teaser path can surface this
            if (notifications != null) {
                // notifications already handle mutual; inbox is separate
            }
            try {
                // Soft dependency: ApplicationContext may wire inbox
                if (com.itsmarsss.callerphone.bootstrap.ApplicationContext.isReady()) {
                    com.itsmarsss.callerphone.bootstrap.ApplicationContext.get().inbox().push(
                            session.subjectId(),
                            SocialInboxService.EntryType.INCOMING_INTEREST,
                            actorId,
                            actorName,
                            "Expressed interest in you"
                    );
                }
            } catch (Exception ignored) {
                // non-fatal
            }
            return DecisionResult.interested("Interest sent privately");
        }
        analytics.track(actorId, "match_skip", session.subjectId());
        analytics.trackSurface(actorId, "discover", "skip", session.subjectId());
        return DecisionResult.skipped("Next");
    }

    public DecisionResult undoLastSkip(String actorId) {
        MatchUser user = users.findById(actorId).orElse(null);
        if (user == null || user.getLastSkipSubjectId() == null || user.getLastSkipSubjectId().isBlank()) {
            return DecisionResult.fail("Nothing to undo.");
        }
        if (user.getLastSkipAt() != null
                && user.getLastSkipAt().isBefore(Instant.now().minus(Duration.ofHours(1)))) {
            return DecisionResult.fail("Too late to undo that skip.");
        }
        String today = LocalDate.now(ZoneOffset.UTC).toString();
        if (!today.equals(user.getUsageDay())) {
            user.setUsageDay(today);
            user.setUndosToday(0);
        }
        int limit = premium.dailyUndos(actorId);
        if (user.getUndosToday() >= limit) {
            return DecisionResult.fail(premium.upsellForLimit("undo"));
        }
        String subjectId = user.getLastSkipSubjectId();
        Optional<MatchDecision> existing = decisions.find(actorId, subjectId);
        if (existing.isEmpty() || existing.get().decision() != DecisionType.SKIP) {
            return DecisionResult.fail("Nothing left to undo.");
        }
        decisions.delete(actorId, subjectId);
        user.setUndosToday(user.getUndosToday() + 1);
        user.setLastSkipSubjectId(null);
        user.setLastSkipAt(null);
        users.save(user);
        analytics.track(actorId, "match_undo_skip", subjectId);
        analytics.trackSurface(actorId, "discover", "undo_skip", subjectId);
        BrowseSession session = discovery.reopenSession(actorId, subjectId);
        String name = profiles.find(subjectId).map(MatchProfile::getDisplayName).orElse("that profile");
        return DecisionResult.undone("Back to **" + name + "**. Decide again.", session);
    }

    /**
     * Express interest without a Discover browse session (bottles, call share, etc.).
     * Same mutual-interest rules as Discover — eligibility, blocks, limits, age cohort.
     */
    public DecisionResult expressInterest(String actorId, String subjectId) {
        if (actorId == null || subjectId == null || actorId.isBlank() || subjectId.isBlank()) {
            return DecisionResult.fail("Couldn't send interest.");
        }
        if (actorId.equals(subjectId)) {
            return DecisionResult.fail("That's your own bottle.");
        }
        if (safety.isMatchSuspended(actorId) || safety.isBlockedEitherWay(actorId, subjectId)) {
            return DecisionResult.fail("You can't act on this profile.");
        }
        Optional<MatchProfile> viewerOpt = profiles.find(actorId);
        if (viewerOpt.isEmpty() || viewerOpt.get().getState() != ProfileState.ACTIVE) {
            return DecisionResult.fail("Publish your Match profile first (`/match join`).");
        }
        Optional<MatchProfile> subjectOpt = profiles.find(subjectId);
        if (subjectOpt.isEmpty() || !subjectOpt.get().getState().isBrowsable()) {
            return DecisionResult.fail("They don't have a live Match profile yet.");
        }
        MatchProfile viewer = viewerOpt.get();
        MatchProfile subject = subjectOpt.get();
        if (viewer.getAgeCohort() != null && subject.getAgeCohort() != null
                && viewer.getAgeCohort() != subject.getAgeCohort()) {
            return DecisionResult.fail("Age groups must match.");
        }
        profiles.resetDailyCountersIfNeeded(viewer);
        int limit = premium.dailyInterests(actorId);
        if (viewer.getInterestSignalsToday() >= limit) {
            return DecisionResult.fail(premium.upsellForLimit("interest"));
        }
        Optional<MatchDecision> existing = decisions.find(actorId, subjectId);
        if (existing.isPresent()
                && existing.get().decision() == DecisionType.INTERESTED
                && !existing.get().isExpired(Instant.now())) {
            // still check mutual in case peer liked since
            if (decisions.findReciprocalInterest(subjectId, actorId).isPresent()) {
                return finalizeMutual(actorId, subjectId, viewer);
            }
            return DecisionResult.interested("Interest already sent privately.");
        }

        decisions.upsert(new MatchDecision(actorId, subjectId, DecisionType.INTERESTED, Instant.now(), null));
        profiles.bumpInterest(viewer);

        if (decisions.findReciprocalInterest(subjectId, actorId).isPresent()) {
            return finalizeMutual(actorId, subjectId, viewer);
        }

        analytics.track(actorId, "match_interested", subjectId);
        analytics.trackSurface(actorId, "discover", "interest", subjectId);
        String actorName = viewer.getDisplayName() == null || viewer.getDisplayName().isBlank()
                ? "Someone"
                : viewer.getDisplayName();
        try {
            if (com.itsmarsss.callerphone.bootstrap.ApplicationContext.isReady()) {
                com.itsmarsss.callerphone.bootstrap.ApplicationContext.get().inbox().push(
                        subjectId,
                        SocialInboxService.EntryType.INCOMING_INTEREST,
                        actorId,
                        actorName,
                        "Expressed interest (from a bottle)"
                );
            }
        } catch (Exception ignored) {
        }
        return DecisionResult.interested("Interest sent privately. Nothing is shared unless it's mutual.");
    }

    private DecisionResult finalizeMutual(String actorId, String subjectId, MatchProfile viewer) {
        int actorCap = premium.activeConversations(actorId);
        int peerCap = premium.activeConversations(subjectId);
        if (conversations.countActiveByUserId(actorId) >= actorCap
                || conversations.countActiveByUserId(subjectId) >= peerCap) {
            return DecisionResult.interested(premium.upsellForLimit("conversations"));
        }
        Optional<Match> existingMatch = matches.findByPairKey(Match.pairKeyFor(actorId, subjectId));
        if (existingMatch.isPresent() && existingMatch.get().getStatus() == MatchStatus.ACTIVE) {
            Optional<MatchConversation> conv = conversations.findByMatchId(existingMatch.get().getMatchId());
            if (conv.isPresent()) {
                String peerName = profiles.find(subjectId).map(MatchProfile::getDisplayName).orElse("your match");
                return DecisionResult.mutual(
                        "You're already connected with **" + peerName + "**.",
                        existingMatch.get(),
                        conv.get().getConversationId()
                );
            }
        }
        MutualCreate created = createMutualMatch(actorId, subjectId);
        notifications.notifyMutualMatch(actorId, subjectId, created.conversationId());
        analytics.track(actorId, "match_mutual", subjectId);
        analytics.track(subjectId, "match_mutual", actorId);
        analytics.trackSurface(actorId, "discover", "mutual", subjectId);
        analytics.trackSurface(subjectId, "discover", "mutual", actorId);
        String peerName = profiles.find(subjectId).map(MatchProfile::getDisplayName).orElse("your match");
        String actorName = viewer.getDisplayName() == null || viewer.getDisplayName().isBlank()
                ? "Someone"
                : viewer.getDisplayName();
        String opener = Icebreakers.forPair(viewer, profiles.find(subjectId).orElse(null));
        try {
            if (com.itsmarsss.callerphone.bootstrap.ApplicationContext.isReady()) {
                var inbox = com.itsmarsss.callerphone.bootstrap.ApplicationContext.get().inbox();
                inbox.push(actorId, SocialInboxService.EntryType.CONNECTION_MESSAGE,
                        created.conversationId(), peerName, "You connected");
                inbox.push(subjectId, SocialInboxService.EntryType.CONNECTION_MESSAGE,
                        created.conversationId(), actorName, "You connected");
            }
        } catch (Exception ignored) {
        }
        return DecisionResult.mutual(
                "You connected with **" + peerName + "**.\n_" + opener + "_",
                created.match(),
                created.conversationId()
        );
    }

    /** Public entry for adjacent features (e.g. call profile share). */
    public Optional<String> createMutualIfBothLiked(String a, String b) {
        if (decisions.findReciprocalInterest(a, b).isEmpty()
                || decisions.findReciprocalInterest(b, a).isEmpty()) {
            return Optional.empty();
        }
        if (conversations.countActiveByUserId(a) >= premium.activeConversations(a)
                || conversations.countActiveByUserId(b) >= premium.activeConversations(b)) {
            return Optional.empty();
        }
        // already have active match?
        Optional<Match> existing = matches.findByPairKey(Match.pairKeyFor(a, b));
        if (existing.isPresent() && existing.get().getStatus() == MatchStatus.ACTIVE) {
            return conversations.findByMatchId(existing.get().getMatchId()).map(MatchConversation::getConversationId);
        }
        MutualCreate created = createMutualMatch(a, b);
        notifications.notifyMutualMatch(a, b, created.conversationId());
        analytics.track(a, "match_mutual", b);
        analytics.track(b, "match_mutual", a);
        return Optional.of(created.conversationId());
    }

    private MutualCreate createMutualMatch(String a, String b) {
        Match match = new Match();
        match.setPairKey(Match.pairKeyFor(a, b));
        match.setUserIds(List.of(a, b));
        match.setStatus(MatchStatus.ACTIVE);
        match.setMatchedAt(Instant.now());
        Match saved = matches.createIfAbsent(match).orElse(match);

        Optional<MatchConversation> existing = conversations.findByMatchId(saved.getMatchId());
        if (existing.isPresent()) {
            return new MutualCreate(saved, existing.get().getConversationId());
        }
        MatchConversation conversation = new MatchConversation();
        conversation.setConversationId(UUID.randomUUID().toString());
        conversation.setMatchId(saved.getMatchId());
        conversation.setParticipants(List.of(a, b));
        conversation.setStage(ConversationStage.MEDIATED);
        conversation.setCreatedAt(Instant.now());
        conversation.setLastActivityAt(Instant.now());
        conversations.save(conversation);
        return new MutualCreate(saved, conversation.getConversationId());
    }

    private record MutualCreate(Match match, String conversationId) {
    }

    public record DecisionResult(
            boolean success,
            boolean mutual,
            String message,
            Match match,
            String conversationId,
            BrowseSession restoredSession
    ) {
        public static DecisionResult fail(String message) {
            return new DecisionResult(false, false, message, null, null, null);
        }

        public static DecisionResult interested(String message) {
            return new DecisionResult(true, false, message, null, null, null);
        }

        public static DecisionResult skipped(String message) {
            return new DecisionResult(true, false, message, null, null, null);
        }

        public static DecisionResult mutual(String message, Match match, String conversationId) {
            return new DecisionResult(true, true, message, match, conversationId, null);
        }

        public static DecisionResult undone(String message, BrowseSession session) {
            return new DecisionResult(true, false, message, null, null, session);
        }
    }
}
