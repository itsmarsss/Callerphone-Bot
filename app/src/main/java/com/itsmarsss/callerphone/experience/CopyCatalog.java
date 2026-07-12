package com.itsmarsss.callerphone.experience;

/** Stable microcopy shared across surfaces. Prefer presenters for contextual composition. */
public final class CopyCatalog {
    private CopyCatalog() {
    }

    public static String expiredAction() {
        return "That action expired. Open a fresh screen to continue.";
    }

    public static String startingUp() {
        return "Still starting up. Try again shortly.";
    }

    public static String tryAgain() {
        return "Try again";
    }

    public static String nothingLost() {
        return "Nothing was used.";
    }

    public static String loadingDiscover() {
        return "Looking for someone…";
    }

    public static String loadingCall() {
        return "Finding a call…";
    }
}
