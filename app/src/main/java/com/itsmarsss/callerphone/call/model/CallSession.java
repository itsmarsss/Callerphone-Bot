package com.itsmarsss.callerphone.call.model;

import com.itsmarsss.callerphone.ToolSet;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Live random chat between two endpoints. Single product mode (no anon / FF variants).
 * Optional Match profile sharing sits on top via sharedUserIds tracking.
 */
public final class CallSession {
    private final String id;
    private final CallEndpoint sideA;
    private final CallEndpoint sideB;
    private final CallMatchSource source;
    private final List<CallMessage> messages = Collections.synchronizedList(new ArrayList<>());
    private final Set<String> participants = Collections.synchronizedSet(new LinkedHashSet<>());
    private final Set<String> sharedProfileUserIds = Collections.synchronizedSet(new LinkedHashSet<>());
    private final Instant startedAt = Instant.now();
    private Instant endedAt;
    private long sideALastMessageTime = System.currentTimeMillis();
    private long sideBLastMessageTime = System.currentTimeMillis();
    private boolean reported;

    public CallSession(CallEndpoint sideA, CallEndpoint sideB, CallMatchSource source) {
        this(ToolSet.generateUID(), sideA, sideB, source);
    }

    public CallSession(String id, CallEndpoint sideA, CallEndpoint sideB, CallMatchSource source) {
        this.id = id != null ? id : ToolSet.generateUID();
        this.sideA = sideA;
        this.sideB = sideB;
        this.source = source == null ? CallMatchSource.LIVE_QUEUE : source;
    }

    public String getId() {
        return id;
    }

    public CallEndpoint getSideA() {
        return sideA;
    }

    public CallEndpoint getSideB() {
        return sideB;
    }

    public CallMatchSource getSource() {
        return source;
    }

    public String getChannelA() {
        return sideA.channelId();
    }

    public String getChannelB() {
        return sideB.channelId();
    }

    public boolean ownsChannel(String channelId) {
        return getChannelA().equals(channelId) || getChannelB().equals(channelId);
    }

    public boolean isSideA(String channelId) {
        return getChannelA().equals(channelId);
    }

    public String otherChannelId(String channelId) {
        return isSideA(channelId) ? getChannelB() : getChannelA();
    }

    public CallEndpoint endpointForChannel(String channelId) {
        return isSideA(channelId) ? sideA : sideB;
    }

    public CallEndpoint otherEndpoint(String channelId) {
        return isSideA(channelId) ? sideB : sideA;
    }

    public void addMessage(CallMessage message) {
        messages.add(message);
        participants.add(message.authorId());
    }

    public List<CallMessage> getMessages() {
        return messages;
    }

    public Set<String> getParticipants() {
        return participants;
    }

    public void markProfileShared(String userId) {
        sharedProfileUserIds.add(userId);
    }

    public boolean hasSharedProfile(String userId) {
        return sharedProfileUserIds.contains(userId);
    }

    public void end() {
        if (endedAt == null) {
            endedAt = Instant.now();
        }
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public void markReported() {
        reported = true;
    }

    public boolean isReported() {
        return reported;
    }

    public boolean canSend(String channelId, long cooldownMs) {
        long last = isSideA(channelId) ? sideALastMessageTime : sideBLastMessageTime;
        return System.currentTimeMillis() - last >= cooldownMs;
    }

    public void touchMessage(String channelId) {
        long now = System.currentTimeMillis();
        if (isSideA(channelId)) {
            sideALastMessageTime = now;
        } else {
            sideBLastMessageTime = now;
        }
    }

    public long getSideALastMessageTime() {
        return sideALastMessageTime;
    }

    public long getSideBLastMessageTime() {
        return sideBLastMessageTime;
    }

    public String formatTranscript() {
        StringBuilder sb = new StringBuilder();
        sb.append("Call ").append(id).append(" source=").append(source).append('\n');
        sb.append(getChannelA()).append(" <-> ").append(getChannelB()).append('\n');
        for (CallMessage m : messages) {
            sb.append(m.sentAt()).append(" [").append(m.authorName()).append("] ")
                    .append(m.content()).append('\n');
        }
        return sb.toString();
    }
}
