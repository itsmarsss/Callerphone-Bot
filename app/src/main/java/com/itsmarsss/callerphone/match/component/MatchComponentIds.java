package com.itsmarsss.callerphone.match.component;

/**
 * Versioned opaque component IDs. Uses {@code -} to match bot routing.
 * Format: m-v1-&lt;action&gt;-&lt;opaqueId&gt; (opaque may contain hyphens)
 */
public final class MatchComponentIds {
    public static final String PREFIX = "m";
    public static final String VERSION = "v1";
    public static final String HEAD = PREFIX + "-" + VERSION + "-";

    public static final String ACTION_INTERESTED = "interested";
    public static final String ACTION_SKIP = "skip";
    public static final String ACTION_CONNECT_ACCEPT = "connect_accept";
    public static final String ACTION_CONNECT_DECLINE = "connect_decline";
    public static final String ACTION_JOIN_ACCEPT = "join_accept";
    public static final String ACTION_AGE_13_15 = "age_13_15";
    public static final String ACTION_AGE_16_17 = "age_16_17";
    public static final String ACTION_AGE_18_PLUS = "age_18_plus";
    public static final String ACTION_CHAT_SELECT = "chat_select";
    public static final String ACTION_BROWSE_NEXT = "browse_next";
    public static final String ACTION_EDIT_BASICS = "edit_basics";
    public static final String ACTION_EDIT_BIO = "edit_bio";
    public static final String ACTION_EDIT_INTERESTS = "edit_interests";
    public static final String ACTION_CONNECT_REQUEST = "connect_req";
    public static final String ACTION_UNMATCH = "unmatch";
    public static final String ACTION_STOP_CHAT = "stop_chat";
    public static final String ACTION_SUBMIT = "submit";


    private MatchComponentIds() {
    }

    public static String of(String action, String opaqueId) {
        return HEAD + action + "-" + (opaqueId == null || opaqueId.isBlank() ? "_" : opaqueId);
    }

    public static boolean isMatch(String customId) {
        return customId != null && customId.startsWith(HEAD);
    }

    public static Parsed parse(String customId) {
        if (!isMatch(customId)) {
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
