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
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.isFromGuild()) {
            return;
        }

        final Message MESSAGE = event.getMessage();

        if (MESSAGE.isWebhookMessage())
            return;

        if (!(ChannelPool.isHost(event.getChannel().getId()) || ChannelPool.isChild(event.getChannel().getId())))
            return;

        final Member MEMBER = event.getMember();

        if (Users.isBlacklisted(MEMBER.getUser().getId())) {
            //event.getMessage().addReaction("\u274C").queue();
            return;
        }

        if (MEMBER.getUser().isBot() || MEMBER.getUser().isSystem())
            return;

        String content = MESSAGE.getContentRaw();

        if (content.startsWith("\\\\") || content.toLowerCase().startsWith(Callerphone.config.getPrefix()))
            return;

        content = ToolSet.filterMessage(content);

        String sendCont = String.format("**%s** `%s` | <t:%d:f>\n%s",
                MESSAGE.getAuthor().getName(),
                MEMBER.getEffectiveName(),
                MESSAGE.getTimeCreated().toEpochSecond(),
                content
        );

        ChannelPool.broadCast(event.getChannel().getId(),
                event.getChannel().getId(),
                sendCont
        );

        if ((System.currentTimeMillis() - Cooldown.getPoolCooldown(event.getAuthor().getId())) > ToolSet.CREDIT_COOLDOWN) {
            Cooldown.setUserCooldown(event.getAuthor().getId());

            Users.reward(event.getAuthor().getId(), 3);
            Users.addTransmit(event.getAuthor().getId(), 1);
        }
    }
}
