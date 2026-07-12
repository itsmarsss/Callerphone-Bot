package com.itsmarsss.callerphone.experience;

/**
 * Discord platform limits. Presenters describe intent; the renderer clamps and validates
 * every user-facing string and component against these bounds.
 */
public final class DiscordLimits {
    public static final int EMBED_TITLE = 256;
    public static final int EMBED_DESCRIPTION = 4096;
    public static final int EMBED_FIELD_NAME = 256;
    public static final int EMBED_FIELD_VALUE = 1024;
    public static final int EMBED_FOOTER = 2048;
    public static final int EMBED_FIELDS = 25;
    public static final int BUTTON_LABEL = 80;
    public static final int CUSTOM_ID = 100;
    public static final int ACTION_ROWS = 5;
    public static final int BUTTONS_PER_ROW = 5;
    public static final int SELECT_OPTIONS = 25;
    public static final int SELECT_PLACEHOLDER = 150;
    public static final int MODAL_TITLE = 45;
    public static final int MODAL_LABEL = 45;
    public static final int MODAL_FIELDS = 5;
    public static final int MODAL_INPUT_SHORT = 4000;
    public static final int MODAL_INPUT_PARAGRAPH = 4000;

    private DiscordLimits() {
    }

    public static String clamp(String value, int max) {
        if (value == null) {
            return null;
        }
        if (value.length() <= max) {
            return value;
        }
        if (max <= 1) {
            return value.substring(0, max);
        }
        return value.substring(0, max - 1) + "…";
    }

    public static boolean isValidCustomId(String id) {
        return id != null && !id.isBlank() && id.length() <= CUSTOM_ID;
    }

    public static boolean isValidModalLabel(String label) {
        return label != null && !label.isBlank() && label.length() <= MODAL_LABEL;
    }

    public static boolean isValidButtonLabel(String label) {
        return label != null && !label.isBlank() && label.length() <= BUTTON_LABEL;
    }
}
