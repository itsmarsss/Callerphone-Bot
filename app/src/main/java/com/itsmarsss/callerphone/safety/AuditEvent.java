package com.itsmarsss.callerphone.safety;

import java.time.Instant;
import java.util.UUID;

public record AuditEvent(
        String id,
        String actorId,
        String action,
        String targetId,
        String product,
        String details,
        Instant createdAt
) {
    public static AuditEvent of(String actorId, String action, String targetId, String product, String details) {
        return new AuditEvent(
                UUID.randomUUID().toString(),
                actorId,
                action,
                targetId == null ? "" : targetId,
                product == null ? "" : product,
                details == null ? "" : details,
                Instant.now()
        );
    }
}
