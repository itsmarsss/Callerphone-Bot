package com.itsmarsss.callerphone.match.service;

import java.time.Instant;

public record BrowseSession(
        String sessionId,
        String viewerId,
        String subjectId,
        Instant expiresAt
) {
    public boolean isExpired(Instant now) {
        return expiresAt != null && !expiresAt.isAfter(now);
    }
}
