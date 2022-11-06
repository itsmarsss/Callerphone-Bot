package com.marsss.callerphone.channelpool;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ChannelPoolListener extends ListenerAdapter {
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (!event.getChannel().canTalk())
            return;

        final Member MEMBER = event.getMember();
        final Message MESSAGE = event.getMessage();

        String CONTENT = MESSAGE.getContentRaw();

        try {

            if (MEMBER.getUser().isBot() || MEMBER.getUser().isSystem())
                return;

        } catch (Exception e) {
        }

        if(CONTENT.startsWith("\\\\")){
            return;
        }

        String sendCont = "**%s**: %s *[<t:%d:R>]*";

        ChannelPool.messageOut(event.getChannel().getId(), String.format(sendCont, MESSAGE.getAuthor().getAsMention(), CONTENT, MESSAGE.getTimeCreated()));

    }
}
