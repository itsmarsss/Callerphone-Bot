package com.marsss.callerphone.channelpool;

import com.marsss.callerphone.Callerphone;
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

        if (CONTENT.startsWith("\\\\")) {
            return;
        }

        String sendCont = "**%s *(%s)*:** %s •[<t:%d:f>]•";

        sendCont = String.format(sendCont,
                MESSAGE.getAuthor().getAsTag(),
                MESSAGE.getMember().getNickname(),
                CONTENT,
                MESSAGE.getTimeCreated().toEpochSecond());

        if (sendCont.length() >= 2000) {
            MESSAGE.reply(Callerphone.Callerphone + "Message Too Long.").queue();
        } else {
            ChannelPool.messageOut(event.getChannel().getId(),
                    event.getChannel().getId(),
                    sendCont);
        }

    }
}
