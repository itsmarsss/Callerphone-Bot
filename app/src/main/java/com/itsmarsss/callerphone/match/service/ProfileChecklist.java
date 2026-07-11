package com.itsmarsss.callerphone.match.service;

import com.itsmarsss.callerphone.identity.MatchUser;
import com.itsmarsss.callerphone.match.model.MatchProfile;
import com.itsmarsss.callerphone.match.model.ProfileState;

import java.util.ArrayList;
import java.util.List;

public final class ProfileChecklist {
    private ProfileChecklist() {
    }

    public static String format(MatchUser user, MatchProfile profile) {
        List<String> lines = new ArrayList<>();
        lines.add(check(user != null && user.getTermsVersionAccepted() != null && !user.getTermsVersionAccepted().isBlank(),
                "Accepted terms & privacy"));
        lines.add(check(user != null && user.getAgeCohort() != null, "Chose age group"));
        lines.add(check(profile != null && profile.getDisplayName() != null && !profile.getDisplayName().isBlank(),
                "Display name"));
        lines.add(check(profile != null && profile.getBio() != null && !profile.getBio().isBlank(), "Bio"));
        lines.add(check(profile != null && profile.getPrompts() != null && !profile.getPrompts().isEmpty(), "Prompt"));
        lines.add(check(profile != null && profile.getInterests() != null && !profile.getInterests().isEmpty(), "Interests"));
        lines.add(check(profile != null && profile.getState() == ProfileState.ACTIVE, "Live in discovery"));
        return String.join("\n", lines);
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
        int total = 7;
        if (user != null && user.getTermsVersionAccepted() != null && !user.getTermsVersionAccepted().isBlank()) {
            score++;
        }
        if (user != null && user.getAgeCohort() != null) {
            score++;
        }
        if (profile != null && profile.getDisplayName() != null && !profile.getDisplayName().isBlank()) {
            score++;
        }
        if (profile != null && profile.getBio() != null && !profile.getBio().isBlank()) {
            score++;
        }
        if (profile != null && profile.getPrompts() != null && !profile.getPrompts().isEmpty()) {
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
        if (user == null || user.getTermsVersionAccepted() == null || user.getTermsVersionAccepted().isBlank()) {
            return "Use `/match join` and accept the notices.";
        }
        if (user.getAgeCohort() == null) {
            return "Choose your age group from the join buttons.";
        }
        if (profile == null || profile.getDisplayName() == null || profile.getDisplayName().isBlank()) {
            return "Set basics with `/match edit field:basics`.";
        }
        if (profile.getBio() == null || profile.getBio().isBlank()) {
            return "Add a bio with `/match edit field:bio`.";
        }
        if (profile.getInterests() == null || profile.getInterests().isEmpty()) {
            return "Pick interests with `/match edit field:interests`.";
        }
        if (profile.getState() == ProfileState.PAUSED) {
            return "Profile paused — `/match resume` to reappear in discovery.";
        }
        if (profile.getState() != ProfileState.ACTIVE) {
            return "Go live with `/match submit` — no approval wait, you can browse right after.";
        }
        return "You're live — `/match browse` to discover people.";
    }

    private static String check(boolean done, String label) {
        return (done ? "✅" : "⬜") + " " + label;
    }
}
