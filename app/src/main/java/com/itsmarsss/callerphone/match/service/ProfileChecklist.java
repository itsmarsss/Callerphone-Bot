package com.itsmarsss.callerphone.match.service;

import com.itsmarsss.callerphone.identity.MatchUser;
import com.itsmarsss.callerphone.match.model.MatchProfile;
import com.itsmarsss.callerphone.match.model.ProfileState;

public final class ProfileChecklist {
    private ProfileChecklist() {
    }

    public static String format(MatchUser user, MatchProfile profile) {
        return String.join("\n",
                check(user != null && user.getAgeCohort() != null, "Age group"),
                check(profile != null && profile.getDisplayName() != null && !profile.getDisplayName().isBlank(), "Name"),
                check(profile != null && profile.getBio() != null && !profile.getBio().isBlank(), "Bio"),
                check(profile != null && profile.getInterests() != null && !profile.getInterests().isEmpty(), "Interests"),
                check(profile != null && profile.getState() == ProfileState.ACTIVE, "Live")
        );
    }

    public static boolean readyToSubmit(MatchProfile profile) {
        return profile != null
                && profile.getAgeCohort() != null
                && profile.getDisplayName() != null && !profile.getDisplayName().isBlank()
                && profile.getBio() != null && !profile.getBio().isBlank()
                && profile.getInterests() != null && !profile.getInterests().isEmpty();
    }

    public static int completionPercent(MatchUser user, MatchProfile profile) {
        int score = 0;
        int total = 5;
        if (user != null && user.getAgeCohort() != null) {
            score++;
        }
        if (profile != null && profile.getDisplayName() != null && !profile.getDisplayName().isBlank()) {
            score++;
        }
        if (profile != null && profile.getBio() != null && !profile.getBio().isBlank()) {
            score++;
        }
        if (profile != null && profile.getInterests() != null && !profile.getInterests().isEmpty()) {
            score++;
        }
        if (profile != null && profile.getState() == ProfileState.ACTIVE) {
            score++;
        }
        return (int) Math.round(100.0 * score / total);
    }

    public static String nextStep(MatchUser user, MatchProfile profile) {
        if (user == null || user.getAgeCohort() == null) {
            return "Pick an age group.";
        }
        if (profile == null || profile.getDisplayName() == null || profile.getDisplayName().isBlank()) {
            return "Add a display name.";
        }
        if (profile.getBio() == null || profile.getBio().isBlank()) {
            return "Write a short bio.";
        }
        if (profile.getInterests() == null || profile.getInterests().isEmpty()) {
            return "Add a few interests.";
        }
        if (profile.getState() == ProfileState.PAUSED) {
            return "Resume when you're ready.";
        }
        if (profile.getState() != ProfileState.ACTIVE) {
            return "Go live when you're ready.";
        }
        return "Browse when you want.";
    }

    private static String check(boolean done, String label) {
        return (done ? "✓" : "○") + "  " + label;
    }
}
