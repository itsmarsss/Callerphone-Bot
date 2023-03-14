package com.marsss.callerphone.utils;

import com.marsss.callerphone.Callerphone;
import com.marsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.Random;

public class Colour implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        switch (e.getSubcommandName()) {
            case "random":
                e.replyEmbeds(color()).queue();
                break;

            case "hex":
                e.replyEmbeds(colorhex(e.getOptionsByName("hex").toString())).queue();
                break;


            case "rgb":
                e.replyEmbeds(colorrgb(
                        e.getOptionsByName("r").toString(),
                        e.getOptionsByName("g").toString(),
                        e.getOptionsByName("b").toString())
                ).queue();
                break;

        }
    }

    public static MessageEmbed colorrgb(String r, String g, String b) {

        EmbedBuilder ColorEmd = new EmbedBuilder()
                .setTitle("Error")
                .setDescription("Please provide a valid r g b value")
                .setColor(new Color(114, 137, 218));

        try {
            if (Integer.parseInt(r) > 255 || Integer.parseInt(g) > 255 || Integer.parseInt(b) > 255)
                return ColorEmd.build();

            final String HEX = String.format("%02X%02X%02X",
                    Integer.parseInt(r),
                    Integer.parseInt(g),
                    Integer.parseInt(b));
            EmbedBuilder ColorEmd2 = new EmbedBuilder()
                    .setTitle("Color")
                    .setDescription("**Hex:** #" + HEX + "\n**RGB:** " + r + ", " + g + ", " + b)
                    .setColor(Integer.parseInt(HEX, 16));
            return ColorEmd2.build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ColorEmd.build();

    }

    public static Color randColor() {
        final Random RAND = new Random();
        final int r = RAND.nextInt(256), g = RAND.nextInt(256), b = RAND.nextInt(256);
        return new Color(r, g, b);
    }

    @Override
    public String getHelp() {
        return "`" + Callerphone.config.getPrefix() + "color` - Get a random color in hex and rgb value.\n" +
                "`" + Callerphone.config.getPrefix() + "colorrgb <r> <g> <b>` - Get the hex value of rgb.\n" +
                "`" + Callerphone.config.getPrefix() + "colorhex <hex>` - Get the rgb value of hex.";
    }

    @Override
    public String[] getTriggers() {
        return "color,colorrgb,colorhex".split(",");
    }

    public static MessageEmbed color() {
        final Random RAND = new Random();
        int r = RAND.nextInt(256), g = RAND.nextInt(256), b = RAND.nextInt(256);
        final Color COLOR = new Color(r, g, b);
        final String HEX = String.format("%02X%02X%02X", r, g, b);
        EmbedBuilder ColorEmd = new EmbedBuilder()
                .setTitle("Color")
                .setDescription("**Hex:** #" + HEX + "\n**RGB:** " + r + ", " + g + ", " + b)
                .setColor(COLOR);

        return ColorEmd.build();
    }

    public static MessageEmbed colorhex(String hex) {
        EmbedBuilder ColorEmd = new EmbedBuilder()
                .setTitle("Error")
                .setDescription("Please provide a valid hex value")
                .setColor(new Color(114, 137, 218));

        try {
            hex = hex.replaceFirst("#", "");
            String RGB = Color.decode("#" + hex.replaceFirst("#", "")).toString();
            RGB = RGB.substring(15, RGB.length() - 1)
                    .replaceAll("[rgb=]", "")
                    .replaceAll(",", ", ");
            EmbedBuilder ColorEmd2 = new EmbedBuilder()
                    .setTitle("Color")
                    .setDescription("**Hex:** #" + hex + "\n**RGB:** " + RGB)
                    .setColor(Integer.parseInt(hex, 16));
            return ColorEmd2.build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ColorEmd.build();

    }
}
