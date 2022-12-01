package com.marsss.callerphone.channelpool;

import com.marsss.callerphone.Callerphone;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ChannelPoolListener extends ListenerAdapter {
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {

        final Message message = event.getMessage();
        if (message.isWebhookMessage())
            return;

        if (!event.getChannel().canTalk())
            return;

        final Member member = event.getMember();

        final String content = message.getContentRaw();

        if (member.getUser().isBot() || member.getUser().isSystem())
            return;

        if (content.startsWith("\\\\") || content.startsWith(Callerphone.Prefix)) {
            return;
        }

        String sendCont = "**%s *(%s)*** | <t:%d:f>:\n%s";

        sendCont = String.format(sendCont,
                message.getAuthor().getAsTag(),
                member.getEffectiveName(),
                message.getTimeCreated().toEpochSecond(),
                content
        );

        if (ChannelPool.isHost(event.getChannel().getId()) || ChannelPool.isChild(event.getChannel().getId())) {
            if (sendCont.length() >= 2000) {
                message.reply(Callerphone.Callerphone + "Message Too Long.").queue();
            } else {
                ChannelPool.broadCast(event.getChannel().getId(),
                        event.getChannel().getId(),
                        sendCont);
            }
        }

    }
}
