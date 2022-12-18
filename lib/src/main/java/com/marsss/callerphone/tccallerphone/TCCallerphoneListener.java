package com.marsss.callerphone.tccallerphone;

import java.time.LocalDateTime;

import com.marsss.callerphone.Callerphone;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class TCCallerphoneListener extends ListenerAdapter {
    private static final String cpEmj = Callerphone.Callerphone;

    private static JDA jda = Callerphone.jda;

    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {

        if (!event.getChannel().canTalk())
            return;

        final Message MESSAGE = event.getMessage();
        String MESSAGERAW = MESSAGE.getContentDisplay();
        final String[] args = MESSAGERAW.toLowerCase().split("\\s+");


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

        String CHANNELID = event.getChannel().getId();

        ConvoStorage c = TCCallerphone.getCall(CHANNELID);

        if (c == null) {
            return;
        }

        if (c.getCallerTCID().equals("empty") || c.getReceiverTCID().equals("")) {
            return;
        }
        c.addMessage((c.getCallerTCID().equals(CHANNELID) ? "Caller " : "Receiver ") + MESSAGE.getAuthor().getAsTag() + "(" + MESSAGE.getAuthor().getId() + ")" + ": " + MESSAGERAW);
        c.setLastMessage(System.currentTimeMillis());

        if (MESSAGERAW.length() > 1500)
            MESSAGERAW = ":x: I sent a message too long for Callerphone to handle! :x:";

        if (c.getCallerTCID().equals(CHANNELID)) {
            if (c.getRFF()) {
                MESSAGERAW = filter(MESSAGERAW);
            }

            try {
                sendMessage(c.getCAnon(), c.getReceiverTCID(), MESSAGERAW, MESSAGE);
            } catch (Exception e) {
                e.printStackTrace();
                terminate(c, jda);
            }
        } else if (c.getReceiverTCID().equals(CHANNELID)) {
            if (c.getCFF()) {
                MESSAGERAW = filter(MESSAGERAW);
            }

            try {
                sendMessage(c.getRAnon(), c.getCallerTCID(), MESSAGERAW, MESSAGE);
            } catch (Exception e) {
                e.printStackTrace();
                terminate(c, jda);
            }
        }
    }

    private void sendMessage(boolean anon, String destination, String content, Message msg) {
        if (anon) {
            jda.getTextChannelById(destination).sendMessage("**DiscordUser**#0000 " + Callerphone.CallerphoneCall + content).queue();
            return;
        }
        User auth = msg.getAuthor();
        String template = "**%s**#%s " + Callerphone.CallerphoneCall + "%s";
        if (Callerphone.admin.contains(msg.getAuthor().getId())) {
            template = "***[Moderator]* %s**#%s " + Callerphone.CallerphoneCall + "%s";
        } else if (Callerphone.prefix.containsKey(msg.getAuthor().getId())) {
            template = "***[" + Callerphone.prefix.get(msg.getAuthor().getId()) + "]* %s**#%s " + Callerphone.CallerphoneCall + "%s";
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

    private void terminate(ConvoStorage c, JDA jda) {
        StringBuilder data = new StringBuilder();
        for (String m : c.getMessages())
            data.append(m).append("\n");

        try {
            jda.getTextChannelById(c.getCallerTCID()).sendMessage(cpEmj + "Connection error, call ended.").queue();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            jda.getTextChannelById(c.getReceiverTCID()).sendMessage(cpEmj + "Connection error, call ended.").queue();
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
            jda.getTextChannelById(Callerphone.reportchannel).sendMessage("**ID:** " + ID).addFile(DATA.getBytes(), ID + ".txt").queue();
        }
    }
}
