package com.marsss.callerphone.channelpool;

import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.Storage;
import com.marsss.callerphone.ToolSet;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ChannelPoolListener extends ListenerAdapter {

    public void onMessageReceived(MessageReceivedEvent event) {

        if(!event.isFromGuild()) {
            return;
        }

        final Message MESSAGE = event.getMessage();

        if (MESSAGE.isWebhookMessage())
            return;

        final Member MEMBER = event.getMember();

        if (Storage.isBlacklisted(MEMBER.getId())) {
            //event.getMessage().addReaction("\u274C").queue();
            return;
        }

        if (MEMBER.getUser().isBot() || MEMBER.getUser().isSystem())
            return;

        String content = MESSAGE.getContentRaw();

        if (content.startsWith("\\\\") || content.toLowerCase().startsWith(Callerphone.config.getPrefix()))
            return;

        if (!(ChannelPool.isHost(event.getChannel().getId()) || ChannelPool.isChild(event.getChannel().getId())))
            return;

        content = ToolSet.messageCheck(content);

        String sendCont = String.format("**%s**#%s `%s` | <t:%d:f>\n%s",
                MESSAGE.getAuthor().getName(),
                MESSAGE.getAuthor().getDiscriminator(),
                MEMBER.getEffectiveName(),
                MESSAGE.getTimeCreated().toEpochSecond(),
                content
        );

        ChannelPool.broadCast(event.getChannel().getId(),
                event.getChannel().getId(),
                sendCont
        );

        if ((System.currentTimeMillis() - Storage.getUserCooldown(event.getAuthor())) > ToolSet.CREDIT_COOLDOWN) {
            Storage.updateUserCooldown(event.getAuthor());

            Storage.reward(event.getAuthor(), 3);
            Storage.addTransmit(event.getAuthor(), 1);
        }
    }
}
