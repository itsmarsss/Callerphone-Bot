package com.marsss.callerphone.tccallerphone;

import java.time.LocalDateTime;

import com.marsss.callerphone.Callerphone;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class TCCallerphoneListener extends ListenerAdapter {

    private JDA jda = Callerphone.jda;

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
        c.setLastMessage(System.currentTimeMillis());

        if (messageRaw.length() > 1500)
            messageRaw = MESSAGE_TOO_LONG;

        if (c.getCallerTCID().equals(CHANNELID)) {
            if (c.getRFF()) {
                messageRaw = filter(messageRaw);
            }

            try {
                sendMessage(c.getCAnon(), c.getReceiverTCID(), messageRaw, MESSAGE);
            } catch (Exception e) {
                e.printStackTrace();
                terminate(c, jda);
            }
        } else if (c.getReceiverTCID().equals(CHANNELID)) {
            if (c.getCFF()) {
                messageRaw = filter(messageRaw);
            }

            try {
                sendMessage(c.getRAnon(), c.getCallerTCID(), messageRaw, MESSAGE);
            } catch (Exception e) {
                e.printStackTrace();
                terminate(c, jda);
            }
        }

        Callerphone.award(event.getAuthor(), 5);
        Callerphone.addTransmit(event.getAuthor(), 1);
    }

    private final String DEFAULT_TEMPLATE = "**%s**#%s " + Callerphone.CallerphoneCall + "%s";
    private final String MODERATOR_TEMPLATE = "***[Moderator]* %s**#%s " + Callerphone.CallerphoneCall + "%s";
    private final String PREFIX_TEMPLATE = "***[%s]* %s**#%s " + Callerphone.CallerphoneCall + "%s";

    private void sendMessage(boolean anon, String destination, String content, Message msg) {
        if (anon) {
            jda.getTextChannelById(destination).sendMessage("**DiscordUser**#0000 " + Callerphone.CallerphoneCall + content).queue();
            return;
        }
        User auth = msg.getAuthor();
        String template = DEFAULT_TEMPLATE;
        if (Callerphone.admin.contains(msg.getAuthor().getId())) {
            template = MODERATOR_TEMPLATE;
        } else if (Callerphone.prefix.containsKey(msg.getAuthor().getId())) {
            template = PREFIX_TEMPLATE.replaceFirst("%s", Callerphone.prefix.get(msg.getAuthor().getId()));
        }
        jda.getTextChannelById(destination).sendMessage(String.format(template, auth.getName(), auth.getDiscriminator(), content)).queue();
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

    private void terminate(ConvoStorage c, JDA jda) {
        StringBuilder data = new StringBuilder();
        for (String m : c.getMessages())
            data.append(m).append("\n");

        try {
            jda.getTextChannelById(c.getCallerTCID()).sendMessage(CONNECTION_ERROR).queue();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            jda.getTextChannelById(c.getReceiverTCID()).sendMessage(CONNECTION_ERROR).queue();
        } catch (Exception ex) {
            ex.printStackTrace();
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
            jda.getTextChannelById(Callerphone.reportchannel)
                    .sendMessage("**ID:** " + ID)
                    .addFile(DATA.getBytes(), ID + ".txt")
                    .queue();
        }
    }
}
