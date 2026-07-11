package com.itsmarsss.callerphone.discord.match;

import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.bootstrap.ApplicationContext;
import com.itsmarsss.callerphone.match.service.MatchConversationService;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * Relays Match DMs only when the user has an explicit selected conversation.
 * Ignores bots/system and never treats all DMs as Social traffic.
 */
public final class MatchDmListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.isFromGuild() || event.getAuthor().isBot() || event.getAuthor().isSystem()) {
            return;
        }
        if (event.getChannelType() != ChannelType.PRIVATE) {
            return;
        }
        if (!ApplicationContext.isReady()) {
            return;
        }
        if (!event.getMessage().getAttachments().isEmpty()
                || !event.getMessage().getStickers().isEmpty()
                || !event.getMessage().getEmbeds().isEmpty()) {
            // Only reject if they have an active chat context
            ApplicationContext ctx = ApplicationContext.get();
            var user = ctx.enrollment().getOrCreate(event.getAuthor().getId());
            if (user.getSelectedConversationId() != null) {
                event.getMessage().reply(ToolSet.CP_EMJ + " Mediated Match chat is text-only for now.").queue();
            }
            return;
        }

        String content = event.getMessage().getContentRaw();
        if (content == null || content.isBlank()) {
            return;
        }
        // Leave admin prefix commands to the mod router
        // Prefix check is cheap; mod listener still owns staff commands.

        ApplicationContext ctx = ApplicationContext.get();
        ctx.dbExecutor().execute(() -> {
            MatchConversationService.RelayResult result = ctx.conversations().relayDm(
                    event.getAuthor().getId(),
                    content,
                    event.getMessageId()
            );
            if (!result.handled()) {
                return;
            }
            if (!result.success()) {
                event.getMessage().reply(ToolSet.CP_EMJ + " " + result.message()).queue();
                return;
            }
            event.getJDA().retrieveUserById(result.recipientId()).queue(recipient -> {
                recipient.openPrivateChannel().queue(channel -> {
                    String body = "**" + result.senderDisplay() + "**\n" + result.content();
                    channel.sendMessage(body)
                            .queue(
                                    ok -> {
                                        event.getMessage().addReaction(
                                                net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode("✅")).queue();
                                        if (ApplicationContext.isReady()) {
                                            ApplicationContext.get().analytics()
                                                    .track(event.getAuthor().getId(), "match_message_relayed",
                                                            result.conversationId());
                                        }
                                    },
                                    err -> event.getMessage().reply(ToolSet.CP_EMJ
                                            + " Delivery failed. They may have DMs closed.").queue()
                            );
                }, err -> event.getMessage().reply(ToolSet.CP_EMJ + " Could not open recipient DM.").queue());
            }, err -> event.getMessage().reply(ToolSet.CP_EMJ + " Recipient not found.").queue());
        });
    }
}
