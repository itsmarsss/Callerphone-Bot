package com.itsmarsss.callerphone.experience;

public final class ProgressBar {
    private ProgressBar() {
    }

    public static String of(int current, int max, int width) {
        if (width < 1) {
            width = 10;
        }
        if (max <= 0) {
            return "░".repeat(width);
        }
        int clamped = Math.max(0, Math.min(current, max));
        int filled = (int) Math.round((clamped / (double) max) * width);
        filled = Math.max(0, Math.min(width, filled));
        return "█".repeat(filled) + "░".repeat(width - filled);
    }
}
