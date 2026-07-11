package com.itsmarsss.callerphone.match.model;

import java.util.Arrays;
import java.util.Optional;

public enum DecisionType {
    INTERESTED,
    SKIP;

    public static Optional<DecisionType> from(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(d -> d.name().equalsIgnoreCase(raw.trim()))
                .findFirst();
    }
}
