package com.itsmarsss.callerphone.tccallerphone.entities;

import java.time.Instant;
import java.util.Arrays;

public final class ChatMessage {
    private final String authorId;
    private final String authorName;
    private final String content;
    private final String channelId;
    private final boolean fromCaller;
    private final String[] flags;
    private final Instant sentAt;

    public ChatMessage(String authorId, String authorName, String content, String channelId,
                       boolean fromCaller, String[] flags, Instant sentAt) {
        this.authorId = authorId;
        this.authorName = authorName;
        this.content = content;
        this.channelId = channelId;
        this.fromCaller = fromCaller;
        this.flags = flags != null ? Arrays.copyOf(flags, flags.length) : new String[0];
        this.sentAt = sentAt != null ? sentAt : Instant.now();
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getContent() {
        return content;
    }

    public String getChannelId() {
        return channelId;
    }

    public boolean isFromCaller() {
        return fromCaller;
    }

    public String[] getFlags() {
        return Arrays.copyOf(flags, flags.length);
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public String format() {
        return (fromCaller ? "Caller " : "Receiver ")
                + (authorName != null ? authorName : "Unknown")
                + "(" + authorId + ")"
                + ": " + content;
    }

    @Override
    public String toString() {
        return format();
    }
}
