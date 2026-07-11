package com.itsmarsss.callerphone.match.model;

import java.util.Arrays;
import java.util.Optional;

/** Inclusive, enum-backed gender taxonomy. Optional on profiles. */
public enum Gender {
    WOMAN("woman", "Woman"),
    MAN("man", "Man"),
    NON_BINARY("non_binary", "Non-binary"),
    OTHER("other", "Other"),
    PREFER_NOT("prefer_not", "Prefer not to say");

    private final String code;
    private final String label;

    Gender(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String code() {
        return code;
    }

    public String label() {
        return label;
    }

    public static Optional<Gender> fromCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        String normalized = code.trim().toLowerCase().replace('-', '_').replace(' ', '_');
        return Arrays.stream(values())
                .filter(g -> g.code.equalsIgnoreCase(normalized) || g.name().equalsIgnoreCase(normalized))
                .findFirst();
    }
}
