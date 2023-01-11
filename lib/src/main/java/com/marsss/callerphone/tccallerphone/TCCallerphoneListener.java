package com.marsss.callerphone.tccallerphone;

import java.time.LocalDateTime;

import com.marsss.callerphone.Callerphone;

import com.marsss.callerphone.listeners.CommandListener;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class TCCallerphoneListener extends ListenerAdapter {

    private final String CP_EMJ = Callerphone.Callerphone;
    private final String MESSAGE_TOO_LONG = ":x: I sent a message too long for Callerphone to handle! :x:";

    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {

        if (!event.getChannel().canTalk())
            return;

        final Message MESSAGE = event.getMessage();
        String messageRaw = MESSAGE.getContentDisplay();
        final String[] args = messageRaw.toLowerCase().split("\\s+");


        if (args[0].toLowerCase().startsWith(Callerphone.Prefix))
            return;

        if (Callerphone.blacklist.contains(event.getAuthor().getId())) {
            event.getMessage().addReaction("\u274C").queue();
            return;
        }

        if (!TCCallerphone.hasCall(event.getChannel().getId()))
            return;

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

        if (messageRaw.length() > 1500)
            messageRaw = MESSAGE_TOO_LONG;

        if (c.getCallerTCID().equals(CHANNELID)) {
            if (c.getReceiverFamilyFriendly()) {
                messageRaw = filter(messageRaw);
            }

            c.setCallerLastMessage(System.currentTimeMillis());
            sendMessage(c, c.getCallerAnonymous(), c.getReceiverTCID(), messageRaw, MESSAGE);

        } else if (c.getReceiverTCID().equals(CHANNELID)) {
            if (c.getCallerFamilyFriendly()) {
                messageRaw = filter(messageRaw);
            }

            c.setReceiverLastMessage(System.currentTimeMillis());
            sendMessage(c, c.getReceiverAnonymous(), c.getCallerTCID(), messageRaw, MESSAGE);
        }

        if(!Callerphone.poolChatCoolDown.containsKey(event.getAuthor())) {
            Callerphone.poolChatCoolDown.put(event.getAuthor().getId(), 0L);
        }

        if((System.currentTimeMillis() - Callerphone.poolChatCoolDown.get(event.getAuthor().getId())) > 30000) {
            Callerphone.poolChatCoolDown.put(event.getAuthor().getId(), System.currentTimeMillis());

            Callerphone.reward(event.getAuthor(), 5);
            Callerphone.addTransmit(event.getAuthor(), 1);
        }

    }

    private final String DEFAULT_TEMPLATE = "**%s**#%s " + Callerphone.CallerphoneCall + "%s";
    private final String MODERATOR_TEMPLATE = "***[Moderator]* %s**#%s " + Callerphone.CallerphoneCall + "%s";
    private final String PREFIX_TEMPLATE = "***[%s]* %s**#%s " + Callerphone.CallerphoneCall + "%s";

    private void sendMessage(ConvoStorage c, boolean anon, String destination, String content, Message msg) {
        final TextChannel DESTINATION_CHANNEL = Callerphone.getTextChannel(destination);

        if (anon) {
            if (DESTINATION_CHANNEL != null) {
                DESTINATION_CHANNEL.sendMessage("**DiscordUser**#0000 " + Callerphone.CallerphoneCall + content).queue();
            } else {
                terminate(c);
            }
            return;
        }
        User auth = msg.getAuthor();
        String template = DEFAULT_TEMPLATE;
        if (Callerphone.admin.contains(msg.getAuthor().getId())) {
            template = MODERATOR_TEMPLATE;
        } else if (Callerphone.prefix.containsKey(msg.getAuthor().getId())) {
            template = PREFIX_TEMPLATE.replaceFirst("%s", Callerphone.prefix.get(msg.getAuthor().getId()));
        }
        if (DESTINATION_CHANNEL != null) {
            DESTINATION_CHANNEL.sendMessage(String.format(template, auth.getName(), auth.getDiscriminator(), content)).queue();
        } else {
            terminate(c);
        }
    }

    private String filter(String messageraw) {
        for (String ftr : Callerphone.filter) {
            StringBuilder rep = new StringBuilder();
            for (int i = 0; i < ftr.length(); i++) {
                rep.append("#");
            }
            messageraw = messageraw.replaceAll("(?i)" + ftr, rep.toString());
        }
        return messageraw;
    }

    private final String CONNECTION_ERROR = CP_EMJ + "Connection error, call ended.";

    private void terminate(ConvoStorage c) {
        StringBuilder data = new StringBuilder();
        for (String m : c.getMessages())
            data.append(m).append("\n");

        final TextChannel CALLER_CHANNEL = Callerphone.getTextChannel(c.getCallerTCID());
        final TextChannel RECEIVER_CHANNEL = Callerphone.getTextChannel(c.getReceiverTCID());
        if (CALLER_CHANNEL != null) {
            CALLER_CHANNEL.sendMessage(CONNECTION_ERROR).queue();
        }

        if (RECEIVER_CHANNEL != null) {
            RECEIVER_CHANNEL.sendMessage(CONNECTION_ERROR).queue();
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
            final TextChannel REPORT_CHANNEL = Callerphone.getTextChannel(Callerphone.reportchannel);
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
