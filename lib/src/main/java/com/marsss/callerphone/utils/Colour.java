package com.marsss.callerphone.utils;

import java.util.Random;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.guild.MessageReceivedEvent;

public class Colour implements ICommand {


    @Override
    public void runCommand(MessageReceivedEvent e) {
        final Message MESSAGE = e.getMessage();
        final String CONTENT = MESSAGE.getContentRaw();
        final String[] ARGS = CONTENT.split("\\s+");

        switch (ARGS[0].toLowerCase().replace(Callerphone.config.getPrefix(), "")) {


            case "color":
                MESSAGE.replyEmbeds(color()).queue();
                break;


            case "colorhex":
                if (ARGS.length < 2) {
                    MESSAGE.reply("Please provide hex value").queue();
                }
                MESSAGE.replyEmbeds(colorhex(ARGS[1])).queue();
                break;


            case "colorrgb":
                if (ARGS.length < 4) {
                    MESSAGE.reply("Please provide r g b values").queue();
                    break;
                }
                MESSAGE.replyEmbeds(colorrgb(ARGS[1], ARGS[2], ARGS[3])).queue();
                break;

        }
    }

    @Override
    public void runSlash(SlashCommandInteractionEvent event) {

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
