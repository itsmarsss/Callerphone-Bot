package com.itsmarsss.callerphone.match.service;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.experience.ActionSpec;
import com.itsmarsss.callerphone.experience.ExperienceIntent;
import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.callerphone.experience.ExperienceView;
import com.itsmarsss.callerphone.identity.MatchUserRepository;
import com.itsmarsss.callerphone.match.component.MatchComponentIds;
import com.itsmarsss.callerphone.match.model.MatchProfile;
import com.itsmarsss.callerphone.match.repository.MatchProfileRepository;
import com.itsmarsss.callerphone.safety.Report;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.util.Optional;

/**
 * Opt-in Match DMs and staff channel alerts. Never spam; only real events.
 */
public final class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final MatchUserRepository users;
    private final MatchProfileRepository profiles;

    public NotificationService(MatchUserRepository users, MatchProfileRepository profiles) {
        this.users = users;
        this.profiles = profiles;
    }

    public void notifyMutualMatch(String userA, String userB, String conversationId) {
        String nameA = displayName(userA);
        String nameB = displayName(userB);
        MatchProfile profileA = profiles.findByUserId(userA).orElse(null);
        MatchProfile profileB = profiles.findByUserId(userB).orElse(null);
        String openerA = Icebreakers.forPair(profileA, profileB);
        String openerB = Icebreakers.forPair(profileB, profileA);
        // DecisionService also writes connection inbox rows for mutual; keep DM delivery here
        dmIfEnabled(userA, "You connected",
                "You and **" + nameB + "** are both interested.\n_" + openerA + "_",
                conversationId);
        dmIfEnabled(userB, "You connected",
                "You and **" + nameA + "** are both interested.\n_" + openerB + "_",
                conversationId);
    }

    public void notifyProfileLive(String userId) {
        pushInbox(userId, SocialInboxService.EntryType.PROFILE_SHARE_RESPONSE,
                userId, "Match", "You're live in Discover");
        dmIfEnabled(userId, "You're live", "Your profile can now appear in Discover.", null);
    }

    public void notifyProfileRestricted(String userId, String reason) {
        String detail = reason == null || reason.isBlank() ? "A moderator paused your profile." : reason;
        pushInbox(userId, SocialInboxService.EntryType.SAFETY_UPDATE,
                userId, "Safety", detail);
        dmIfEnabled(userId, "Profile paused", detail, null);
    }

    public void notifyConnectRequest(String recipientId, String requesterId, String conversationId) {
        String name = displayName(requesterId);
        pushInbox(recipientId, SocialInboxService.EntryType.CONNECTION_MESSAGE,
                conversationId, name, "Wants to connect (48h)");
        dmWithConnectButtons(recipientId, "Connect request",
                "**" + name + "** wants to connect. Expires in 48 hours.",
                conversationId);
    }

    public void notifyConnectAccepted(String requesterId, String acceptorId) {
        notifyConnectAccepted(requesterId, acceptorId, null);
    }

    public void notifyConnectAccepted(String requesterId, String acceptorId, String conversationId) {
        String source = conversationId != null && !conversationId.isBlank() ? conversationId : acceptorId;
        pushInbox(requesterId, SocialInboxService.EntryType.CONNECTION_MESSAGE,
                source, displayName(acceptorId), "Accepted your connect request");
        pushInbox(acceptorId, SocialInboxService.EntryType.CONNECTION_MESSAGE,
                source, displayName(requesterId), "You're connected");
        dmIfEnabled(requesterId, "Connected",
                "**" + displayName(acceptorId) + "** accepted. <@" + acceptorId + ">",
                conversationId);
        dmIfEnabled(acceptorId, "Connected",
                "You're connected with **" + displayName(requesterId) + "**. <@" + requesterId + ">",
                conversationId);
    }

    public void notifyConnectDeclined(String requesterId) {
        notifyConnectDeclined(requesterId, null);
    }

    public void notifyConnectDeclined(String requesterId, String conversationId) {
        String source = conversationId != null && !conversationId.isBlank() ? conversationId : requesterId;
        pushInbox(requesterId, SocialInboxService.EntryType.CONNECTION_MESSAGE,
                source, "Match", "Connect request declined");
        dmIfEnabled(requesterId, "Request declined", "You can keep chatting here if you want.", conversationId);
    }

    public void notifyUnmatched(String recipientId, String actorId) {
        notifyUnmatched(recipientId, actorId, null);
    }

    public void notifyUnmatched(String recipientId, String actorId, String conversationId) {
        String source = conversationId != null && !conversationId.isBlank()
                ? conversationId
                : (actorId == null ? "system" : actorId);
        pushInbox(recipientId, SocialInboxService.EntryType.CONNECTION_MESSAGE,
                source, "Someone", "That connection was closed.");
        dmIfEnabled(recipientId, "Chat ended", "That connection was closed.", null);
    }

    public void notifyInactivityNudge(String userId, String peerName, String conversationId, String opener) {
        pushInbox(userId, SocialInboxService.EntryType.CONNECTION_MESSAGE,
                conversationId, peerName, "Still waiting for you");
        dmIfEnabled(userId, "Still there?",
                "**" + peerName + "** is waiting.\n_" + opener + "_",
                conversationId);
        try {
            if (com.itsmarsss.callerphone.bootstrap.ApplicationContext.isReady()) {
                com.itsmarsss.callerphone.bootstrap.ApplicationContext.get().analytics()
                        .track(userId, "match_inactivity_nudge", conversationId);
            }
        } catch (Exception e) {
            logger.debug("nudge analytics failed: {}", e.getMessage());
        }
    }

    public void notifyWeeklyDigest(String userId, String body) {
        dmIfEnabled(userId, "Your week on Match", body, null);
    }

    public void postReportToStaff(Report report) {
        if (Callerphone.config == null) {
            return;
        }
        TextChannel channel = ToolSet.getTextChannel(Callerphone.config.getReportChatChannel());
        if (channel == null) {
            logger.debug("No report channel for Match report {}", report.getId());
            return;
        }
        StringBuilder evidence = new StringBuilder();
        if (report.getEvidence() != null) {
            int n = 0;
            for (String line : report.getEvidence()) {
                if (n++ >= 8) {
                    evidence.append("…\n");
                    break;
                }
                evidence.append(line.length() > 120 ? line.substring(0, 117) + "…" : line).append('\n');
            }
        }
        EmbedBuilder emb = new EmbedBuilder()
                .setTitle(report.getPriority() >= 10 ? "URGENT Match report" : "Match report")
                .setColor(report.getPriority() >= 10 ? Color.RED : new Color(220, 120, 40))
                .addField("Report ID", "`" + report.getId() + "`", true)
                .addField("Category", nullSafe(report.getCategory()), true)
                .addField("Priority", String.valueOf(report.getPriority()), true)
                .addField("Reporter", "`" + report.getReporterId() + "`", true)
                .addField("Subject", "`" + report.getSubjectId() + "`", true)
                .addField("Auto-paused", report.isAutoPaused() ? "yes" : "no", true)
                .addField("Target", nullSafe(report.getTargetType()) + " `" + nullSafe(report.getTargetId()) + "`", false)
                .addField("Details", nullSafe(report.getDetails()).isBlank() ? "_none_" : report.getDetails(), false)
                .addField("Evidence", evidence.isEmpty() ? "_none captured_" : evidence.toString(), false)
                .setFooter("Staff: mreports / mresolve / mpause / msuspend");
        channel.sendMessageEmbeds(emb.build()).queue(
                ok -> {
                },
                err -> logger.warn("Failed to post Match report: {}", err.getMessage())
        );
    }



    private void dmIfEnabled(String userId, String title, String body, String conversationId) {
        if (!notificationsOn(userId)) {
            return;
        }
        ShardManager sm = Callerphone.sdMgr;
        if (sm == null) {
            return;
        }
        sm.retrieveUserById(userId).queue(user -> {
            ExperienceView.Builder vb = ExperienceView.builder(ExperienceIntent.SOCIAL)
                    .title(title)
                    .description(body);
            if (conversationId != null && !conversationId.isBlank()) {
                vb.actions(ActionSpec.primary(
                        MatchComponentIds.of(MatchComponentIds.ACTION_CHAT_SELECT, conversationId),
                        "Open chat"
                ));
            }
            user.openPrivateChannel()
                    .flatMap(ch -> ch.sendMessage(ExperienceRenderer.toMessage(vb.build())))
                    .queue(ok -> {
                    }, err -> logger.debug("Match DM failed for {}: {}", userId, err.getMessage()));
        }, err -> logger.debug("Match DM user missing {}", userId));
    }

    private void dmWithConnectButtons(String userId, String title, String body, String conversationId) {
        if (!notificationsOn(userId)) {
            return;
        }
        ShardManager sm = Callerphone.sdMgr;
        if (sm == null) {
            return;
        }
        sm.retrieveUserById(userId).queue(user -> user.openPrivateChannel().queue(ch ->
                ch.sendMessage(ExperienceRenderer.toMessage(
                        ExperienceView.builder(ExperienceIntent.SOCIAL)
                                .title(title)
                                .description(body)
                                .actions(
                                        ActionSpec.success(
                                                MatchComponentIds.of(
                                                        MatchComponentIds.ACTION_CONNECT_ACCEPT,
                                                        conversationId
                                                ),
                                                "Accept connect"
                                        ),
                                        ActionSpec.danger(
                                                MatchComponentIds.of(
                                                        MatchComponentIds.ACTION_CONNECT_DECLINE,
                                                        conversationId
                                                ),
                                                "Decline"
                                        ),
                                        ActionSpec.primary(
                                                MatchComponentIds.of(
                                                        MatchComponentIds.ACTION_CHAT_SELECT,
                                                        conversationId
                                                ),
                                                "Open chat"
                                        )
                                )
                                .build()
                )).queue(ok -> {
                }, err -> logger.debug("Connect DM failed for {}", userId))
        ));
    }

    private boolean notificationsOn(String userId) {
        return users.findById(userId)
                .map(u -> u.isNotificationsEnabled())
                .orElse(true);
    }

    private String displayName(String userId) {
        Optional<MatchProfile> profile = profiles.findByUserId(userId);
        return profile.map(MatchProfile::getDisplayName)
                .filter(n -> n != null && !n.isBlank())
                .orElse("someone");
    }

    private static void pushInbox(
            String userId,
            SocialInboxService.EntryType type,
            String sourceId,
            String actorDisplay,
            String preview
    ) {
        try {
            if (com.itsmarsss.callerphone.bootstrap.ApplicationContext.isReady()) {
                com.itsmarsss.callerphone.bootstrap.ApplicationContext.get().inbox()
                        .push(userId, type, sourceId, actorDisplay, preview);
            }
        } catch (Exception e) {
            logger.debug("Inbox push failed: {}", e.getMessage());
        }
    }

    private static String nullSafe(String s) {
        return s == null ? "" : s;
    }
}
