package com.itsmarsss.callerphone.match.service;

import com.itsmarsss.callerphone.analytics.AnalyticsService;
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

    public DecisionService(
            MatchDecisionRepository decisions,
            MatchRepository matches,
            MatchConversationRepository conversations,
            DiscoveryService discovery,
            ProfileService profiles,
            PremiumService premium,
            SafetyService safety,
            NotificationService notifications,
            AnalyticsService analytics
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
    }

    public DecisionResult decide(String actorId, String sessionId, DecisionType type) {
        Optional<BrowseSession> sessionOpt = discovery.session(sessionId);
        if (sessionOpt.isEmpty() || sessionOpt.get().isExpired(Instant.now())) {
            return DecisionResult.fail("This card expired. Use `/match browse` for a new profile.");
        }
        BrowseSession session = sessionOpt.get();
        if (!session.viewerId().equals(actorId)) {
            return DecisionResult.fail("This card is not yours.");
        }
        if (safety.isMatchSuspended(actorId) || safety.isBlockedEitherWay(actorId, session.subjectId())) {
            discovery.clearSession(sessionId);
            return DecisionResult.fail("You cannot act on this profile.");
        }

        Optional<MatchProfile> viewerOpt = profiles.find(actorId);
        if (viewerOpt.isEmpty()) {
            return DecisionResult.fail("Create a profile first.");
        }
        MatchProfile viewer = viewerOpt.get();
        profiles.resetDailyCountersIfNeeded(viewer);

        if (type == DecisionType.INTERESTED) {
            int limit = premium.dailyInterests(actorId);
            if (viewer.getInterestSignalsToday() >= limit) {
                return DecisionResult.fail(
                        "Daily interest limit reached (" + limit + "). Come back tomorrow — higher limits ship with Premium later.");
            }
        }

        Instant expiresAt = type == DecisionType.SKIP
                ? Instant.now().plus(Duration.ofDays(MatchLimits.SKIP_EXPIRE_DAYS))
                : null;
        decisions.upsert(new MatchDecision(actorId, session.subjectId(), type, Instant.now(), expiresAt));
        if (type == DecisionType.INTERESTED) {
            profiles.bumpInterest(viewer);
        }
        discovery.clearSession(sessionId);

        if (type == DecisionType.INTERESTED
                && decisions.findReciprocalInterest(session.subjectId(), actorId).isPresent()) {
            int actorCap = premium.activeConversations(actorId);
            int peerCap = premium.activeConversations(session.subjectId());
            if (conversations.countActiveByUserId(actorId) >= actorCap
                    || conversations.countActiveByUserId(session.subjectId()) >= peerCap) {
                return DecisionResult.interested(
                        "Interest saved — it's mutual, but an active-conversation limit was hit. "
                                + "Unmatch an old chat or wait; Premium will raise this limit later.");
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
                    "It's a match with **" + peerName + "**! Suggested opener: _" + opener
                            + "_ — open `/match chats` or the Open chat button in your DMs.",
                    created.match(),
                    created.conversationId()
            );
        }

        if (type == DecisionType.INTERESTED) {
            long used = viewer.getInterestSignalsToday();
            int limit = premium.dailyInterests(actorId);
            analytics.track(actorId, "match_interested", session.subjectId());
            return DecisionResult.interested("Interest sent (" + used + "/" + limit + " today). Keep browsing!");
        }
        analytics.track(actorId, "match_skip", session.subjectId());
        return DecisionResult.skipped("Skipped. Next profile coming up.");
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

    public record DecisionResult(boolean success, boolean mutual, String message, Match match, String conversationId) {
        public static DecisionResult fail(String message) {
            return new DecisionResult(false, false, message, null, null);
        }

        public static DecisionResult interested(String message) {
            return new DecisionResult(true, false, message, null, null);
        }

        public static DecisionResult skipped(String message) {
            return new DecisionResult(true, false, message, null, null);
        }

        public static DecisionResult mutual(String message, Match match, String conversationId) {
            return new DecisionResult(true, true, message, match, conversationId);
        }
    }
}
