package com.marsss.callerphone.tccallerphone;

import com.marsss.callerphone.Callerphone;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class TCCallerphone {

    //public static ConvoStorage[]convos = new ConvoStorage[10000];

    public static ArrayList<ConvoStorage> convos = new ArrayList<>();

    public static String onCallCommand(TextChannel tcchannel, boolean cens, boolean anon) {
        final Logger logger = LoggerFactory.getLogger(TCCallerphone.class);
        final String CHANNELID = tcchannel.getId();
        final JDA jda = Callerphone.jda;

        for (int i = 0; i < convos.size(); i++) {
            ConvoStorage convo = convos.get(i);
            if (!convo.getCallerTCID().equals("empty") && convo.getReceiverTCID().equals("")) {
                StringBuilder msg = new StringBuilder();
                if (!cens) {
                    msg.append("This chat will be uncensored, if you do not wish to proceed please run `" + Callerphone.Prefix + "endchat`");
                }
                convo.setRFF(cens);
                convo.setRAnon(anon);
                convo.setReceiverTCID(CHANNELID);
                convo.setLastMessage(System.currentTimeMillis());

                jda.getTextChannelById(convo.getCallerTCID()).sendMessage(Callerphone.Callerphone + "Someone picked up the phone!").queue();

                msg.append((!cens ? "\n\n" : "") + Callerphone.Callerphone + "Calling...");
                jda.getTextChannelById(convo.getReceiverTCID()).sendMessage(Callerphone.Callerphone + "Someone picked up the phone!").queue();

                logger.info("From TC: " + convo.getCallerTCID() + " - To TC: " + convo.getReceiverTCID());
                logger.info("From Guild: " + jda.getTextChannelById(convo.getCallerTCID()).getGuild().getId() + " - To Guild: " + jda.getTextChannelById(convo.getReceiverTCID()).getGuild().getId());

                return msg.toString();
            } else if (convo.getCallerTCID().equals("empty")) {
                StringBuilder msg = new StringBuilder();
                if (!cens) {
                    msg.append("This chat will be uncensored, if you do not wish to proceed please run `" + Callerphone.Prefix + "endchat`");
                }

                convo.setCFF(cens);
                convo.setCAnon(anon);
                convo.setCallerTCID(CHANNELID);

                msg.append((!cens ? "\n\n" : "") + Callerphone.Callerphone + "Calling...");

                return msg.toString();
            }
        }
        logger.warn("Port not found");
        return Callerphone.Callerphone + "Hmmm, I was unable to find an open port!";
    }

    public static String onEndCallCommand(TextChannel channel) {
        if (!hasCall(channel.getId())) {
            return Callerphone.Callerphone + "There is no call to end!";
        }

        final JDA jda = Callerphone.jda;

        for(ConvoStorage c : convos) {
            TextChannel CALLER = null;
            TextChannel RECEIVER = null;

            try {
                CALLER = jda.getTextChannelById(c.getCallerTCID());
            }catch(Exception e) {}

            try {
                RECEIVER = jda.getTextChannelById(c.getReceiverTCID());
            }catch(Exception e) {}

            if(CALLER == null) {
                c.resetMessage();
                continue;
            }

            if(CALLER.getId().equals(channel.getId())) {
                if(RECEIVER != null) {
                    RECEIVER.sendMessage(Callerphone.Callerphone + "The other party hung up the phone.").queue();
                }

                final String callerID = c.getCallerTCID();
                final String receiverID = c.getReceiverTCID();

                boolean report = c.getReport();

                ArrayList<String> DATA = new ArrayList<>(c.getMessages());

                c.resetMessage();

                if(report) {

                    LocalDateTime now = LocalDateTime.now();
                    final String month = String.valueOf(now.getMonthValue());
                    final String day = String.valueOf(now.getDayOfMonth());
                    final String hour = String.valueOf(now.getHour());
                    final String minute = String.valueOf(now.getMinute());
                    final String ID = month + "/" + day + "/" + hour + "/" + minute + "C" + callerID + "R" + receiverID;

                    StringBuilder data = new StringBuilder();
                    for(String m : DATA)
                        data.append(m).append("\n");
                    jda.getTextChannelById(com.marsss.callerphone.Callerphone.reportchannel).sendMessage("**ID:** " + ID).addFile(data.toString().getBytes(), ID + ".txt").queue();

                }

                return Callerphone.Callerphone + "You hung up the phone.";
            }else if(RECEIVER.getId().equals(channel.getId())) {
                if(CALLER != null) {
                    CALLER.sendMessage(Callerphone.Callerphone + "The other party hung up the phone.").queue();
                }

                final String callerID = c.getCallerTCID();
                final String receiverID = c.getReceiverTCID();

                boolean report = c.getReport();

                ArrayList<String> DATA = new ArrayList<>(c.getMessages());

                c.resetMessage();

                if(report) {
                    LocalDateTime now = LocalDateTime.now();
                    final String month = String.valueOf(now.getMonthValue());
                    final String day = String.valueOf(now.getDayOfMonth());
                    final String hour = String.valueOf(now.getHour());
                    final String minute = String.valueOf(now.getMinute());
                    final String ID = month + "/" + day + "/" + hour + "/" + minute + "C" + callerID + "R" + receiverID;

                    StringBuilder data = new StringBuilder();
                    for(String m : DATA)
                        data.append(m).append("\n");
                    jda.getTextChannelById(com.marsss.callerphone.Callerphone.reportchannel).sendMessage("**ID:** " + ID).addFile(data.toString().getBytes(), ID + ".txt").queue();
                }

                return Callerphone.Callerphone + "You hung up the phone.";
            }
        }

        return Callerphone.Callerphone + "I was not able to find the call...";
    }

    private static void report(ArrayList<String> data, String callerID, String receiverID) {
        LocalDateTime now = LocalDateTime.now();
        final String month = String.valueOf(now.getMonthValue());
        final String day = String.valueOf(now.getDayOfMonth());
        final String hour = String.valueOf(now.getHour());
        final String minute = String.valueOf(now.getMinute());
        final String ID = month + "/" + day + "/" + hour + "/" + minute + "C" + callerID + "R" + receiverID;

        StringBuilder dataString = new StringBuilder();
        for (String m : data)
            dataString.append(m).append("\n");

        Callerphone.jda.getTextChannelById(Callerphone.reportchannel).sendMessage("**ID:** " + ID).addFile(dataString.toString().getBytes(), ID + ".txt").queue();
    }

    public static ConvoStorage getCall(String tc) {
        for(ConvoStorage c : convos) {
            try {
                if((tc.equals(c.getCallerTCID()) || tc.equals(c.getReceiverTCID()))) {
                    return c;
                }
            }catch(Exception e) {}
        }
        return null;
    }

    public static boolean hasCall(String tc) {
        for(ConvoStorage c : convos) {
            try {
                if((tc.equals(c.getCallerTCID()) || tc.equals(c.getReceiverTCID()))) {
                    return true;
                }
            }catch(Exception e) {}
        }
        return false;
    }

}
