package com.marsss.callerphone.channelpool;

import com.marsss.callerphone.Callerphone;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ChannelPoolListener extends ListenerAdapter {

    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {

        final Message MESSAGE = event.getMessage();
        if (MESSAGE.isWebhookMessage())
            return;

        if (!event.getChannel().canTalk())
            return;

        final Member MEMBER = event.getMember();

        final String CONTENT = MESSAGE.getContentDisplay();

        if (MEMBER.getUser().isBot() || MEMBER.getUser().isSystem())
            return;

        if (CONTENT.startsWith("\\\\") || CONTENT.startsWith(Callerphone.Prefix)) {
            return;
        }
        if (!(ChannelPool.isHost(event.getChannel().getId()) || ChannelPool.isChild(event.getChannel().getId()))) {
            return;
        }
        String sendCont = String.format("**%s**#%s `%s` | <t:%d:f>\n%s",
                MESSAGE.getAuthor().getName(),
                MESSAGE.getAuthor().getDiscriminator(),
                MEMBER.getEffectiveName(),
                MESSAGE.getTimeCreated().toEpochSecond(),
                CONTENT
        );

        if (ChannelPool.isHost(event.getChannel().getId()) || ChannelPool.isChild(event.getChannel().getId())) {
            if (sendCont.length() >= 2000) {
                MESSAGE.reply(Callerphone.Callerphone + "Message Too Long.").queue();
                return;
            }
            ChannelPool.broadCast(event.getChannel().getId(),
                    event.getChannel().getId(),
                    sendCont
            );
        }
        Callerphone.award(event.getAuthor(), 3);
        Callerphone.addTransmit(event.getAuthor(), 1);
    }

}
