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
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

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
            return ShareResult.fail("Match isn't ready yet.");
        }
        Optional<MatchProfile> profile = ApplicationContext.get().profiles().find(sharerUserId);
        if (profile.isEmpty() || profile.get().getState() != ProfileState.ACTIVE) {
            return ShareResult.fail("Go live first with `/match join`.");
        }
        MessageChannel other = ToolSet.getMessageChannel(session.otherChannelId(fromChannelId));
        if (other == null) {
            return ShareResult.fail("Couldn't reach the other side.");
        }
        session.markProfileShared(sharerUserId);
        MatchProfile p = profile.get();
        other.sendMessageEmbeds(
                        MatchEmbeds.soft("Profile shared", "Someone wants you to meet them."),
                        MatchEmbeds.profileCard(p, false)
                )
                .setComponents(ActionRow.of(
                        Button.success(CallComponentIds.like(sessionId, sharerUserId), "Interested"),
                        Button.secondary(CallComponentIds.pass(sessionId, sharerUserId), "Not now")
                ))
                .queue();
        try {
            ApplicationContext.get().analytics().track(sharerUserId, "call_profile_share", sessionId);
        } catch (Exception ignored) {
        }
        return ShareResult.ok("Your profile was sent to the other side.");
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
            return ShareResult.fail("Match isn't ready yet.");
        }
        if (actorUserId.equals(subjectUserId)) {
            return ShareResult.fail("That's your own profile.");
        }
        Optional<MatchProfile> actorProfile = ApplicationContext.get().profiles().find(actorUserId);
        Optional<MatchProfile> subjectProfile = ApplicationContext.get().profiles().find(subjectUserId);
        if (actorProfile.isEmpty() || actorProfile.get().getState() != ProfileState.ACTIVE) {
            return ShareResult.fail("Go live first with `/match join`.");
        }
        if (subjectProfile.isEmpty()) {
            return ShareResult.fail("That profile is gone.");
        }
        AgeCohort a = actorProfile.get().getAgeCohort();
        AgeCohort b = subjectProfile.get().getAgeCohort();
        if (a == null || b == null || a != b) {
            return ShareResult.fail("You're in different age groups.");
        }
        if (ApplicationContext.get().safety().isBlockedEitherWay(actorUserId, subjectUserId)) {
            return ShareResult.fail("You can't interact with this profile.");
        }
        if (!interested) {
            return ShareResult.ok("Passed.");
        }
        if (decisions != null) {
            decisions.upsert(new MatchDecision(actorUserId, subjectUserId, DecisionType.INTERESTED, Instant.now(), null));
            Optional<MatchDecision> back = decisions.findReciprocalInterest(subjectUserId, actorUserId);
            if (back.isPresent()) {
                Optional<String> conversationId = ApplicationContext.get().decisions()
                        .createMutualIfBothLiked(actorUserId, subjectUserId);
                String opener = Icebreakers.forPair(actorProfile.get(), subjectProfile.get());
                MessageChannel other = ToolSet.getMessageChannel(opt.get().otherChannelId(actorChannelId));
                if (other != null) {
                    other.sendMessage(com.itsmarsss.callerphone.experience.ExperienceRenderer.toMessage(
                            com.itsmarsss.callerphone.experience.ExperienceView.builder(
                                            com.itsmarsss.callerphone.experience.ExperienceIntent.SOCIAL)
                                    .title("You connected")
                                    .description("You're both interested.\n\n_" + opener + "_")
                                    .actions(
                                            conversationId.isPresent()
                                                    ? com.itsmarsss.callerphone.experience.ActionSpec.success(
                                                    com.itsmarsss.callerphone.match.component.MatchComponentIds.of(
                                                            com.itsmarsss.callerphone.match.component.MatchComponentIds.ACTION_CHAT_SELECT,
                                                            conversationId.get()
                                                    ),
                                                    "Open Match chat"
                                            )
                                                    : com.itsmarsss.callerphone.experience.ActionSpec.success(
                                                    CallComponentIds.prompt(sessionId),
                                                    "Keep talking"
                                            )
                                    )
                                    .build()
                    )).queue();
                }
                try {
                    ApplicationContext.get().analytics().track(actorUserId, "call_share_to_connection",
                            conversationId.orElse("pending"));
                } catch (Exception ignored) {
                }
                return ShareResult.ok(conversationId.isPresent()
                        ? "You connected.\n_" + opener + "_"
                        : "You're both interested. Chat limit may be full for now.");
            }
        }
        try {
            ApplicationContext.get().analytics().track(actorUserId, "call_share_interest", subjectUserId);
        } catch (Exception ignored) {
        }
        MessageChannel other = ToolSet.getMessageChannel(opt.get().otherChannelId(actorChannelId));
        if (other != null) {
            other.sendMessage(com.itsmarsss.callerphone.experience.ExperienceRenderer.toMessage(
                    com.itsmarsss.callerphone.experience.ExperienceView.builder(
                                    com.itsmarsss.callerphone.experience.ExperienceIntent.SOCIAL)
                            .title("Interest received")
                            .description("Someone is interested in the shared profile.")
                            .build()
            )).queue();
        }
        return ShareResult.ok("Interest sent privately.");
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
