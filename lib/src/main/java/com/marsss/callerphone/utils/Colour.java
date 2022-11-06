package com.marsss.entertainments;

import java.util.Random;

import com.marsss.callerphone.Bot;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class Colour {
	public static MessageEmbed color(){
		final Random rand = new Random();
		int r = rand.nextInt(256), g = rand.nextInt(256), b = rand.nextInt(256);
		final Color COLOR = new Color(r, g, b);
		final String hex = String.format("%02X%02X%02X", r, g, b);
		EmbedBuilder ColorEmd = new EmbedBuilder()
				.setTitle("Color")
				.setDescription("**Hex:** #" + hex + "\n**RGB:** " + r + ", " + g + ", " + b)
				.setColor(COLOR);

		return ColorEmd.build();
	}

	public static MessageEmbed colorhex(String hex) {
		EmbedBuilder ColorEmd = new EmbedBuilder()
				.setTitle("Error")
				.setDescription("Please provide a valid hex value")
				.setColor(Color.cyan);
		
		try {
			hex = hex.replaceFirst("#", "");
			String RGB = Color.decode("#" + hex.replaceFirst("#", "")).toString();
			RGB = RGB.substring(15, RGB.length()-1)
					.replaceAll("[rgb=]", "")
					.replaceAll(",", ", ");
			EmbedBuilder ColorEmd2 = new EmbedBuilder()
					.setTitle("Color")
					.setDescription("**Hex:** #" + hex + "\n**RGB:** " + RGB)
					.setColor(Integer.parseInt(hex, 16));
			return ColorEmd2.build();
		}catch (Exception e) {}
		return ColorEmd.build();

	}

	public static MessageEmbed colorrgb(String r, String g, String b) {

		EmbedBuilder ColorEmd = new EmbedBuilder()
				.setTitle("Error")
				.setDescription("Please provide a valid r g b value")
				.setColor(Color.cyan);

		try {
			if(Integer.parseInt(r) > 255 || Integer.parseInt(g) > 255 || Integer.parseInt(b) > 255)
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
		}catch(Exception e) {}

		return ColorEmd.build();

	}

	public static Color randColor() {
		final Random rand = new Random();
		final int r = rand.nextInt(256), g = rand.nextInt(256), b = rand.nextInt(256);
		return new Color(r, g, b);
	}

	public static String getHelp() {
		return "`" + Bot.Prefix + "color` - Get a random color in hex and rgb value.\n" +
				"`" + Bot.Prefix + "colorrgb <r> <g> <b>` - Get the hex value of rgb.\n" +
				"`" + Bot.Prefix + "colorhex <hex>` - Get the rgb value of hex.";
	}
}
