package com.itsmarsss.callerphone.match.service;

public final class EmptyStates {
    private EmptyStates() {
    }

    public static String noCandidates() {
        return "No more profiles right now in your age group.\n"
                + "• Check back later — new people join often\n"
                + "• Skips expire after " + MatchLimits.SKIP_EXPIRE_DAYS + " days\n"
                + "• Complete your profile to rank better: `/match profile`";
    }

    public static String notActive() {
        return "Your profile isn't live yet.\n"
                + "Finish basics → bio → interests, then `/match submit` to go live.";
    }

    public static String notEnrolled() {
        return "Join Match first with `/match join` and pick your age group.";
    }

    public static String restricted() {
        return "Your Match access is restricted. Contact support if this looks wrong.";
    }

    public static String noChats() {
        return "No connections yet.\n"
                + "Browse with `/match browse`, express interest, and wait for a mutual match.";
    }

    public static String noLikes() {
        return "No incoming interest yet.\n"
                + "Stay active in discovery — people who liked you show up here.";
    }
}
