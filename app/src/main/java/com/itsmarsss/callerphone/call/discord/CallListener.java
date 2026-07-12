package com.itsmarsss.callerphone.call.discord;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.call.service.CallSessionService;
import com.itsmarsss.database.categories.Cooldown;
import com.itsmarsss.database.categories.Users;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * Relays call messages for guild channels and bot DMs that are mid-call.
 */
public final class CallListener extends ListenerAdapter {
    private final CallSessionService calls = CallSessionService.get();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();
        if (message.isWebhookMessage() || message.getAuthor().isBot() || message.getAuthor().isSystem()) {
            return;
        }

        String channelId = event.getChannel().getId();
        if (!calls.isInCall(channelId)) {
            return;
        }

        boolean fromGuild = event.isFromGuild();
        boolean fromDm = event.getChannelType() == ChannelType.PRIVATE
                || event.getChannelType() == ChannelType.GROUP;
        if (!fromGuild && !fromDm) {
            return;
        }

        if (fromGuild && event.getMember() == null) {
            return;
        }

        String userId = message.getAuthor().getId();
        if (!Users.hasUser(userId)) {
            if (fromGuild) {
                ToolSet.sendPPAndTOS(event);
            } else {
                event.getChannel().sendMessage(
                        com.itsmarsss.callerphone.experience.ExperienceRenderer.toMessage(
                                com.itsmarsss.callerphone.experience.ExperienceView.builder(
                                                com.itsmarsss.callerphone.experience.ExperienceIntent.WARNING)
                                        .title("Almost ready")
                                        .description(
                                                "Accept Callerphone terms via **`/match join`** "
                                                        + "(or our support server) before calling."
                                        )
                                        .actions(com.itsmarsss.callerphone.experience.ActionSpec.success(
                                                com.itsmarsss.callerphone.match.component.MatchComponentIds.of(
                                                        com.itsmarsss.callerphone.match.component.MatchComponentIds.ACTION_JOIN_ACCEPT,
                                                        "_"
                                                ),
                                                "Create profile"
                                        ))
                                        .build()
                        )
                ).queue();
            }
            return;
        }
        if (Users.isBlacklisted(userId)) {
            return;
        }

        String raw = message.getContentDisplay();
        if (raw.startsWith("\\\\")
                || (Callerphone.config != null
                && raw.toLowerCase().startsWith(Callerphone.config.getPrefix().toLowerCase()))) {
            return;
        }

        calls.handleMessage(channelId, message.getAuthor(), raw);
        if ((System.currentTimeMillis() - Cooldown.getCreditCooldown(userId)) > ToolSet.CREDIT_COOLDOWN) {
            Cooldown.setCreditCooldown(userId);
            Users.reward(userId, 5);
            Users.addTransmit(userId, 1);
        }
    }
}
