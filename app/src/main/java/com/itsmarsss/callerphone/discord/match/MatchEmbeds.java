package com.itsmarsss.callerphone.discord.match;

import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.match.model.MatchProfile;
import com.itsmarsss.callerphone.match.model.ProfilePrompt;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.Color;
import java.util.stream.Collectors;

public final class MatchEmbeds {
    private static final Color COLOR = new Color(88, 101, 242);

    private MatchEmbeds() {
    }

    public static MessageEmbed profileCard(MatchProfile profile, boolean self) {
        EmbedBuilder emb = new EmbedBuilder()
                .setTitle(self ? "Your Match profile" : "Discover")
                .setColor(COLOR)
                .setDescription(profile.getBio() == null || profile.getBio().isBlank()
                        ? "_No bio yet_"
                        : profile.getBio());
        emb.addField("Name", nullToDash(profile.getDisplayName()), true);
        emb.addField("Age group", profile.getAgeCohort() == null ? "—" : profile.getAgeCohort().label(), true);
        emb.addField("State", profile.getState() == null ? "—" : profile.getState().name(), true);
        if (profile.getGender() != null) {
            emb.addField("Gender", profile.getGender().label(), true);
        }
        if (profile.getPronouns() != null && !profile.getPronouns().isBlank()) {
            emb.addField("Pronouns", profile.getPronouns(), true);
        }
        if (profile.getInterests() != null && !profile.getInterests().isEmpty()) {
            emb.addField("Interests", String.join(", ", profile.getInterests()), false);
        }
        if (profile.getPrompts() != null && !profile.getPrompts().isEmpty()) {
            ProfilePrompt prompt = profile.getPrompts().get(0);
            emb.addField("Prompt", prompt.answer(), false);
        }
        if (profile.getMedia() != null && !profile.getMedia().isEmpty()
                && profile.getMedia().get(0).attachmentUrl() != null) {
            emb.setThumbnail(profile.getMedia().get(0).attachmentUrl());
        }
        emb.setFooter(self ? "Callerphone Social" : "Express interest or skip", null);
        return emb.build();
    }

    public static MessageEmbed simple(String title, String description) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(COLOR)
                .setFooter(ToolSet.CP_EMJ + " Callerphone Social")
                .build();
    }

    public static String interestsLine(MatchProfile profile) {
        if (profile.getInterests() == null || profile.getInterests().isEmpty()) {
            return "none";
        }
        return profile.getInterests().stream().collect(Collectors.joining(", "));
    }

    private static String nullToDash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }
}
