package com.itsmarsss.callerphone.match.service;

import com.itsmarsss.callerphone.match.model.MatchProfile;
import com.itsmarsss.callerphone.match.model.ProfilePrompt;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class Icebreakers {
    private static final List<String> FALLBACK = List.of(
            "What's something fun you've been into lately?",
            "If we hung out for an hour online, what would we do?",
            "What's a comfort show, game, or song on repeat for you?",
            "What's one hobby you'd love to talk about more?"
    );

    private Icebreakers() {
    }

    public static String forPair(MatchProfile self, MatchProfile other) {
        List<String> options = new ArrayList<>();
        if (self != null && other != null && self.getInterests() != null && other.getInterests() != null) {
            for (String interest : self.getInterests()) {
                if (other.getInterests().contains(interest)) {
                    options.add("You both like **" + interest + "** — what got you into it?");
                }
            }
        }
        if (other != null && other.getPrompts() != null && !other.getPrompts().isEmpty()) {
            ProfilePrompt prompt = other.getPrompts().get(0);
            if (prompt.answer() != null && !prompt.answer().isBlank()) {
                options.add("You wrote about an ideal Sunday — mine is… what's yours like in more detail?");
            }
        }
        if (other != null && other.getBio() != null && other.getBio().length() > 20) {
            options.add("Your bio stood out — what's one thing people usually get wrong about you?");
        }
        if (options.isEmpty()) {
            options.addAll(FALLBACK);
        }
        return options.get(ThreadLocalRandom.current().nextInt(options.size()));
    }
}
