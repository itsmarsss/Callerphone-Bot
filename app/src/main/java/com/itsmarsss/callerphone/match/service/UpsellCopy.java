package com.itsmarsss.callerphone.match.service;

/** Contextual Premium copy. Purchases not live — honest about that. */
public final class UpsellCopy {
    private UpsellCopy() {
    }

    public static String forLimit(String limitName) {
        return switch (limitName == null ? "" : limitName) {
            case "discovery" -> "You've hit today's free discoveries ("
                    + MatchLimits.FREE_DAILY_DISCOVERIES + "). Premium will raise this to "
                    + MatchLimits.PREMIUM_DAILY_DISCOVERIES + " — purchases not live yet. Come back tomorrow!";
            case "interest" -> "Daily free interests used ("
                    + MatchLimits.FREE_DAILY_INTERESTS + "). Premium will offer "
                    + MatchLimits.PREMIUM_DAILY_INTERESTS + ". Purchases not enabled yet.";
            case "conversations" -> "Active chat limit reached ("
                    + MatchLimits.FREE_ACTIVE_CONVERSATIONS + "). Unmatch an old chat or wait — Premium will raise this later.";
            case "undo" -> "No undos left today (free: " + MatchLimits.FREE_DAILY_UNDOS
                    + "/day). Premium will include more undos when billing goes live.";
            default -> "Premium raises limits (discoveries, interests, chats, undos). Purchases aren't enabled yet.";
        };
    }

    public static String premiumPitch() {
        return """
                **Callerphone Social Premium** (coming later via Discord)
                • More daily discoveries & interests
                • More active chats
                • More undo-skips
                • Extra profile themes

                Safety, reports, and basic matching stay free. Purchases are not live yet.
                """;
    }
}
