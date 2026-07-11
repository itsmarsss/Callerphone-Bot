package com.itsmarsss.callerphone.call.service;

import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.bootstrap.ApplicationContext;
import com.itsmarsss.callerphone.call.discord.CallComponentIds;
import com.itsmarsss.callerphone.call.model.CallSession;
import com.itsmarsss.callerphone.discord.match.MatchEmbeds;
import com.itsmarsss.callerphone.identity.AgeCohort;
import com.itsmarsss.callerphone.match.model.DecisionType;
import com.itsmarsss.callerphone.match.model.MatchDecision;
import com.itsmarsss.callerphone.match.model.MatchProfile;
import com.itsmarsss.callerphone.match.model.ProfileState;
import com.itsmarsss.callerphone.match.repository.MatchDecisionRepository;
import com.itsmarsss.callerphone.match.service.Icebreakers;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.time.Instant;
import java.util.Optional;

/**
 * Adjacent to Match: during a live call, either side can share their Social profile.
 * The other may express interest (same decision store / age isolation as `/match`).
 */
public final class CallProfileShareService {
    private final CallSessionService sessions;
    private MatchDecisionRepository decisions;

    public CallProfileShareService(CallSessionService sessions) {
        this.sessions = sessions;
    }

    public void setDecisions(MatchDecisionRepository decisions) {
        this.decisions = decisions;
    }

    public ShareResult share(String sessionId, String sharerUserId, String fromChannelId) {
        Optional<CallSession> opt = sessions.getBySessionId(sessionId);
        if (opt.isEmpty() || !opt.get().ownsChannel(fromChannelId)) {
            return ShareResult.fail("No active call.");
        }
        CallSession session = opt.get();
        if (!ApplicationContext.isReady()) {
            return ShareResult.fail("Match is not ready.");
        }
        Optional<MatchProfile> profile = ApplicationContext.get().profiles().find(sharerUserId);
        if (profile.isEmpty() || profile.get().getState() != ProfileState.ACTIVE) {
            return ShareResult.fail("You need a **live** Match profile first (`/match submit`).");
        }
        TextChannel other = ToolSet.getTextChannel(session.otherChannelId(fromChannelId));
        if (other == null) {
            return ShareResult.fail("Other channel unreachable.");
        }
        session.markProfileShared(sharerUserId);
        MatchProfile p = profile.get();
        other.sendMessage(ToolSet.CP_EMJ + " Someone in the call shared a **Match profile** "
                        + "(age group " + (p.getAgeCohort() == null ? "?" : p.getAgeCohort().label()) + "). "
                        + "Interested only works if you're in the **same** age group and have a live profile.")
                .addEmbeds(MatchEmbeds.profileCard(p, false))
                .setComponents(ActionRow.of(
                        Button.success(CallComponentIds.like(sessionId, sharerUserId), "Interested"),
                        Button.secondary(CallComponentIds.pass(sessionId, sharerUserId), "Not now")
                ))
                .queue();
        return ShareResult.ok("Profile shared to the other side.");
    }

    public ShareResult react(
            String sessionId,
            String actorUserId,
            String subjectUserId,
            boolean interested,
            String actorChannelId
    ) {
        Optional<CallSession> opt = sessions.getBySessionId(sessionId);
        if (opt.isEmpty() || !opt.get().ownsChannel(actorChannelId)) {
            return ShareResult.fail("Call not found.");
        }
        if (!ApplicationContext.isReady()) {
            return ShareResult.fail("Match is not ready.");
        }
        if (actorUserId.equals(subjectUserId)) {
            return ShareResult.fail("That's your own profile.");
        }
        Optional<MatchProfile> actorProfile = ApplicationContext.get().profiles().find(actorUserId);
        Optional<MatchProfile> subjectProfile = ApplicationContext.get().profiles().find(subjectUserId);
        if (actorProfile.isEmpty() || actorProfile.get().getState() != ProfileState.ACTIVE) {
            return ShareResult.fail("You need a live Match profile to express interest (`/match join` → submit).");
        }
        if (subjectProfile.isEmpty()) {
            return ShareResult.fail("Their Match profile is gone.");
        }
        AgeCohort a = actorProfile.get().getAgeCohort();
        AgeCohort b = subjectProfile.get().getAgeCohort();
        if (a == null || b == null || a != b) {
            return ShareResult.fail("Age groups don't match — likes only work within the same group.");
        }
        if (ApplicationContext.get().safety().isBlockedEitherWay(actorUserId, subjectUserId)) {
            return ShareResult.fail("You can't interact with this profile.");
        }
        if (!interested) {
            return ShareResult.ok("Passed. Keep chatting or share your own profile.");
        }
        if (decisions != null) {
            decisions.upsert(new MatchDecision(actorUserId, subjectUserId, DecisionType.INTERESTED, Instant.now(), null));
            // mutual?
            Optional<MatchDecision> back = decisions.findReciprocalInterest(subjectUserId, actorUserId);
            if (back.isPresent()) {
                Optional<String> conversationId = ApplicationContext.get().decisions()
                        .createMutualIfBothLiked(actorUserId, subjectUserId);
                String opener = Icebreakers.forPair(actorProfile.get(), subjectProfile.get());
                TextChannel other = ToolSet.getTextChannel(opt.get().otherChannelId(actorChannelId));
                String note = ToolSet.CP_EMJ + " **Mutual Match!** You both liked each other. "
                        + "Open `/match chats`. Suggested opener: _" + opener + "_";
                if (other != null) {
                    other.sendMessage(note).queue();
                }
                return ShareResult.ok(conversationId.isPresent()
                        ? "It's a mutual match! Check `/match chats`. Opener: _" + opener + "_"
                        : "Mutual interest recorded (chat limit may apply). Opener: _" + opener + "_");
            }
        }
        TextChannel other = ToolSet.getTextChannel(opt.get().otherChannelId(actorChannelId));
        if (other != null) {
            other.sendMessage(ToolSet.CP_EMJ + " Someone in the call liked a shared Match profile.").queue();
        }
        return ShareResult.ok("Interest saved. If they like you back, you'll get a Match connection.");
    }

    public record ShareResult(boolean success, String message) {
        public static ShareResult ok(String m) {
            return new ShareResult(true, m);
        }

        public static ShareResult fail(String m) {
            return new ShareResult(false, m);
        }
    }
}
