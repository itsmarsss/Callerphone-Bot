package com.itsmarsss.callerphone.utils;

import com.itsmarsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.awt.Color;
import java.util.concurrent.ThreadLocalRandom;

public class Colour implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        String sub = e.getSubcommandName();
        if (sub == null) {
            e.replyEmbeds(EmbedHelpers.error("Missing subcommand.")).setEphemeral(true).queue();
            return;
        }

        switch (sub) {
            case "random":
                e.replyEmbeds(colorRandom()).queue();
                break;
            case "hex":
                OptionMapping hexOpt = e.getOption("hex");
                e.replyEmbeds(colorHex(hexOpt != null ? hexOpt.getAsString() : "")).queue();
                break;
            case "rgb":
                e.replyEmbeds(colorRGB(
                        optInt(e, "r", 0),
                        optInt(e, "g", 0),
                        optInt(e, "b", 0)
                )).queue();
                break;
            default:
                e.replyEmbeds(EmbedHelpers.error("Unknown subcommand.")).setEphemeral(true).queue();
        }
    }

    private static int optInt(SlashCommandInteractionEvent e, String name, int fallback) {
        OptionMapping opt = e.getOption(name);
        return opt != null ? opt.getAsInt() : fallback;
    }

    public static MessageEmbed colorRandom() {
        return colorEmbed(randColor());
    }

    public static MessageEmbed colorHex(String hex) {
        try {
            String cleaned = hex == null ? "" : hex.replace("#", "").trim();
            if (cleaned.length() != 6) {
                throw new IllegalArgumentException("bad hex");
            }
            Color color = Color.decode("#" + cleaned);
            return colorEmbed(color);
        } catch (Exception e) {
            return EmbedHelpers.error("Please provide a valid hex value (e.g. `#FF00AA`).");
        }
    }

    public static MessageEmbed colorRGB(int r, int g, int b) {
        r = clamp(r);
        g = clamp(g);
        b = clamp(b);
        return colorEmbed(new Color(r, g, b));
    }

    private static MessageEmbed colorEmbed(Color color) {
        int r = color.getRed(), g = color.getGreen(), b = color.getBlue();
        String hex = String.format("%02X%02X%02X", r, g, b);
        return new EmbedBuilder()
                .setTitle("Color")
                .setDescription("**Hex:** #" + hex + "\n**RGB:** " + r + ", " + g + ", " + b)
                .setColor(color)
                .build();
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    public static Color randColor() {
        return new Color(
                ThreadLocalRandom.current().nextInt(256),
                ThreadLocalRandom.current().nextInt(256),
                ThreadLocalRandom.current().nextInt(256)
        );
    }

    @Override
    public String getHelp() {
        return "</colour random:1089656103391985666> - Get a random color in hex and rgb value.\n" +
                "</colour rgb:1089656103391985666> - Get the hex value of rgb.\n" +
                "</colour hex:1089656103391985666> - Get the rgb value of hex.";
    }

    @Override
    public String getName() {
        return "color";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Colour tools [random | hex | rgb]")
                .addSubcommands(
                        new SubcommandData("random", "Random colour"),
                        new SubcommandData("hex", "Hex colour")
                                .addOptions(new OptionData(OptionType.STRING, "hex", "Hex code").setRequired(true)),
                        new SubcommandData("rgb", "RGB colour")
                                .addOptions(
                                        new OptionData(OptionType.INTEGER, "r", "Red value").setRequiredRange(0, 255).setRequired(true),
                                        new OptionData(OptionType.INTEGER, "g", "Green value").setRequiredRange(0, 255).setRequired(true),
                                        new OptionData(OptionType.INTEGER, "b", "Blue value").setRequiredRange(0, 255).setRequired(true)
                                )
                )
                .setContexts(InteractionContextType.GUILD);
    }
}
