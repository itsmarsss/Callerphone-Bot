package com.marsss.callerphone.tccallerphone;

import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.ToolSet;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.utils.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class TCCallerphone {

    public static final ArrayList<ConvoStorage> convos = new ArrayList<>();

    public static ChatStatus onCallCommand(MessageChannelUnion tcchannel, boolean cens, boolean anon) {
        final Logger logger = LoggerFactory.getLogger(TCCallerphone.class);
        final String CHANNELID = tcchannel.getId();

        for (ConvoStorage convo : convos) {
            if (!convo.getCallerTCId().equals("empty") && convo.getReceiverTCId().equals("")) {
                convo.setReceiverFamilyFriendly(cens);
                convo.setReceiverAnonymous(anon);
                convo.setReceiverTCId(CHANNELID);
                convo.setCallerLastMessage(System.currentTimeMillis());
                convo.setReceiverLastMessage(System.currentTimeMillis());

                final TextChannel CALLER_CHANNEL = ToolSet.getTextChannel(convo.getCallerTCId());
                final TextChannel RECEIVER_CHANNEL = ToolSet.getTextChannel(convo.getReceiverTCId());

                if (CALLER_CHANNEL == null || RECEIVER_CHANNEL == null) {
                    convo.resetMessage();
                    return ChatStatus.NON_EXISTENT;
                }
                CALLER_CHANNEL.sendMessage(ChatResponse.PICKED_UP.toString()).queue();

                logger.info("From TC: "
                        + convo.getCallerTCId()
                        + " - To TC: "
                        + convo.getReceiverTCId()
                );

                logger.info("From Guild: "
                        + CALLER_CHANNEL.getGuild().getId()
                        + " - To Guild: "
                        + RECEIVER_CHANNEL.getGuild().getId()
                );

                return ChatStatus.SUCCESS_RECEIVER;
            } else if (convo.getCallerTCId().equals("empty")) {
                convo.setCallerFamilyFriendly(cens);
                convo.setCallerAnonymous(anon);
                convo.setCallerTCId(CHANNELID);

                return ChatStatus.SUCCESS_CALLER;
            }
        }
        logger.warn("Port not found");
        return ChatStatus.NON_EXISTENT;
    }

    public static String onEndCallCommand(MessageChannelUnion channel) {
        if (!hasCall(channel.getId())) {
            return ChatResponse.NO_CALL.toString();
        }

        ConvoStorage convo = getCall(channel.getId());

        if (convo != null) {
            final String CALLER_ID = convo.getCallerTCId();
            final String RECEIVER_ID = convo.getReceiverTCId();

            final TextChannel CALLER_CHANNEL = ToolSet.getTextChannel(CALLER_ID);
            final TextChannel RECEIVER_CHANNEL = ToolSet.getTextChannel(RECEIVER_ID);

            if (RECEIVER_ID.equals(channel.getId())) {
                if (!convo.getCallerTCId().equals("empty")) {
                    if (CALLER_CHANNEL != null) {
                        CALLER_CHANNEL.sendMessage(ChatResponse.OTHER_PARTY_HUNG_UP.toString()).queue();
                    }
                }
            } else {
                if (!convo.getReceiverTCId().equals("")) {
                    if (RECEIVER_CHANNEL != null) {
                        RECEIVER_CHANNEL.sendMessage(ChatResponse.OTHER_PARTY_HUNG_UP.toString()).queue();
                    }
                }
            }

            final boolean report = convo.getReport();

            ArrayList<String> data = new ArrayList<>(convo.getMessages());

            log(data, CALLER_ID, RECEIVER_ID);

            if (report) {
                report(data, CALLER_ID, RECEIVER_ID);
            }

            convo.resetMessage();

            return ChatResponse.HUNG_UP.toString();
        }
        return ChatResponse.NO_CALL.toString();
    }

    private static void log(ArrayList<String> data, String callerID, String receiverID) {
        LocalDateTime now = LocalDateTime.now();
        final String month = String.valueOf(now.getMonthValue());
        final String day = String.valueOf(now.getDayOfMonth());
        final String hour = String.valueOf(now.getHour());
        final String minute = String.valueOf(now.getMinute());
        final String ID = month + "/" + day + "/" + hour + "/" + minute + "C" + callerID + "R" + receiverID;

        StringBuilder dataString = new StringBuilder();
        for (String m : data)
            dataString.append(m).append("\n");


        final TextChannel TEMP_CHANNEL = ToolSet.getTextChannel(Callerphone.config.getTempChatChannel());
        if (TEMP_CHANNEL != null) {
            TEMP_CHANNEL.sendMessage("**ID:** " + ID).addFiles(FileUpload.fromData(dataString.toString().getBytes(), ID + ".txt")).queue();
        }
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


        final TextChannel REPORT_CHANNEL = ToolSet.getTextChannel(Callerphone.config.getReportChatChannel());
        if (REPORT_CHANNEL != null) {
            REPORT_CHANNEL.sendMessage("**ID:** " + ID).addFiles(FileUpload.fromData(dataString.toString().getBytes(), ID + ".txt")).queue();
        }
    }

    public static ConvoStorage getCall(String tc) {
        for (ConvoStorage c : convos) {
            if ((tc.equals(c.getCallerTCId()) || tc.equals(c.getReceiverTCId()))) {
                return c;
            }
        }
        return null;
    }

    public static boolean hasCall(String tc) {
        for (ConvoStorage c : convos) {
            if ((tc.equals(c.getCallerTCId()) || tc.equals(c.getReceiverTCId()))) {
                return true;
            }
        }
        return false;
    }

}
