package com.marsss.callerphone.utils;

import com.marsss.callerphone.ToolSet;
import com.marsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.awt.*;
import java.util.Random;

public class Colour implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        switch (e.getSubcommandName()) {
            case "random":
                e.replyEmbeds(colorRandom()).queue();
                break;

            case "hex":
                e.replyEmbeds(colorHex(e.getOption("hex").getAsString())).queue();
                break;


            case "rgb":
                e.replyEmbeds(colorRGB(
                        e.getOption("r").getAsInt(),
                        e.getOption("g").getAsInt(),
                        e.getOption("b").getAsInt())
                ).queue();
                break;
        }
    }

    public static MessageEmbed colorRandom() {
        final Color COLOR = randColor();
        final int R = COLOR.getRed(), G = COLOR.getGreen(), B = COLOR.getBlue();
        final String HEX = String.format("%02X%02X%02X", R, G, B);

        EmbedBuilder colorEmbed = new EmbedBuilder()
                .setTitle("Color")
                .setDescription("**Hex:** #" + HEX + "\n**RGB:** " + R + ", " + G + ", " + B)
                .setColor(COLOR);

        return colorEmbed.build();
    }

    public static MessageEmbed colorHex(String hex) {
        try {
            hex = hex.replaceFirst("#", "");

            final String RGB = Color.decode("#" + hex.replaceFirst("#", "")).toString()
                    .substring(15)
                    .replaceAll("[rgb=]", "")
                    .replaceAll(",", ", ");

            EmbedBuilder colorEmbed = new EmbedBuilder()
                    .setTitle("Color")
                    .setDescription("**Hex:** #" + hex + "\n**RGB:** " + RGB)
                    .setColor(Integer.parseInt(hex, 16));

            return colorEmbed.build();
        } catch (Exception e) {
        }

        return new EmbedBuilder()
                .setTitle("Error")
                .setDescription("Please provide a valid hex value")
                .setColor(ToolSet.COLOR)
                .build();
    }

    public static MessageEmbed colorRGB(int r, int g, int b) {
        final String HEX = String.format("%02X%02X%02X", r, g, b);

        EmbedBuilder colorEmd = new EmbedBuilder()
                .setTitle("Color")
                .setDescription("**Hex:** #" + HEX + "\n**RGB:** " + r + ", " + g + ", " + b)
                .setColor(Integer.parseInt(HEX, 16));

        return colorEmd.build();
    }

    public static Color randColor() {
        final Random RAND = new Random();
        final int r = RAND.nextInt(256), g = RAND.nextInt(256), b = RAND.nextInt(256);
        return new Color(r, g, b);
    }

    @Override
    public String getHelp() {
        return "</colour random:1089656103391985666> - Get a random color in hex and rgb value.\n" +
                "</colour rgb:1089656103391985666> - Get the hex value of rgb.\n" +
                "</colour hex:1089656103391985666> - Get the rgb value of hex.";
    }

    @Override
    public String[] getTriggers() {
        return "color,colour".split(",");
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getTriggers()[0], "Colour's corner [random | hex | rgb]")
                .addSubcommands(
                        new SubcommandData("random", "Random colour"),
                        new SubcommandData("hex", "Hex colour")
                                .addOptions(
                                        new OptionData(OptionType.STRING, "hex", "Hex code")
                                                .setRequired(true)
                                ),
                        new SubcommandData("rgb", "RGB colour")
                                .addOptions(
                                        new OptionData(OptionType.INTEGER, "r", "Red value")
                                                .setRequiredRange(0, 255)
                                                .setRequired(true),
                                        new OptionData(OptionType.INTEGER, "g", "Red value")
                                                .setRequiredRange(0, 255)
                                                .setRequired(true),
                                        new OptionData(OptionType.INTEGER, "b", "Red value")
                                                .setRequiredRange(0, 255)
                                                .setRequired(true)
                                )
                )
                .setGuildOnly(true);
    }
}
