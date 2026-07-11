package com.itsmarsss.callerphone.match.model;

public record ProfilePrompt(String promptId, String answer) {
    public ProfilePrompt {
        if (promptId == null || promptId.isBlank()) {
            throw new IllegalArgumentException("promptId required");
        }
        answer = answer == null ? "" : answer.trim();
    }
}
