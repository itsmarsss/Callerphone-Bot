package com.itsmarsss.callerphone.match.model;

import java.util.Arrays;
import java.util.Optional;

public enum ProfileState {
    DRAFT,
    PENDING_REVIEW,
    ACTIVE,
    PAUSED,
    SUSPENDED,
    DELETED;

    public static Optional<ProfileState> from(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(s -> s.name().equalsIgnoreCase(raw.trim()))
                .findFirst();
    }

    public boolean isBrowsable() {
        return this == ACTIVE;
    }
}
