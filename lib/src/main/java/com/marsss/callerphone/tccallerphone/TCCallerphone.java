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

    public static ChatStatus onCallCommand(TextChannel tcchannel, boolean cens, boolean anon) {
        final Logger logger = LoggerFactory.getLogger(TCCallerphone.class);
        final String CHANNELID = tcchannel.getId();
        final JDA jda = Callerphone.jda;

        for (int i = 0; i < convos.size(); i++) {
            ConvoStorage convo = convos.get(i);
            if (!convo.getCallerTCID().equals("empty") && convo.getReceiverTCID().equals("")) {
                convo.setRFF(cens);
                convo.setRAnon(anon);
                convo.setReceiverTCID(CHANNELID);
                convo.setLastMessage(System.currentTimeMillis());

                logger.info("From TC: " + convo.getCallerTCID() + " - To TC: " + convo.getReceiverTCID());
                logger.info("From Guild: " + jda.getTextChannelById(convo.getCallerTCID()).getGuild().getId() + " - To Guild: " + jda.getTextChannelById(convo.getReceiverTCID()).getGuild().getId());

                return ChatStatus.SUCCESS_RECEIVER;
            } else if (convo.getCallerTCID().equals("empty")) {
                convo.setCFF(cens);
                convo.setCAnon(anon);
                convo.setCallerTCID(CHANNELID);

                return ChatStatus.SUCCESS_CALLER;
            }
        }
        logger.warn("Port not found");
        return ChatStatus.NON_EXISTENT;
    }

    public static String onEndCallCommand(TextChannel channel) {
        if (!hasCall(channel.getId())) {
            return Callerphone.Callerphone + "There is no call to end!";
        }

        final JDA jda = Callerphone.jda;

        ConvoStorage convo = getCall(channel.getId());

        if (convo != null) {
            final String callerID = convo.getCallerTCID();
            final String receiverID = convo.getReceiverTCID();

            if (receiverID.equals(channel.getId())) {
                jda.getTextChannelById(callerID).sendMessage(Callerphone.Callerphone + "The other party hung up the phone.").queue();
            } else {
                jda.getTextChannelById(receiverID).sendMessage(Callerphone.Callerphone + "The other party hung up the phone.").queue();
            }

            final boolean report = convo.getReport();

            ArrayList<String> data = new ArrayList<>(convo.getMessages());

            if (report) {
                report(data, callerID, receiverID);
            }

            convo.resetMessage();

            return Callerphone.Callerphone + "You hung up the phone.";
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
        for (ConvoStorage c : convos) {
            try {
                if ((tc.equals(c.getCallerTCID()) || tc.equals(c.getReceiverTCID()))) {
                    return c;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static boolean hasCall(String tc) {
        for (ConvoStorage c : convos) {
            try {
                if ((tc.equals(c.getCallerTCID()) || tc.equals(c.getReceiverTCID()))) {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

}
