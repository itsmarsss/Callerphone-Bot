package com.itsmarsss.callerphone.call.service;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.Constants;
import com.itsmarsss.callerphone.Response;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.call.model.CallMessage;
import com.itsmarsss.callerphone.call.model.CallSession;
import com.itsmarsss.database.categories.Users;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.time.Instant;
import java.util.List;

public final class CallMessageService {

    public CallMessage create(User author, String content, String channelId, boolean fromSideA) {
        return new CallMessage(
                author.getId(),
                author.getName(),
                content,
                channelId,
                fromSideA,
                List.of(ToolSet.messageFlagged(content)),
                Instant.now()
        );
    }

    public String prepareOutbound(String content) {
        if (ToolSet.hasPing(content)) {
            return Response.ATTEMPTED_PING.toString();
        }
        if (ToolSet.hasLink(content)) {
            return Response.ATTEMPTED_LINK.toString();
        }
        if (content.length() > Constants.MAX_MESSAGE_LENGTH) {
            return Response.MESSAGE_TOO_LONG.toString();
        }
        return content;
    }

    public boolean send(TextChannel destination, User author, String content) {
        if (destination == null) {
            return false;
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

    public void sendTranscript(TextChannel channel, CallSession session) {
        if (channel == null || session == null) {
            return;
        }
        channel.sendMessage("**Call ID:** " + session.getId() + " (" + session.getSource() + ")")
                .addFiles(net.dv8tion.jda.api.utils.FileUpload.fromData(
                        session.formatTranscript().getBytes(), session.getId() + ".txt"))
                .queue();
    }
}
