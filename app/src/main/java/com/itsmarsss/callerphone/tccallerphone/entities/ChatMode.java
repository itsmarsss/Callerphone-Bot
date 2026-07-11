package com.itsmarsss.callerphone.tccallerphone.entities;

public enum ChatMode {
    DEFAULT,        // Not anonymous, no profanity filter
    ANONYMOUS,      // Anonymous, no profanity filter
    FILTERED_ANON;  // Anonymous + profanity filtered (ffandanon)

    public boolean isAnonymous() {
        return this != DEFAULT;
    }

    public boolean filtersProfanity() {
        return this == FILTERED_ANON;
    }

    public static ChatMode fromSubcommand(String subcommand) {
        if (subcommand == null) {
            return null;
        }
        switch (subcommand) {
            case "default":
                return DEFAULT;
            case "anonymous":
                return ANONYMOUS;
            case "ffandanon":
                return FILTERED_ANON;
            default:
                return null;
        }
    }
}
