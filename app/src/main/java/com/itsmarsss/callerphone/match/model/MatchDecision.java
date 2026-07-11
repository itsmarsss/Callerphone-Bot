package com.itsmarsss.callerphone.match.model;

import java.time.Instant;

public record MatchDecision(
        String viewerId,
        String subjectId,
        DecisionType decision,
        Instant createdAt,
        Instant expiresAt
) {
    public MatchDecision {
        if (viewerId == null || viewerId.isBlank()) {
            throw new IllegalArgumentException("viewerId required");
        }
        if (subjectId == null || subjectId.isBlank()) {
            throw new IllegalArgumentException("subjectId required");
        }
        if (decision == null) {
            throw new IllegalArgumentException("decision required");
        }
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    public boolean isExpired(Instant now) {
        return expiresAt != null && !expiresAt.isAfter(now);
    }
}
