package com.itsmarsss.callerphone.channelpool;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.database.categories.Cooldown;
import com.itsmarsss.database.categories.Users;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ChannelPoolListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.isFromGuild()) {
            return;
        }

        final Message message = event.getMessage();
        if (message.isWebhookMessage()) {
            return;
        }

        final String channelId = event.getChannel().getId();
        if (!ChannelPool.isInPool(channelId)) {
            return;
        }

        final Member member = event.getMember();
        if (member == null || member.getUser().isBot() || member.getUser().isSystem()) {
            return;
        }

        if (Users.isBlacklisted(member.getId())) {
            return;
        }

        String content = message.getContentRaw();
        if (content.startsWith("\\\\") || content.toLowerCase().startsWith(Callerphone.config.getPrefix())) {
            return;
        }

        content = ToolSet.filterMessage(content);
        String payload = String.format("**%s** `%s` | <t:%d:f>\n%s",
                message.getAuthor().getName(),
                member.getEffectiveName(),
                message.getTimeCreated().toEpochSecond(),
                content
        );

        ChannelPool.broadCast(channelId, channelId, payload);

        if ((System.currentTimeMillis() - Cooldown.getPoolCooldown(event.getAuthor().getId())) > ToolSet.CREDIT_COOLDOWN) {
            Cooldown.setUserCooldown(event.getAuthor().getId());
            Users.reward(event.getAuthor().getId(), 3);
            Users.addTransmit(event.getAuthor().getId(), 1);
        }
    }
}
