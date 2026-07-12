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
    /** Opens the one-shot setup modal (guided onboarding). */
    public static final String ACTION_SETUP = "setup";
    /** Start browsing after going live. */
    public static final String ACTION_START_BROWSE = "start_browse";
    /** Open chats list from home / empty states. */
    public static final String ACTION_OPEN_CHATS = "open_chats";
    /**
     * Contextual safety menu. Opaque: {@code profile:{userId}} or {@code conversation:{id}}.
     */
    public static final String ACTION_SAFETY_OPEN = "safety_open";
    public static final String ACTION_SAFETY_BLOCK = "safety_block";
    public static final String ACTION_SAFETY_REPORT = "safety_report";
    public static final String ACTION_SAFETY_UNMATCH = "safety_unmatch";
    /** String select: pick a chat. Values are conversation ids. */
    public static final String ACTION_CHAT_MENU = "chat_menu";
    /** String select: report category. Opaque is safety context. Values are category codes. */
    public static final String ACTION_REPORT_CAT = "report_cat";
    public static final String ACTION_LEAVE_CONFIRM = "leave_ok";
    public static final String ACTION_LEAVE_CANCEL = "leave_no";
    public static final String ACTION_DELETE_CONFIRM = "del_ok";
    public static final String ACTION_DELETE_CANCEL = "del_no";
    public static final String ACTION_EDIT_MENU = "edit_menu";
    public static final String ACTION_PREVIEW_SELF = "preview_self";
    public static final String ACTION_SETTINGS = "settings";
    public static final String ACTION_OPEN_LIKES = "open_likes";
    public static final String ACTION_RESUME = "resume";
    public static final String ACTION_HOME = "home";
    public static final String ACTION_GAME_TTT = "game_ttt";
    public static final String ACTION_GAME_ACCEPT = "game_ok";
    public static final String ACTION_GAME_DECLINE = "game_no";
    /** Bottle author interest from a found bottle. Opaque: authorUserId. */
    public static final String ACTION_BOTTLE_INTEREST = "bot_int";

    private MatchComponentIds() {
    }

    public static SafetyContext parseSafetyContext(String opaque) {
        if (opaque == null || opaque.isBlank() || "_".equals(opaque)) {
            return null;
        }
        int colon = opaque.indexOf(':');
        if (colon <= 0 || colon >= opaque.length() - 1) {
            return null;
        }
        String kind = opaque.substring(0, colon);
        String id = opaque.substring(colon + 1);
        if (id.isBlank()) {
            return null;
        }
        return switch (kind) {
            case "profile" -> new SafetyContext(SafetyKind.PROFILE, id, id);
            case "conversation" -> new SafetyContext(SafetyKind.CONVERSATION, id, null);
            default -> null;
        };
    }

    public enum SafetyKind {
        PROFILE,
        CONVERSATION
    }

    public record SafetyContext(SafetyKind kind, String referenceId, String subjectUserId) {
        public String opaque() {
            return switch (kind) {
                case PROFILE -> "profile:" + referenceId;
                case CONVERSATION -> "conversation:" + referenceId;
            };
        }
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
