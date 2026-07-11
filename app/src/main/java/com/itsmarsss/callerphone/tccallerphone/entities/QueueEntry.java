package com.itsmarsss.callerphone.tccallerphone.entities;

import java.time.Duration;
import java.time.Instant;

public final class QueueEntry {
    private final String channelId;
    private final ChatMode mode;
    private final Instant queuedAt;

    public QueueEntry(String channelId, ChatMode mode) {
        this.channelId = channelId;
        this.mode = mode;
        this.queuedAt = Instant.now();
    }

    public String getChannelId() {
        return channelId;
    }

    public ChatMode getMode() {
        return mode;
    }

    public Instant getQueuedAt() {
        return queuedAt;
    }

    public boolean isExpired(Duration timeout) {
        return Instant.now().isAfter(queuedAt.plus(timeout));
    }
}
