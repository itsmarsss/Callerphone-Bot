package com.itsmarsss.callerphone.tccallerphone;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.tccallerphone.services.ConversationService;
import com.itsmarsss.database.categories.Cooldown;
import com.itsmarsss.database.categories.Users;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class TCCallerphoneListener extends ListenerAdapter {
    private final ConversationService conversationService = ConversationService.getInstance();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.isFromGuild()) {
            return;
        }

        final Message message = event.getMessage();
        if (message.isWebhookMessage() || message.getAuthor().isBot() || message.getAuthor().isSystem()) {
            return;
        }

        final String channelId = event.getChannel().getId();
        if (!conversationService.isInConversation(channelId)) {
            return;
        }

        final Member member = event.getMember();
        if (member == null) {
            return;
        }

        if (!Users.hasUser(member.getId())) {
            ToolSet.sendPPAndTOS(event);
            return;
        }

        if (Users.isBlacklisted(member.getId())) {
            return;
        }

        String messageRaw = message.getContentDisplay();
        if (messageRaw.startsWith("\\\\") || messageRaw.toLowerCase().startsWith(Callerphone.config.getPrefix())) {
            return;
        }

        conversationService.handleMessage(channelId, message.getAuthor(), messageRaw);

        if ((System.currentTimeMillis() - Cooldown.getPoolCooldown(event.getAuthor().getId())) > ToolSet.CREDIT_COOLDOWN) {
            Cooldown.setUserCooldown(event.getAuthor().getId());
            Users.reward(event.getAuthor().getId(), 5);
            Users.addTransmit(event.getAuthor().getId(), 1);
        }
    }
}
