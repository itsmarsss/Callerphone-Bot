package com.itsmarsss.callerphone.match.service;

public final class EmptyStates {
    private EmptyStates() {
    }

    public static String noCandidates() {
        return "No one new right now. Check back later.";
    }

    public static String notActive() {
        return "Finish your profile first. Try `/match join`.";
    }

    public static String notEnrolled() {
        return "Start with `/match join`.";
    }

    public static String restricted() {
        return "Match is paused on your account.";
    }

    public static String noChats() {
        return "No chats yet. Browse and say interested when you vibe.";
    }

    public static String noLikes() {
        return "No likes yet. Keep browsing.";
    }
}
