package com.itsmarsss.callerphone.identity;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * Self-selected age groups for pairing isolation.
 * Discord presence is treated as 13+; users only choose a cohort for matching.
 */
public enum AgeCohort {
    AGE_13_15("13_15", "13–15"),
    AGE_16_17("16_17", "16–17"),
    AGE_18_PLUS("18_plus", "18+");

    private final String code;
    private final String label;

    AgeCohort(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String code() {
        return code;
    }

    public String label() {
        return label;
    }

    public static Optional<AgeCohort> fromCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        String normalized = code.trim().toLowerCase(Locale.ROOT).replace('-', '_');
        return Arrays.stream(values())
                .filter(c -> c.code.equalsIgnoreCase(normalized) || c.name().equalsIgnoreCase(normalized))
                .findFirst();
    }
}
