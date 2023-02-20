package com.marsss.callerphone.tccallerphone;

import java.time.LocalDateTime;

import com.marsss.callerphone.Callerphone;

import com.marsss.callerphone.Response;
import com.marsss.callerphone.Storage;
import com.marsss.callerphone.ToolSet;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class TCCallerphoneListener extends ListenerAdapter {

    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {

        if (!event.getChannel().canTalk())
            return;

        final Message MESSAGE = event.getMessage();
        String messageRaw = MESSAGE.getContentDisplay();
        final String[] args = messageRaw.toLowerCase().split("\\s+");


        if (args[0].toLowerCase().startsWith(Callerphone.config.getPrefix()))
            return;

        if (!TCCallerphone.hasCall(event.getChannel().getId()))
            return;

        if (Storage.isBlacklisted(event.getAuthor().getId())) {
            event.getMessage().addReaction("\u274C").queue();
            return;
        }

        if (MESSAGE.getAuthor().isBot() | MESSAGE.isWebhookMessage()) {
            return;
        }

        final String CHANNELID = event.getChannel().getId();

        ConvoStorage c = TCCallerphone.getCall(CHANNELID);

        if (c == null) {
            return;
        }

        if (c.getCallerTCID().equals("empty") || c.getReceiverTCID().equals("")) {
            return;
        }
        c.addMessage(
                (c.getCallerTCID().equals(CHANNELID) ? "Caller " : "Receiver ")
                        + MESSAGE.getAuthor().getAsTag()
                        + "(" + MESSAGE.getAuthor().getId() + ")"
                        + ": " + messageRaw
        );

        messageRaw = ToolSet.messageCheck(messageRaw);

        if (c.getCallerTCID().equals(CHANNELID)) {
            if (System.currentTimeMillis() - c.getCallerLastMessage() > ToolSet.MESSAGE_COOLDOWN) {
                if (c.getReceiverFamilyFriendly()) {
                    messageRaw = ToolSet.filter(messageRaw);
                }

                c.setCallerLastMessage(System.currentTimeMillis());
                sendMessage(c, c.getCallerAnonymous(), c.getReceiverTCID(), messageRaw, MESSAGE);
            }
        } else if (c.getReceiverTCID().equals(CHANNELID)) {
            if (System.currentTimeMillis() - c.getReceiverLastMessage() > ToolSet.MESSAGE_COOLDOWN) {
                if (c.getCallerFamilyFriendly()) {
                    messageRaw = ToolSet.filter(messageRaw);
                }

                c.setReceiverLastMessage(System.currentTimeMillis());
                sendMessage(c, c.getReceiverAnonymous(), c.getCallerTCID(), messageRaw, MESSAGE);
            }
        }

        if ((System.currentTimeMillis() - Storage.getUserCooldown(event.getAuthor())) > ToolSet.CREDIT_COOLDOWN) {
            Storage.updateUserCooldown(event.getAuthor());

            Storage.reward(event.getAuthor(), 5);
            Storage.addTransmit(event.getAuthor(), 1);
        }

    }

    private void sendMessage(ConvoStorage c, boolean anon, String destination, String content, Message msg) {
        final TextChannel DESTINATION_CHANNEL = ToolSet.getTextChannel(destination);

        if (anon) {
            if (DESTINATION_CHANNEL != null) {
                DESTINATION_CHANNEL.sendMessage("**DiscordUser**#0000 " + Callerphone.config.getCallerphoneCall() + content).complete();
            } else {
                terminate(c);
            }
            return;
        }
        User auth = msg.getAuthor();
        String template = Response.DEFAULT_MESSAGE_TEMPLATE.toString();
        if (Storage.isAdmin(msg.getAuthor().getId())) {
            template = Response.MODERATOR_MESSAGE_TEMPLATE.toString();
        } else if (Storage.hasPrefix(msg.getAuthor().getId())) {
            template = Response.PREFIX_MESSAGE_TEMPLATE.toString().replaceFirst("%s", Storage.getPrefix(msg.getAuthor().getId()));
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

        final TextChannel CALLER_CHANNEL = ToolSet.getTextChannel(c.getCallerTCID());
        final TextChannel RECEIVER_CHANNEL = ToolSet.getTextChannel(c.getReceiverTCID());
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
        String ID = month + day + hour + minute + c.getCallerTCID() + c.getReceiverTCID();

        final String DATA = data.toString();
        if (c.getReport()) {
            final TextChannel REPORT_CHANNEL = ToolSet.getTextChannel(Callerphone.config.getReportChatChannel());
            if (REPORT_CHANNEL == null) {
                System.out.println("Invalid REPORT channel.");
            } else {
                REPORT_CHANNEL
                        .sendMessage("**ID:** " + ID)
                        .addFile(DATA.getBytes(), ID + ".txt")
                        .queue();
            }
        }
    }
}
