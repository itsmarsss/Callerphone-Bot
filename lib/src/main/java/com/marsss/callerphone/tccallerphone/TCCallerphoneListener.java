package com.marsss.callerphone.tccallerphone;

import java.time.LocalDateTime;

import com.marsss.callerphone.Callerphone;

import com.marsss.callerphone.Response;
import com.marsss.callerphone.ToolSet;
import com.marsss.database.categories.Cooldown;
import com.marsss.database.categories.Users;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;

public class TCCallerphoneListener extends ListenerAdapter {

    public void onMessageReceived(MessageReceivedEvent event) {
        if(!event.isFromGuild()) {
            return;
        }

        final Message MESSAGE = event.getMessage();


        if (MESSAGE.isWebhookMessage())
            return;

        if (!TCCallerphone.hasCall(event.getChannel().getId()))
            return;

        final Member MEMBER = event.getMember();

        if(!Users.hasUser(MEMBER.getId())) {
            ToolSet.sendPPAndTOS(event);
            return;
        }

        if (Users.isBlacklisted(MEMBER.getId())) {
            //event.getMessage().addReaction("\u274C").queue();
            return;
        }

        if (MESSAGE.getAuthor().isBot() || MESSAGE.getAuthor().isSystem())
            return;

        String messageRaw = MESSAGE.getContentDisplay();

        if (messageRaw.startsWith("\\\\") || messageRaw.toLowerCase().startsWith(Callerphone.config.getPrefix()))
            return;

        final String CHANNELID = event.getChannel().getId();

        ConvoStorage c = TCCallerphone.getCall(CHANNELID);

        if (c == null) {
            return;
        }

        if (c.getCallerTCId().equals("empty") || c.getReceiverTCId().isEmpty()) {
            return;
        }
        c.addMessage(
                (c.getCallerTCId().equals(CHANNELID) ? "Caller " : "Receiver ")
                        + MESSAGE.getAuthor().getAsTag()
                        + "(" + MESSAGE.getAuthor().getId() + ")"
                        + ": " + messageRaw
        );

        messageRaw = ToolSet.messageCheck(messageRaw);

        if (c.getCallerTCId().equals(CHANNELID)) {
            if (System.currentTimeMillis() - c.getCallerLastMessage() > ToolSet.MESSAGE_COOLDOWN) {
                if (c.getReceiverFamilyFriendly()) {
                    messageRaw = ToolSet.filter(messageRaw);
                }

                c.setCallerLastMessage(System.currentTimeMillis());
                sendMessage(c, c.getCallerAnonymous(), c.getReceiverTCId(), messageRaw, MESSAGE);
            }
        } else if (c.getReceiverTCId().equals(CHANNELID)) {
            if (System.currentTimeMillis() - c.getReceiverLastMessage() > ToolSet.MESSAGE_COOLDOWN) {
                if (c.getCallerFamilyFriendly()) {
                    messageRaw = ToolSet.filter(messageRaw);
                }

                c.setReceiverLastMessage(System.currentTimeMillis());
                sendMessage(c, c.getReceiverAnonymous(), c.getCallerTCId(), messageRaw, MESSAGE);
            }
        }

        if ((System.currentTimeMillis() - Cooldown.getPoolCooldown(event.getAuthor().getId())) > ToolSet.CREDIT_COOLDOWN) {
            Cooldown.setUserCooldown(event.getAuthor().getId());

            Users.reward(event.getAuthor().getId(), 5);
            Users.addTransmit(event.getAuthor().getId(), 1);
        }

    }

    private void sendMessage(ConvoStorage c, boolean anon, String destination, String content, Message msg) {
        final TextChannel DESTINATION_CHANNEL = ToolSet.getTextChannel(destination);

        if (anon) {
            if (DESTINATION_CHANNEL != null) {
                DESTINATION_CHANNEL.sendMessage("**DiscordUser** " + Callerphone.config.getCallerphoneCall() + content).complete();
            } else {
                terminate(c);
            }
            return;
        }
        User auth = msg.getAuthor();
        String template = Response.DEFAULT_MESSAGE_TEMPLATE.toString();
        if (Users.isModerator(msg.getAuthor().getId())) {
            template = Response.MODERATOR_MESSAGE_TEMPLATE.toString();
        } else if (Users.hasPrefix(msg.getAuthor().getId())) {
            template = Response.PREFIX_MESSAGE_TEMPLATE.toString().replaceFirst("%s", Users.getPrefix(msg.getAuthor().getId()));
        }
        if (DESTINATION_CHANNEL != null) {
            DESTINATION_CHANNEL.sendMessage(String.format(template, auth.getName(), auth.getDiscriminator(), content)).complete();
        } else {
            terminate(c);
        }
    }

    private void terminate(ConvoStorage c) {
        StringBuilder data = new StringBuilder();
        for (String m : c.getMessages())
            data.append(m).append("\n");

        final TextChannel CALLER_CHANNEL = ToolSet.getTextChannel(c.getCallerTCId());
        final TextChannel RECEIVER_CHANNEL = ToolSet.getTextChannel(c.getReceiverTCId());
        if (CALLER_CHANNEL != null) {
            CALLER_CHANNEL.sendMessage(Response.CONNECTION_ERROR.toString()).queue();
        }

        if (RECEIVER_CHANNEL != null) {
            RECEIVER_CHANNEL.sendMessage(Response.CONNECTION_ERROR.toString()).queue();
        }

        c.resetMessage();
        LocalDateTime now = LocalDateTime.now();
        String month = String.valueOf(now.getMonthValue());
        String day = String.valueOf(now.getDayOfMonth());
        String hour = String.valueOf(now.getHour());
        String minute = String.valueOf(now.getMinute());
        String ID = month + day + hour + minute + c.getCallerTCId() + c.getReceiverTCId();

        final String DATA = data.toString();
        if (c.getReport()) {
            final TextChannel REPORT_CHANNEL = ToolSet.getTextChannel(Callerphone.config.getReportChatChannel());
            if (REPORT_CHANNEL == null) {
                System.out.println("Invalid REPORT channel.");
            } else {
                REPORT_CHANNEL
                        .sendMessage("**ID:** " + ID)
                        .addFiles(FileUpload.fromData(DATA.getBytes(), ID + ".txt"))
                        .queue();
            }
        }
    }
}
