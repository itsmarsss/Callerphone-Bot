package com.itsmarsss.callerphone.match.service;

public final class EmptyStates {
    private EmptyStates() {
    }

    public static String noCandidates() {
        return "You've seen everyone available right now. New and refreshed profiles appear later.";
    }

    public static String notActive() {
        return "Finish your profile first so you can appear in Discover.";
    }

    public static String notEnrolled() {
        return "Create a profile to start discovering people.";
    }

    public static String restricted() {
        return "Discover is paused on your account.";
    }

    public static String noChats() {
        return "When you and someone are both interested, your chat appears here.";
    }

    public static String noLikes() {
        return "No new interest yet. Your profile can still appear in Discover.";
    }
}
