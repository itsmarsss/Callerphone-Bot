package com.itsmarsss.callerphone.match.model;

import java.time.Instant;

public record MatchMessage(
        String messageId,
        String conversationId,
        String senderId,
        String recipientId,
        String content,
        Instant createdAt,
        String deliveryStatus
) {
    public MatchMessage {
        createdAt = createdAt == null ? Instant.now() : createdAt;
        deliveryStatus = deliveryStatus == null ? "sent" : deliveryStatus;
        content = content == null ? "" : content;
    }
}
