package com.itsmarsss.callerphone.safety;

import java.util.Arrays;
import java.util.Optional;

public enum ReportCategory {
    HARASSMENT("harassment", "Harassment / bullying", false),
    SPAM("spam", "Spam or scam", false),
    INAPPROPRIATE("inappropriate", "Inappropriate content", false),
    AGE_MISREPRESENTATION("age_lie", "Age misrepresentation", true),
    GROOMING("grooming", "Grooming / exploitation", true),
    THREATS("threats", "Threats or violence", true),
    CONTACT_EXCHANGE("contact", "Unwanted contact exchange", true),
    IMPERSONATION("impersonation", "Impersonation", false),
    OTHER("other", "Other", false);

    private final String code;
    private final String label;
    private final boolean urgent;

    ReportCategory(String code, String label, boolean urgent) {
        this.code = code;
        this.label = label;
        this.urgent = urgent;
    }

    public String code() {
        return code;
    }

    public String label() {
        return label;
    }

    public boolean urgent() {
        return urgent;
    }

    public static Optional<ReportCategory> from(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String n = raw.trim().toLowerCase().replace('-', '_').replace(' ', '_');
        return Arrays.stream(values())
                .filter(c -> c.code.equalsIgnoreCase(n) || c.name().equalsIgnoreCase(n))
                .findFirst();
    }
}
