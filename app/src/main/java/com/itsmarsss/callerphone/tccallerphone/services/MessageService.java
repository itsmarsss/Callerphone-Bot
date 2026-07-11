package com.itsmarsss.callerphone.tccallerphone.services;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.Response;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.tccallerphone.entities.ChatMessage;
import com.itsmarsss.callerphone.tccallerphone.entities.Conversation;
import com.itsmarsss.database.categories.Users;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.time.Instant;

public class MessageService {

    /**
     * Builds a log message from raw user input. Always records flags for moderation.
     */
    public ChatMessage createMessage(User author, String content, String channelId, boolean fromCaller) {
        return new ChatMessage(
                author.getId(),
                author.getName(),
                content,
                channelId,
                fromCaller,
                ToolSet.messageFlagged(content),
                Instant.now()
        );
    }

    /**
     * Applies safety filters always; profanity censoring only when conversation requires it.
     */
    public String prepareOutboundContent(String content, boolean filterProfanity) {
        if (ToolSet.hasPing(content)) {
            return Response.ATTEMPTED_PING.toString();
        }
        if (ToolSet.hasLink(content)) {
            return Response.ATTEMPTED_LINK.toString();
        }
        if (content.length() > 1500) {
            return Response.MESSAGE_TOO_LONG.toString();
        }
        if (filterProfanity) {
            return ToolSet.filterMessage(content);
        }
        return content;
    }

    /**
     * @return false if destination channel is missing/unwritable
     */
    public boolean sendToChannel(TextChannel destination, Conversation conversation,
                                 User author, String content, boolean anonymous) {
        if (destination == null) {
            return false;
        }

        if (anonymous) {
            int participantNum = conversation.getParticipantIndex(author.getId()) + 1;
            destination.sendMessage("**Discordian " + participantNum + "** "
                    + Callerphone.config.getCallerphoneCall() + content).queue();
            return true;
        }

        String template = Response.DEFAULT_MESSAGE_TEMPLATE.toString();
        if (Users.isModerator(author.getId())) {
            template = Response.MODERATOR_MESSAGE_TEMPLATE.toString();
        } else if (Users.hasPrefix(author.getId())) {
            template = Response.PREFIX_MESSAGE_TEMPLATE.toString()
                    .replaceFirst("%s", Users.getPrefix(author.getId()));
        }
        destination.sendMessage(String.format(template, author.getName(), content)).queue();
        return true;
    }

    public void sendTranscript(TextChannel channel, Conversation conversation) {
        if (channel == null) {
            return;
        }
        String data = conversation.formatTranscript();
        channel.sendMessage("**ID:** " + conversation.getId())
                .addFiles(net.dv8tion.jda.api.utils.FileUpload.fromData(
                        data.getBytes(), conversation.getId() + ".txt"))
                .queue();
    }
}
