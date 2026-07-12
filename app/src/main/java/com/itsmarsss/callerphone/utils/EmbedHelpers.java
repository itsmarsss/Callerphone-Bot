package com.itsmarsss.callerphone.utils;

import com.itsmarsss.callerphone.ToolSet;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.attribute.ICategorizableChannel;

import java.awt.Color;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.EnumSet;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Shared embed helpers for info commands.
 */
public final class EmbedHelpers {
    public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.RFC_1123_DATE_TIME;
    private static final int FIELD_MAX = 1024;

    private EmbedHelpers() {
    }

    public static EmbedBuilder base() {
        return new EmbedBuilder().setColor(ToolSet.COLOR);
    }

    public static String orDefault(String value, String fallback) {
        return value == null || value.isEmpty() ? fallback : value;
    }

    public static String parentCategory(ICategorizableChannel channel) {
        return channel.getParentCategory() != null
                ? channel.getParentCategory().getAsMention()
                : "Server";
    }

    public static String joinPermissions(EnumSet<Permission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return "No permissions.";
        }
        return truncate(permissions.stream()
                .map(Permission::getName)
                .collect(Collectors.joining(", ")), FIELD_MAX);
    }

    public static String joinMentions(Collection<? extends IMentionable> items, String emptyLabel) {
        if (items == null || items.isEmpty()) {
            return emptyLabel;
        }
        String joined = items.stream().map(IMentionable::getAsMention).collect(Collectors.joining(", "));
        if (joined.length() <= FIELD_MAX) {
            return joined;
        }
        // Keep as many whole mentions as fit
        String truncated = joined.substring(0, FIELD_MAX - 20);
        int lastComma = truncated.lastIndexOf(',');
        if (lastComma > 0) {
            truncated = truncated.substring(0, lastComma);
        }
        int shown = truncated.split("@").length - 1;
        int remaining = Math.max(0, items.size() - Math.max(shown, 0));
        return truncated + " `+ " + remaining + " more`";
    }

    public static String truncate(String value, int max) {
        if (value == null) {
            return "";
        }
        if (value.length() <= max) {
            return value;
        }
        return value.substring(0, max - 3) + "...";
    }

    public static Color roleColor(Role role) {
        return role != null && role.getColor() != null ? role.getColor() : ToolSet.COLOR;
    }

    public static MessageEmbed error(String message) {
        return base().setTitle("Error").setDescription(message).build();
    }

    public static Color randColor() {
        return new Color(
                ThreadLocalRandom.current().nextInt(256),
                ThreadLocalRandom.current().nextInt(256),
                ThreadLocalRandom.current().nextInt(256)
        );
    }
}
