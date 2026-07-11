package com.itsmarsss.callerphone.match.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class ProfileValidator {
    private static final Pattern URL = Pattern.compile(
            "(?i)(https?://|www\\.|discord\\.gg/|discord\\.com/invite/)");
    private static final Pattern CONTACT = Pattern.compile(
            "(?i)(\\b\\d{3}[-.\\s]?\\d{3}[-.\\s]?\\d{4}\\b|@\\w{3,}|\\b\\w+@\\w+\\.\\w+\\b|snap(chat)?|instagram|onlyfans|telegram)");
    private static final int MAX_BIO = 300;
    private static final int MAX_NAME = 32;
    private static final int MAX_PROMPT = 200;
    private static final int MAX_INTERESTS = 5;

    private ProfileValidator() {
    }

    public static List<String> validateDisplayName(String name) {
        List<String> errors = new ArrayList<>();
        if (name == null || name.isBlank()) {
            errors.add("Display name is required.");
            return errors;
        }
        if (name.length() > MAX_NAME) {
            errors.add("Display name must be " + MAX_NAME + " characters or fewer.");
        }
        if (containsContactOrUrl(name)) {
            errors.add("Display name cannot include contact info or links.");
        }
        return errors;
    }

    public static List<String> validateBio(String bio) {
        List<String> errors = new ArrayList<>();
        if (bio == null) {
            bio = "";
        }
        if (bio.length() > MAX_BIO) {
            errors.add("Bio must be " + MAX_BIO + " characters or fewer.");
        }
        if (containsContactOrUrl(bio)) {
            errors.add("Bio cannot include contact info or links.");
        }
        return errors;
    }

    public static List<String> validatePromptAnswer(String answer) {
        List<String> errors = new ArrayList<>();
        if (answer == null || answer.isBlank()) {
            errors.add("Prompt answer is required.");
            return errors;
        }
        if (answer.length() > MAX_PROMPT) {
            errors.add("Prompt answer must be " + MAX_PROMPT + " characters or fewer.");
        }
        if (containsContactOrUrl(answer)) {
            errors.add("Prompt cannot include contact info or links.");
        }
        return errors;
    }

    public static List<String> validateInterests(List<String> interests) {
        List<String> errors = new ArrayList<>();
        if (interests == null || interests.isEmpty()) {
            errors.add("Pick at least one interest.");
            return errors;
        }
        if (interests.size() > MAX_INTERESTS) {
            errors.add("Pick up to " + MAX_INTERESTS + " interests.");
        }
        for (String interest : interests) {
            if (interest == null || interest.isBlank() || interest.length() > 24) {
                errors.add("Each interest must be 1–24 characters.");
                break;
            }
        }
        return errors;
    }

    public static boolean containsContactOrUrl(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String lower = text.toLowerCase(Locale.ROOT);
        return URL.matcher(lower).find() || CONTACT.matcher(lower).find();
    }
}
