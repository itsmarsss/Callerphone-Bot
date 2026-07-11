package com.itsmarsss.callerphone.identity;

import java.time.Instant;
import java.util.UUID;

public record ConsentEvent(
        String id,
        String userId,
        ConsentType type,
        String version,
        boolean accepted,
        Instant createdAt,
        String metadata
) {
    public static ConsentEvent of(String userId, ConsentType type, String version, boolean accepted, String metadata) {
        return new ConsentEvent(
                UUID.randomUUID().toString(),
                userId,
                type,
                version == null ? "" : version,
                accepted,
                Instant.now(),
                metadata == null ? "" : metadata
        );
    }
}
