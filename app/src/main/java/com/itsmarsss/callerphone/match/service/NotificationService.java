package com.itsmarsss.callerphone.match.service;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.discord.match.MatchEmbeds;
import com.itsmarsss.callerphone.identity.MatchUserRepository;
import com.itsmarsss.callerphone.match.component.MatchComponentIds;
import com.itsmarsss.callerphone.match.model.MatchProfile;
import com.itsmarsss.callerphone.match.repository.MatchProfileRepository;
import com.itsmarsss.callerphone.safety.Report;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
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
        dmIfEnabled(userA, "It's a match!",
                "You and **" + nameB + "** both liked each other.\n_"
                        + Icebreakers.forPair(profileA, profileB) + "_",
                conversationId);
        dmIfEnabled(userB, "It's a match!",
                "You and **" + nameA + "** both liked each other.\n_"
                        + Icebreakers.forPair(profileB, profileA) + "_",
                conversationId);
    }

    public void notifyProfileLive(String userId) {
        dmIfEnabled(userId, "You're live", "People in your age group can find you now.", null);
    }

    public void notifyProfileRestricted(String userId, String reason) {
        String detail = reason == null || reason.isBlank() ? "A moderator paused your profile." : reason;
        dmIfEnabled(userId, "Profile paused", detail, null);
    }

    public void notifyConnectRequest(String recipientId, String requesterId, String conversationId) {
        String name = displayName(requesterId);
        dmWithConnectButtons(recipientId, "Connect request",
                "**" + name + "** wants to connect. Expires in 48 hours.",
                conversationId);
    }

    public void notifyConnectAccepted(String requesterId, String acceptorId) {
        dmIfEnabled(requesterId, "Connected",
                "**" + displayName(acceptorId) + "** accepted. <@" + acceptorId + ">",
                null);
        dmIfEnabled(acceptorId, "Connected",
                "You're connected with **" + displayName(requesterId) + "**. <@" + requesterId + ">",
                null);
    }

    public void notifyConnectDeclined(String requesterId) {
        dmIfEnabled(requesterId, "Request declined", "You can keep chatting here if you want.", null);
    }

    public void notifyUnmatched(String recipientId, String actorId) {
        dmIfEnabled(recipientId, "Chat ended", "That connection was closed.", null);
    }

    public void notifyInactivityNudge(String userId, String peerName, String conversationId, String opener) {
        dmIfEnabled(userId, "Still there?",
                "**" + peerName + "** is waiting.\n_" + opener + "_",
                conversationId);
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
            var msg = user.openPrivateChannel()
                    .flatMap(ch -> ch.sendMessageEmbeds(MatchEmbeds.simple(title, body)));
            if (conversationId != null && !conversationId.isBlank()) {
                msg = user.openPrivateChannel().flatMap(ch -> ch.sendMessageEmbeds(MatchEmbeds.simple(title, body))
                        .setComponents(ActionRow.of(
                                Button.primary(
                                        MatchComponentIds.of(MatchComponentIds.ACTION_CHAT_SELECT, conversationId),
                                        "Open chat"
                                )
                        )));
            }
            msg.queue(ok -> {
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
                ch.sendMessageEmbeds(MatchEmbeds.simple(title, body))
                        .setComponents(ActionRow.of(
                                Button.success(
                                        MatchComponentIds.of(MatchComponentIds.ACTION_CONNECT_ACCEPT, conversationId),
                                        "Accept connect"
                                ),
                                Button.danger(
                                        MatchComponentIds.of(MatchComponentIds.ACTION_CONNECT_DECLINE, conversationId),
                                        "Decline"
                                ),
                                Button.primary(
                                        MatchComponentIds.of(MatchComponentIds.ACTION_CHAT_SELECT, conversationId),
                                        "Open chat"
                                )
                        ))
                        .queue(ok -> {
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

    private static String nullSafe(String s) {
        return s == null ? "" : s;
    }
}
