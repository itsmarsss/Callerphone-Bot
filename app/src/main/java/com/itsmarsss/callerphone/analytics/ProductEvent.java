package com.itsmarsss.callerphone.analytics;

import java.time.Instant;
import java.util.UUID;

public record ProductEvent(
        String id,
        String userId,
        String name,
        String metadata,
        Instant createdAt
) {
    public static ProductEvent of(String userId, String name, String metadata) {
        return new ProductEvent(
                UUID.randomUUID().toString(),
                userId == null ? "" : userId,
                name,
                metadata == null ? "" : metadata,
                Instant.now()
        );
    }
}
