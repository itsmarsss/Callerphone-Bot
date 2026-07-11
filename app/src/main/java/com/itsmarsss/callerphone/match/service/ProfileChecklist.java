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
        lines.add(check(profile != null && profile.getState() == ProfileState.PENDING_REVIEW, "Submitted for review"));
        lines.add(check(profile != null && profile.getState() == ProfileState.ACTIVE, "Approved & active"));
        return String.join("\n", lines);
    }

    public static boolean readyToSubmit(MatchProfile profile) {
        return profile != null
                && profile.getAgeCohort() != null
                && profile.getDisplayName() != null && !profile.getDisplayName().isBlank()
                && profile.getBio() != null && !profile.getBio().isBlank()
                && profile.getInterests() != null && !profile.getInterests().isEmpty();
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
        if (profile.getState() == ProfileState.DRAFT || profile.getState() == ProfileState.PAUSED) {
            return "Submit with `/match submit` for review.";
        }
        if (profile.getState() == ProfileState.PENDING_REVIEW) {
            return "Waiting on moderator approval. Hang tight!";
        }
        if (profile.getState() == ProfileState.ACTIVE) {
            return "You're live — `/match browse` to discover people.";
        }
        return "Use `/match profile` to see your status.";
    }

    private static String check(boolean done, String label) {
        return (done ? "✅" : "⬜") + " " + label;
    }
}
