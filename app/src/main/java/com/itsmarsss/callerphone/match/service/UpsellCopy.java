package com.itsmarsss.callerphone.match.service;

/** Limit messages. Premium purchase is not live; keep copy soft. */
public final class UpsellCopy {
    private UpsellCopy() {
    }

    public static String forLimit(String limitName) {
        return switch (limitName == null ? "" : limitName) {
            case "discovery" -> "Today's discoveries are done. Your free set refreshes tomorrow.";
            case "interest" -> "Daily interests used up. See you tomorrow.";
            case "conversations" -> "Chat limit reached. Unmatch someone to free a slot.";
            case "undo" -> "Nothing left to undo today.";
            default -> "Limit reached. Try again later.";
        };
    }

    public static String premiumPitch() {
        return """
                Premium is planned for later.

                Higher daily Discover limits, more open chats, and extra undos.

                Discover stays free either way.
                """;
    }
}
