package com.itsmarsss.callerphone.call.discord;

/**
 * Call component ids. Prefix {@code c} routes via first custom-id segment (delimiter {@code -}).
 * Format: c-v1-&lt;action&gt;-&lt;opaque...&gt;
 */
public final class CallComponentIds {
    public static final String HEAD = "c-v1-";
    public static final String SHARE = "share";
    public static final String LIKE = "like";
    public static final String PASS = "pass";
    public static final String REPORT = "report";

    private CallComponentIds() {
    }

    public static String share(String sessionId) {
        return HEAD + SHARE + "-" + sessionId;
    }

    public static String like(String sessionId, String subjectUserId) {
        return HEAD + LIKE + "-" + sessionId + "-" + subjectUserId;
    }

    public static String pass(String sessionId, String subjectUserId) {
        return HEAD + PASS + "-" + sessionId + "-" + subjectUserId;
    }

    public static String report(String sessionId) {
        return HEAD + REPORT + "-" + sessionId;
    }

    public static boolean isCall(String customId) {
        return customId != null && customId.startsWith(HEAD);
    }

    public static Parsed parse(String customId) {
        if (!isCall(customId)) {
            return null;
        }
        String rest = customId.substring(HEAD.length());
        String[] parts = rest.split("-", 3);
        if (parts.length < 2) {
            return null;
        }
        String action = parts[0];
        if (LIKE.equals(action) || PASS.equals(action)) {
            if (parts.length < 3) {
                return null;
            }
            // sessionId may contain hyphens if we used UUID with hyphens - use generateUID without hyphens
            // opaque: sessionId-subjectUserId — subject is last snowflake segment
            String opaque = parts[1] + (parts.length > 2 ? "-" + parts[2] : "");
            int last = opaque.lastIndexOf('-');
            if (last < 0) {
                return new Parsed(action, opaque, null);
            }
            return new Parsed(action, opaque.substring(0, last), opaque.substring(last + 1));
        }
        return new Parsed(action, parts.length > 1 ? rest.substring(action.length() + 1) : "", null);
    }

    public record Parsed(String action, String sessionId, String subjectUserId) {
    }
}
