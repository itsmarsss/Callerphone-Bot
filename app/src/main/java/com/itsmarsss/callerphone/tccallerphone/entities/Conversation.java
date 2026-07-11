package com.itsmarsss.callerphone.tccallerphone.entities;

import com.itsmarsss.callerphone.ToolSet;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Active chat conversation between two text channels.
 */
public class Conversation {
    private final String id;
    private final String callerChannelId;
    private final String receiverChannelId;
    private final ChatMode callerMode;
    private final ChatMode receiverMode;
    private final ConversationSettings settings;
    private final List<ChatMessage> messages;
    private final Set<String> participants;
    private final Instant startedAt;
    private Instant endedAt;
    private long callerLastMessageTime;
    private long receiverLastMessageTime;
    private boolean reported;

    public Conversation(String callerChannelId, String receiverChannelId,
                        ChatMode callerMode, ChatMode receiverMode) {
        this(ToolSet.generateUID(), callerChannelId, receiverChannelId, callerMode, receiverMode);
    }

    /** Reconstruct a persisted conversation (e.g. for reports). */
    public Conversation(String id, String callerChannelId, String receiverChannelId,
                        ChatMode callerMode, ChatMode receiverMode) {
        this.id = id != null ? id : ToolSet.generateUID();
        this.callerChannelId = callerChannelId;
        this.receiverChannelId = receiverChannelId;
        this.callerMode = callerMode;
        this.receiverMode = receiverMode;
        this.settings = ConversationSettings.forModes(callerMode, receiverMode);
        this.messages = Collections.synchronizedList(new ArrayList<ChatMessage>());
        this.participants = Collections.synchronizedSet(new LinkedHashSet<String>());
        this.startedAt = Instant.now();
        this.endedAt = null;
        long now = System.currentTimeMillis();
        this.callerLastMessageTime = now;
        this.receiverLastMessageTime = now;
        this.reported = false;
    }

    public synchronized void addMessage(ChatMessage message) {
        messages.add(message);
        participants.add(message.getAuthorId());
    }

    public boolean isParticipant(String userId) {
        return participants.contains(userId);
    }

    public int getParticipantIndex(String userId) {
        synchronized (participants) {
            int index = 0;
            for (String id : participants) {
                if (id.equals(userId)) {
                    return index;
                }
                index++;
            }
            return -1;
        }
    }

    public synchronized void end() {
        if (endedAt == null) {
            this.endedAt = Instant.now();
        }
    }

    public void markAsReported() {
        this.reported = true;
    }

    public boolean isCallerChannel(String channelId) {
        return callerChannelId.equals(channelId);
    }

    public String getOtherChannelId(String channelId) {
        return isCallerChannel(channelId) ? receiverChannelId : callerChannelId;
    }

    public long getLastMessageTime(String channelId) {
        return isCallerChannel(channelId) ? callerLastMessageTime : receiverLastMessageTime;
    }

    public void updateLastMessageTime(String channelId) {
        long now = System.currentTimeMillis();
        if (isCallerChannel(channelId)) {
            callerLastMessageTime = now;
        } else {
            receiverLastMessageTime = now;
        }
    }

    public boolean canSendMessage(String channelId, long cooldownMs) {
        return System.currentTimeMillis() - getLastMessageTime(channelId) > cooldownMs;
    }

    public boolean isAnonymous(String channelId) {
        return isCallerChannel(channelId)
                ? settings.isCallerAnonymous()
                : settings.isReceiverAnonymous();
    }

    public boolean shouldFilterProfanity() {
        return settings.isFilterProfanity();
    }

    public String formatTranscript() {
        StringBuilder transcript = new StringBuilder();
        synchronized (messages) {
            for (ChatMessage message : messages) {
                transcript.append(message.format()).append('\n');
            }
        }
        return transcript.toString();
    }

    public String getId() {
        return id;
    }

    public String getCallerChannelId() {
        return callerChannelId;
    }

    public String getReceiverChannelId() {
        return receiverChannelId;
    }

    public ChatMode getCallerMode() {
        return callerMode;
    }

    public ChatMode getReceiverMode() {
        return receiverMode;
    }

    public ConversationSettings getSettings() {
        return settings;
    }

    public List<ChatMessage> getMessages() {
        synchronized (messages) {
            return new ArrayList<ChatMessage>(messages);
        }
    }

    public Set<String> getParticipants() {
        synchronized (participants) {
            return new LinkedHashSet<String>(participants);
        }
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public long getCallerLastMessageTime() {
        return callerLastMessageTime;
    }

    public long getReceiverLastMessageTime() {
        return receiverLastMessageTime;
    }

    public boolean isReported() {
        return reported;
    }

    public boolean isActive() {
        return endedAt == null;
    }
}
