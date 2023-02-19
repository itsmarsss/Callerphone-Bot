package com.marsss.callerphone.tccallerphone;

import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.ToolSet;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class TCCallerphone {

    public static final ArrayList<ConvoStorage> convos = new ArrayList<>();

    private static final String CP_EMJ = Callerphone.Callerphone;

    private static final String PICKED_UP = CP_EMJ + "Someone picked up the phone!";

    public static ChatStatus onCallCommand(TextChannel tcchannel, boolean cens, boolean anon) {
        final Logger logger = LoggerFactory.getLogger(TCCallerphone.class);
        final String CHANNELID = tcchannel.getId();

        for (ConvoStorage convo : convos) {
            if (!convo.getCallerTCID().equals("empty") && convo.getReceiverTCID().equals("")) {
                convo.setReceiverFamilyFriendly(cens);
                convo.setReceiverAnonymous(anon);
                convo.setReceiverTCID(CHANNELID);
                convo.setCallerLastMessage(System.currentTimeMillis());
                convo.setReceiverLastMessage(System.currentTimeMillis());

                final TextChannel CALLER_CHANNEL = ToolSet.getTextChannel(convo.getCallerTCID());
                final TextChannel RECEIVER_CHANNEL = ToolSet.getTextChannel(convo.getReceiverTCID());

                if (CALLER_CHANNEL == null || RECEIVER_CHANNEL == null) {
                    convo.resetMessage();
                    return ChatStatus.NON_EXISTENT;
                }
                CALLER_CHANNEL.sendMessage(PICKED_UP).queue();

                logger.info("From TC: "
                        + convo.getCallerTCID()
                        + " - To TC: "
                        + convo.getReceiverTCID()
                );

                logger.info("From Guild: "
                        + CALLER_CHANNEL.getGuild().getId()
                        + " - To Guild: "
                        + RECEIVER_CHANNEL.getGuild().getId()
                );

                return ChatStatus.SUCCESS_RECEIVER;
            } else if (convo.getCallerTCID().equals("empty")) {
                convo.setCallerFamilyFriendly(cens);
                convo.setCallerAnonymous(anon);
                convo.setCallerTCID(CHANNELID);

                return ChatStatus.SUCCESS_CALLER;
            }
        }
        logger.warn("Port not found");
        return ChatStatus.NON_EXISTENT;
    }

    private static final String NO_CALL = CP_EMJ + "There is no call to end!";
    private static final String OTHER_PARTY_HUNG_UP = CP_EMJ + "The other party hung up the phone.";
    private static final String HUNG_UP = CP_EMJ + "You hung up the phone.";

    private static final String NOT_FOUND = CP_EMJ + "I was not able to find the call...";

    public static String onEndCallCommand(TextChannel channel) {
        if (!hasCall(channel.getId())) {
            return NO_CALL;
        }

        ConvoStorage convo = getCall(channel.getId());

        if (convo != null) {
            final String CALLER_ID = convo.getCallerTCID();
            final String RECEIVER_ID = convo.getReceiverTCID();

            final TextChannel CALLER_CHANNEL = ToolSet.getTextChannel(CALLER_ID);
            final TextChannel RECEIVER_CHANNEL = ToolSet.getTextChannel(RECEIVER_ID);

            if (RECEIVER_ID.equals(channel.getId())) {
                if (!convo.getCallerTCID().equals("empty")) {
                    if (CALLER_CHANNEL != null) {
                        CALLER_CHANNEL.sendMessage(OTHER_PARTY_HUNG_UP).queue();
                    }
                }
            } else {
                if (!convo.getReceiverTCID().equals("")) {
                    if (RECEIVER_CHANNEL != null) {
                        RECEIVER_CHANNEL.sendMessage(OTHER_PARTY_HUNG_UP).queue();
                    }
                }
            }

            final boolean report = convo.getReport();

            ArrayList<String> data = new ArrayList<>(convo.getMessages());

            if (report) {
                report(data, CALLER_ID, RECEIVER_ID);
            }

            convo.resetMessage();

            return HUNG_UP;
        }
        return NOT_FOUND;
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


        final TextChannel REPORT_CHANNEL = ToolSet.getTextChannel("867770751440650260"/*Callerphone.reportchannel*/);
        if (REPORT_CHANNEL != null) {
            REPORT_CHANNEL.sendMessage("**ID:** " + ID).addFile(dataString.toString().getBytes(), ID + ".txt").queue();
        }
    }

    public static ConvoStorage getCall(String tc) {
        for (ConvoStorage c : convos) {
            if ((tc.equals(c.getCallerTCID()) || tc.equals(c.getReceiverTCID()))) {
                return c;
            }
        }
        return null;
    }

    public static boolean hasCall(String tc) {
        for (ConvoStorage c : convos) {
            if ((tc.equals(c.getCallerTCID()) || tc.equals(c.getReceiverTCID()))) {
                return true;
            }
        }
        return false;
    }

}
