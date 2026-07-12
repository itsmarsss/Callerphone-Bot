package com.itsmarsss.callerphone.discord.match;

import com.itsmarsss.callerphone.match.model.MatchProfile;
import com.itsmarsss.callerphone.match.model.ProfilePrompt;
import com.itsmarsss.callerphone.match.model.ProfileState;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.Color;
import java.util.stream.Collectors;

public final class MatchEmbeds {
    private static final Color BLURPLE = new Color(88, 101, 242);
    private static final Color SUCCESS = new Color(87, 242, 135);
    private static final Color WARM = new Color(255, 183, 77);
    private static final Color SOFT = new Color(155, 163, 255);

    private MatchEmbeds() {
    }

    public static MessageEmbed profileCard(MatchProfile profile, boolean self) {
        String name = displayName(profile.getDisplayName());
        String cohort = profile.getAgeCohort() == null ? "-" : profile.getAgeCohort().label();
        String bio = profile.getBio() == null || profile.getBio().isBlank()
                ? "_No bio yet_"
                : profile.getBio();

        EmbedBuilder emb = new EmbedBuilder()
                .setColor(self ? SOFT : BLURPLE)
                .setTitle(self ? name : "Meet " + name)
                .setDescription(bio);

        if (self) {
            emb.addField("Age group", cohort, true);
            emb.addField("Visibility", prettyState(profile.getState()), true);
        } else {
            emb.addField("Age group", cohort, true);
        }
        if (profile.getPronouns() != null && !profile.getPronouns().isBlank()) {
            emb.addField("Pronouns", profile.getPronouns(), true);
        }
        if (profile.getInterests() != null && !profile.getInterests().isEmpty()) {
            emb.addField(self ? "Interests" : "Interests", String.join(" · ", profile.getInterests()), false);
        }
        if (profile.getPrompts() != null && !profile.getPrompts().isEmpty()) {
            ProfilePrompt prompt = profile.getPrompts().get(0);
            emb.addField("Ideal Sunday", prompt.answer(), false);
        }
        if (profile.getMedia() != null && !profile.getMedia().isEmpty()
                && profile.getMedia().get(0).attachmentUrl() != null) {
            emb.setThumbnail(profile.getMedia().get(0).attachmentUrl());
        }
        emb.setFooter(self ? "Your profile" : null);
        return emb.build();
    }

    public static MessageEmbed simple(String title, String description) {
        return simple(title, description, BLURPLE);
    }

    public static MessageEmbed success(String title, String description) {
        return simple(title, description, SUCCESS);
    }

    public static MessageEmbed soft(String title, String description) {
        return simple(title, description, SOFT);
    }

    public static MessageEmbed warm(String title, String description) {
        return simple(title, description, WARM);
    }

    public static MessageEmbed simple(String title, String description, Color color) {
        EmbedBuilder emb = new EmbedBuilder()
                .setTitle(title)
                .setColor(color);
        if (description != null && !description.isBlank()) {
            emb.setDescription(description);
        }
        return emb.build();
    }

    public static String interestsLine(MatchProfile profile) {
        if (profile.getInterests() == null || profile.getInterests().isEmpty()) {
            return "none";
        }
        return profile.getInterests().stream().collect(Collectors.joining(", "));
    }

    private static String prettyState(ProfileState state) {
        if (state == null) {
            return "-";
        }
        return switch (state) {
            case ACTIVE -> "In Discover";
            case DRAFT -> "Incomplete";
            case PAUSED -> "Paused";
            case PENDING_REVIEW -> "Incomplete";
            case SUSPENDED -> "Paused";
            case DELETED -> "Removed";
        };
    }

    private static String displayName(String value) {
        return value == null || value.isBlank() ? "Someone" : value;
    }
}
