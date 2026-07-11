package com.itsmarsss.callerphone.match.service;

/** Short limit messages. No purchase flow yet. */
public final class UpsellCopy {
    private UpsellCopy() {
    }

    public static String forLimit(String limitName) {
        return switch (limitName == null ? "" : limitName) {
            case "discovery" -> "That's all for today. Come back tomorrow.";
            case "interest" -> "Daily interests used up. See you tomorrow.";
            case "conversations" -> "You're at your chat limit. Unmatch someone to open a slot.";
            case "undo" -> "No undos left today.";
            default -> "Limit reached. Try again later.";
        };
    }

    public static String premiumPitch() {
        return """
                **Premium** coming later
                Higher daily limits
                More open chats
                Extra undos

                Matching stays free either way.
                """;
    }
}
