package com.itsmarsss.callerphone.msginbottle;

/**
 * Versioned bottle component IDs.
 * Format: b-v1-&lt;action&gt;-&lt;opaque&gt;
 */
public final class BottleComponentIds {
    public static final String PREFIX = "b";
    public static final String VERSION = "v1";
    public static final String HEAD = PREFIX + "-" + VERSION + "-";

    public static final String ACTION_THREAD_MENU = "th_menu";
    public static final String ACTION_SAVED_MENU = "sv_menu";
    public static final String ACTION_OPEN = "open";

    private BottleComponentIds() {
    }

    public static String of(String action, String opaqueId) {
        return HEAD + action + "-" + (opaqueId == null || opaqueId.isBlank() ? "_" : opaqueId);
    }

    public static boolean isBottle(String customId) {
        return customId != null && customId.startsWith(HEAD);
    }

    public static Parsed parse(String customId) {
        if (!isBottle(customId)) {
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
