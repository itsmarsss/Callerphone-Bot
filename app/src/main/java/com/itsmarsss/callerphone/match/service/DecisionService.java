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
            return DecisionResult.fail("That card expired. Browse again.");
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
            return DecisionResult.fail("Set up your profile first.");
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
            String peerName = profiles.find(session.subjectId())
                    .map(MatchProfile::getDisplayName)
                    .orElse("your match");
            String opener = Icebreakers.forPair(viewer, profiles.find(session.subjectId()).orElse(null));
            return DecisionResult.mutual(
                    "It's a match with **" + peerName + "**!\n_" + opener + "_",
                    created.match(),
                    created.conversationId()
            );
        }

        if (type == DecisionType.INTERESTED) {
            long used = viewer.getInterestSignalsToday();
            int limit = premium.dailyInterests(actorId);
            analytics.track(actorId, "match_interested", session.subjectId());
            return DecisionResult.interested("Sent (" + used + "/" + limit + " today).");
        }
        analytics.track(actorId, "match_skip", session.subjectId());
        return DecisionResult.skipped("Skipped.");
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
        BrowseSession session = discovery.reopenSession(actorId, subjectId);
        String name = profiles.find(subjectId).map(MatchProfile::getDisplayName).orElse("that profile");
        return DecisionResult.undone("Back to **" + name + "**.", session);
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
