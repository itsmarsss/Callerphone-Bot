package com.itsmarsss.callerphone.discord.match;

import com.itsmarsss.callerphone.bootstrap.ApplicationContext;
import com.itsmarsss.callerphone.call.service.CallSessionService;
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
        // Active DM calls own the private channel; do not relay as Match chat
        if (CallSessionService.get().isInCall(event.getChannel().getId())) {
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
                event.getMessage().reply(com.itsmarsss.callerphone.experience.ExperienceRenderer.toMessage(
                        MatchPresenter.serviceFailed("Chat is text-only for now. Send a short message without attachments.")
                )).queue();
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
                event.getMessage().reply(com.itsmarsss.callerphone.experience.ExperienceRenderer.toMessage(
                        MatchPresenter.serviceFailed(result.message())
                )).queue();
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
                                    err -> event.getMessage().reply(
                                            com.itsmarsss.callerphone.experience.ExperienceRenderer.toMessage(
                                                    MatchPresenter.serviceFailed(
                                                            "Delivery failed — they may have DMs closed."
                                                    )
                                            )
                                    ).queue()
                            );
                }, err -> event.getMessage().reply(
                        com.itsmarsss.callerphone.experience.ExperienceRenderer.toMessage(
                                MatchPresenter.serviceFailed("Couldn't open their DMs.")
                        )
                ).queue());
            }, err -> event.getMessage().reply(
                    com.itsmarsss.callerphone.experience.ExperienceRenderer.toMessage(
                            MatchPresenter.serviceFailed("Recipient not found.")
                    )
            ).queue());
        });
    }
}
