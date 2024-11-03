package com.marsss.callerphone.tccallerphone;

import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.ToolSet;
import com.marsss.callerphone.tccallerphone.entities.TCConversation;
import com.marsss.callerphone.tccallerphone.entities.TCMessage;
import com.marsss.database.categories.Chats;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;

public class TCCallerphone {

    public static final List<TCConversation> queue = new ArrayList<>();
    public static final Map<String, TCConversation> conversationMap = new HashMap<>();

    public static ChatStatus onCallCommand(MessageChannelUnion tcchannel, boolean anon) {
        final Logger logger = LoggerFactory.getLogger(TCCallerphone.class);
        final String CHANNELID = tcchannel.getId();

        if (queue.isEmpty()) {
            TCConversation convo = new TCConversation();

            convo.setCallerAnonymous(anon);
            convo.setCallerTCId(CHANNELID);
            queue.add(convo);

            return ChatStatus.SUCCESS_CALLER;
        }

        TCConversation convo = queue.get(0);
        queue.remove(0);

        convo.setReceiverAnonymous(anon);
        convo.setReceiverTCId(CHANNELID);

        convo.setStarted(Instant.now().getEpochSecond());
        convo.setCallerLastMessage(System.currentTimeMillis());
        convo.setReceiverLastMessage(System.currentTimeMillis());

        final TextChannel CALLER_CHANNEL = ToolSet.getTextChannel(convo.getCallerTCId());
        final TextChannel RECEIVER_CHANNEL = ToolSet.getTextChannel(convo.getReceiverTCId());

        if (CALLER_CHANNEL == null || RECEIVER_CHANNEL == null) {
            convo.resetMessage();
            return ChatStatus.NON_EXISTENT;
        }

        conversationMap.put(convo.getCallerTCId(), convo);
        conversationMap.put(convo.getReceiverTCId(), convo);

        CALLER_CHANNEL.sendMessage(ChatResponse.PICKED_UP.toString()).queue();

        logger.info("From Channel: {} - To Channel: {}", convo.getCallerTCId(), convo.getReceiverTCId());
        logger.info("From Guild: {} - To Guild: {}", CALLER_CHANNEL.getGuild().getId(), RECEIVER_CHANNEL.getGuild().getId());

        return ChatStatus.SUCCESS_RECEIVER;
    }

    public static MessageCreateData onEndCallCommand(MessageChannelUnion channel) {
        if (!hasCall(channel.getId())) {
            return new MessageCreateBuilder().setContent(ChatResponse.NO_CALL.toString()).build();
        }

        TCConversation convo = getCall(channel.getId());

        if (convo == null) {
            return new MessageCreateBuilder().setContent(ChatResponse.NO_CALL.toString()).build();
        }

        final String CALLER_ID = convo.getCallerTCId();
        final String RECEIVER_ID = convo.getReceiverTCId();

        final TextChannel CALLER_CHANNEL = ToolSet.getTextChannel(CALLER_ID);
        final TextChannel RECEIVER_CHANNEL = ToolSet.getTextChannel(RECEIVER_ID);

        Button reportButton = Button.danger("reportchat-" + convo.getId(), "Report");

        if (RECEIVER_ID.equals(channel.getId())) {
            if (!convo.getCallerTCId().equals("empty")) {
                if (CALLER_CHANNEL != null) {
                    CALLER_CHANNEL.sendMessage(ChatResponse.OTHER_PARTY_HUNG_UP.toString()).setComponents(ActionRow.of(reportButton)).queue();
                }
            }
        } else {
            if (!convo.getReceiverTCId().isEmpty()) {
                if (RECEIVER_CHANNEL != null) {
                    RECEIVER_CHANNEL.sendMessage(ChatResponse.OTHER_PARTY_HUNG_UP.toString()).setComponents(ActionRow.of(reportButton)).queue();
                }
            }
        }

        convo.setEnded(Instant.now().getEpochSecond());

        final boolean report = convo.getReport();

        log(convo);
        Chats.createChat(convo);

        if (report) {
            report(convo);
        }

        conversationMap.remove(convo.getCallerTCId());
        conversationMap.remove(convo.getReceiverTCId());

        return new MessageCreateBuilder().setContent(ChatResponse.HUNG_UP.toString()).setComponents(ActionRow.of(reportButton)).build();
    }

    private static void log(TCConversation convo) {
        List<TCMessage> data = convo.getMessages();

        StringBuilder dataString = new StringBuilder();
        for (TCMessage m : data)
            dataString.append(m).append("\n");


        final TextChannel TEMP_CHANNEL = ToolSet.getTextChannel(Callerphone.config.getTempChatChannel());
        if (TEMP_CHANNEL != null) {
            TEMP_CHANNEL.sendMessage("**ID:** " + convo.getId())
                    .addFiles(FileUpload.fromData(dataString.toString().getBytes(), convo.getId() + ".txt")).queue();
        }
    }

    public static void report(TCConversation convo) {
        List<TCMessage> data = convo.getMessages();

        StringBuilder dataString = new StringBuilder();
        for (TCMessage m : data)
            dataString.append(m).append("\n");


        final TextChannel REPORT_CHANNEL = ToolSet.getTextChannel(Callerphone.config.getReportChatChannel());
        if (REPORT_CHANNEL != null) {
            REPORT_CHANNEL.sendMessage("**ID:** " + convo.getId())
                    .addFiles(FileUpload.fromData(dataString.toString().getBytes(), convo.getId() + ".txt")).queue();
        }
    }

    public static TCConversation getCall(String tc) {
        return conversationMap.getOrDefault(tc, null);
    }

    public static boolean hasCall(String tc) {
        return conversationMap.containsKey(tc);
    }

}
