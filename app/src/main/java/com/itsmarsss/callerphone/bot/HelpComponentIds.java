package com.itsmarsss.callerphone.bot;

/**
 * Versioned help navigation component IDs.
 * Format: h-v1-&lt;action&gt;-&lt;opaque&gt;
 */
public final class HelpComponentIds {
    public static final String PREFIX = "h";
    public static final String VERSION = "v1";
    public static final String HEAD = PREFIX + "-" + VERSION + "-";

    public static final String ACTION_CAT = "cat";
    public static final String ACTION_HOME = "home";

    public static final String CAT_MATCH = "match";
    public static final String CAT_CALL = "call";
    public static final String CAT_BOTTLE = "bottle";
    public static final String CAT_GAMES = "games";
    public static final String CAT_BOT = "bot";
    public static final String CAT_CREDS = "creds";

    private HelpComponentIds() {
    }

    public static String category(String name) {
        return HEAD + ACTION_CAT + "-" + name;
    }

    public static String home() {
        return HEAD + ACTION_HOME + "-_";
    }

    public static boolean isHelp(String customId) {
        return customId != null && customId.startsWith(HEAD);
    }

    public static Parsed parse(String customId) {
        if (!isHelp(customId)) {
            return null;
        }
        String rest = customId.substring(HEAD.length());
        int dash = rest.indexOf('-');
        if (dash < 0) {
            return new Parsed(rest, "");
        }
        return new Parsed(rest.substring(0, dash), rest.substring(dash + 1));
    }

    public record Parsed(String action, String opaqueId) {
    }
}
