package com.itsmarsss.callerphone.tccallerphone.services;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.Response;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.tccallerphone.ChatResponse;
import com.itsmarsss.callerphone.tccallerphone.ChatResult;
import com.itsmarsss.callerphone.tccallerphone.entities.ChatMessage;
import com.itsmarsss.callerphone.tccallerphone.entities.ChatMode;
import com.itsmarsss.callerphone.tccallerphone.entities.Conversation;
import com.itsmarsss.callerphone.tccallerphone.entities.QueueEntry;
import com.itsmarsss.callerphone.tccallerphone.repository.ConversationRepository;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ConversationService {
    private static final Logger logger = LoggerFactory.getLogger(ConversationService.class);

    private static final ConversationService INSTANCE = new ConversationService();

    private final ConcurrentHashMap<String, Conversation> activeConversations = new ConcurrentHashMap<String, Conversation>();
    private final QueueService queueService;
    private final MessageService messageService;
    private final ConversationRepository repository;

    private ConversationService() {
        this.queueService = new QueueService();
        this.messageService = new MessageService();
        this.repository = new ConversationRepository();
    }

    public static ConversationService getInstance() {
        return INSTANCE;
    }

    public synchronized ChatResult startChat(String channelId, ChatMode mode) {
        if (isInConversation(channelId) || queueService.isQueued(channelId)) {
            return ChatResult.conflict();
        }

        Optional<QueueEntry> match = queueService.dequeueMatch(channelId);
        if (!match.isPresent()) {
            queueService.enqueue(channelId, mode);
            return ChatResult.queued();
        }

        QueueEntry waiting = match.get();
        Conversation convo = new Conversation(
                waiting.getChannelId(),
                channelId,
                waiting.getMode(),
                mode
        );

        TextChannel callerChannel = ToolSet.getTextChannel(convo.getCallerChannelId());
        TextChannel receiverChannel = ToolSet.getTextChannel(convo.getReceiverChannelId());

        if (callerChannel == null || receiverChannel == null) {
            // Re-queue the still-valid party if possible
            if (callerChannel != null) {
                queueService.enqueue(waiting.getChannelId(), waiting.getMode());
            } else if (receiverChannel != null) {
                queueService.enqueue(channelId, mode);
            }
            return ChatResult.nonExistent();
        }

        activeConversations.put(convo.getCallerChannelId(), convo);
        activeConversations.put(convo.getReceiverChannelId(), convo);

        callerChannel.sendMessage(ChatResponse.PICKED_UP.toString()).queue();

        logger.info("Matched channels {} <-> {}", convo.getCallerChannelId(), convo.getReceiverChannelId());
        logger.info("Guilds {} <-> {}", callerChannel.getGuild().getId(), receiverChannel.getGuild().getId());

        return ChatResult.matched(convo);
    }

    public synchronized MessageCreateData endConversation(String channelId) {
        Conversation convo = activeConversations.get(channelId);
        if (convo == null) {
            // Cancel queue if waiting
            if (queueService.removeFromQueue(channelId)) {
                return new MessageCreateBuilder().setContent(ChatResponse.HUNG_UP.toString()).build();
            }
            return new MessageCreateBuilder().setContent(ChatResponse.NO_CALL.toString()).build();
        }

        TextChannel callerChannel = ToolSet.getTextChannel(convo.getCallerChannelId());
        TextChannel receiverChannel = ToolSet.getTextChannel(convo.getReceiverChannelId());
        Button reportButton = Button.danger("reportchat-" + convo.getId(), "Report");

        TextChannel other = convo.isCallerChannel(channelId) ? receiverChannel : callerChannel;
        if (other != null) {
            other.sendMessage(ChatResponse.OTHER_PARTY_HUNG_UP.toString())
                    .setComponents(ActionRow.of(reportButton))
                    .queue();
        }

        finalizeConversation(convo);

        return new MessageCreateBuilder()
                .setContent(ChatResponse.HUNG_UP.toString())
                .setComponents(ActionRow.of(reportButton))
                .build();
    }

    public boolean reportActive(String channelId) {
        Conversation convo = activeConversations.get(channelId);
        if (convo == null) {
            return false;
        }
        convo.markAsReported();
        return true;
    }

    public void reportById(String conversationId) {
        Optional<Conversation> stored = repository.findById(conversationId);
        if (stored.isPresent()) {
            messageService.sendTranscript(
                    ToolSet.getTextChannel(Callerphone.config.getReportChatChannel()),
                    stored.get()
            );
            repository.markAsReported(conversationId);
            return;
        }

        // Active conversation report by id
        for (Conversation convo : activeConversations.values()) {
            if (convo.getId().equals(conversationId)) {
                convo.markAsReported();
                messageService.sendTranscript(
                        ToolSet.getTextChannel(Callerphone.config.getReportChatChannel()),
                        convo
                );
                return;
            }
        }
    }

    public Optional<Conversation> getActiveConversation(String channelId) {
        return Optional.ofNullable(activeConversations.get(channelId));
    }

    public boolean isInConversation(String channelId) {
        return activeConversations.containsKey(channelId);
    }

    public boolean handleMessage(String channelId, User author, String rawContent) {
        Conversation convo = activeConversations.get(channelId);
        if (convo == null) {
            return false;
        }

        boolean fromCaller = convo.isCallerChannel(channelId);
        ChatMessage message = messageService.createMessage(author, rawContent, channelId, fromCaller);
        convo.addMessage(message);

        if (!convo.canSendMessage(channelId, ToolSet.MESSAGE_COOLDOWN)) {
            return true;
        }

        convo.updateLastMessageTime(channelId);

        String outbound = messageService.prepareOutboundContent(rawContent, convo.shouldFilterProfanity());
        String destinationId = convo.getOtherChannelId(channelId);
        TextChannel destination = ToolSet.getTextChannel(destinationId);

        boolean sent = messageService.sendToChannel(
                destination,
                convo,
                author,
                outbound,
                convo.isAnonymous(channelId)
        );

        if (!sent) {
            terminateDueToError(convo);
        }
        return true;
    }

    public void terminateDueToError(Conversation convo) {
        TextChannel callerChannel = ToolSet.getTextChannel(convo.getCallerChannelId());
        TextChannel receiverChannel = ToolSet.getTextChannel(convo.getReceiverChannelId());

        if (callerChannel != null) {
            callerChannel.sendMessage(Response.CONNECTION_ERROR.toString()).queue();
        }
        if (receiverChannel != null) {
            receiverChannel.sendMessage(Response.CONNECTION_ERROR.toString()).queue();
        }

        finalizeConversation(convo);
    }

    private void finalizeConversation(Conversation convo) {
        convo.end();

        messageService.sendTranscript(
                ToolSet.getTextChannel(Callerphone.config.getTempChatChannel()),
                convo
        );
        repository.save(convo);

        if (convo.isReported()) {
            messageService.sendTranscript(
                    ToolSet.getTextChannel(Callerphone.config.getReportChatChannel()),
                    convo
            );
        }

        activeConversations.remove(convo.getCallerChannelId(), convo);
        activeConversations.remove(convo.getReceiverChannelId(), convo);
    }

    public QueueService getQueueService() {
        return queueService;
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public ConversationRepository getRepository() {
        return repository;
    }
}
