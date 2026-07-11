package com.itsmarsss.callerphone.match.service;

import com.itsmarsss.callerphone.identity.EnrollmentService;
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

    public DecisionService(
            MatchDecisionRepository decisions,
            MatchRepository matches,
            MatchConversationRepository conversations,
            DiscoveryService discovery,
            ProfileService profiles,
            PremiumService premium,
            SafetyService safety
    ) {
        this.decisions = decisions;
        this.matches = matches;
        this.conversations = conversations;
        this.discovery = discovery;
        this.profiles = profiles;
        this.premium = premium;
        this.safety = safety;
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
            if (viewer.getInterestSignalsToday() >= premium.dailyInterests(actorId)) {
                return DecisionResult.fail("Daily interest limit reached (" + premium.dailyInterests(actorId) + ").");
            }
            if (conversations.countActiveByUserId(actorId) >= premium.activeConversations(actorId)
                    && decisions.findReciprocalInterest(session.subjectId(), actorId).isPresent()) {
                // mutual would open a conversation; enforce limit
                // still allow interested if not mutual yet
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

        boolean mutual = false;
        Match created = null;
        if (type == DecisionType.INTERESTED
                && decisions.findReciprocalInterest(session.subjectId(), actorId).isPresent()) {
            if (conversations.countActiveByUserId(actorId) >= premium.activeConversations(actorId)
                    || conversations.countActiveByUserId(session.subjectId()) >= premium.activeConversations(session.subjectId())) {
                return DecisionResult.interested("Interest saved. Conversation limit reached for a mutual connect.");
            }
            mutual = true;
            created = createMutualMatch(actorId, session.subjectId());
        }

        if (mutual && created != null) {
            return DecisionResult.mutual("It's a match! Open `/match chats` to say hi.", created);
        }
        if (type == DecisionType.INTERESTED) {
            return DecisionResult.interested("Interest sent. Keep browsing!");
        }
        return DecisionResult.skipped("Skipped. Next profile coming up.");
    }

    private Match createMutualMatch(String a, String b) {
        Match match = new Match();
        match.setPairKey(Match.pairKeyFor(a, b));
        match.setUserIds(List.of(a, b));
        match.setStatus(MatchStatus.ACTIVE);
        match.setMatchedAt(Instant.now());
        Match saved = matches.createIfAbsent(match).orElse(match);

        Optional<MatchConversation> existing = conversations.findByMatchId(saved.getMatchId());
        if (existing.isEmpty()) {
            MatchConversation conversation = new MatchConversation();
            conversation.setConversationId(UUID.randomUUID().toString());
            conversation.setMatchId(saved.getMatchId());
            conversation.setParticipants(List.of(a, b));
            conversation.setStage(ConversationStage.MEDIATED);
            conversation.setCreatedAt(Instant.now());
            conversation.setLastActivityAt(Instant.now());
            conversations.save(conversation);
        }
        return saved;
    }

    public record DecisionResult(boolean success, boolean mutual, String message, Match match) {
        public static DecisionResult fail(String message) {
            return new DecisionResult(false, false, message, null);
        }

        public static DecisionResult interested(String message) {
            return new DecisionResult(true, false, message, null);
        }

        public static DecisionResult skipped(String message) {
            return new DecisionResult(true, false, message, null);
        }

        public static DecisionResult mutual(String message, Match match) {
            return new DecisionResult(true, true, message, match);
        }
    }
}
