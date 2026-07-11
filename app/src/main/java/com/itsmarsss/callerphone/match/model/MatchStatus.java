package com.itsmarsss.callerphone.match.model;

import java.util.Arrays;
import java.util.Optional;

public enum MatchStatus {
    ACTIVE,
    UNMATCHED,
    BLOCKED,
    ARCHIVED;

    public static Optional<MatchStatus> from(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(s -> s.name().equalsIgnoreCase(raw.trim()))
                .findFirst();
    }
}
