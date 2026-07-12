package com.itsmarsss.callerphone.experience;

import java.awt.Color;

/** Intent → color tokens. Keep feature-specific colors out of command handlers. */
public final class ExperienceTheme {
    private static final Color SOCIAL = new Color(88, 101, 242);
    private static final Color SUCCESS = new Color(87, 242, 135);
    private static final Color PROGRESS = new Color(59, 130, 246);
    private static final Color DISCOVERY = new Color(255, 183, 77);
    private static final Color PREMIUM = new Color(234, 179, 8);
    private static final Color WARNING = new Color(251, 146, 60);
    private static final Color ERROR = new Color(239, 68, 68);
    private static final Color SAFETY = new Color(100, 116, 139);
    private static final Color NEUTRAL = new Color(148, 163, 184);

    private ExperienceTheme() {
    }

    public static Color color(ExperienceIntent intent) {
        if (intent == null) {
            return NEUTRAL;
        }
        return switch (intent) {
            case SOCIAL -> SOCIAL;
            case SUCCESS -> SUCCESS;
            case PROGRESS -> PROGRESS;
            case DISCOVERY -> DISCOVERY;
            case PREMIUM -> PREMIUM;
            case WARNING -> WARNING;
            case ERROR -> ERROR;
            case SAFETY -> SAFETY;
            case NEUTRAL -> NEUTRAL;
        };
    }
}
