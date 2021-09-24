package com.marsss.Entertainments;

import java.util.Random;
import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class Colour {
	public static MessageEmbed color(){
		Random rand = new Random();
		int r = rand.nextInt(256), g = rand.nextInt(256), b = rand.nextInt(256);
		Color color = new Color(r, g, b);
		String hex = String.format("%02X%02X%02X", r, g, b);
		EmbedBuilder ColorEmd = new EmbedBuilder()
				.setTitle("Color")
				.setDescription("**Hex:** #" + hex + "\n**RGB:** " + r + ", " + g + ", " + b)
				.setColor(color);

		return ColorEmd.build();
	}

	public static MessageEmbed colorhex(String hex) {
		hex = hex.replaceFirst("#", "");
		String rgb = Color.decode("#" + hex.replaceFirst("#", "")).toString();
		rgb = rgb.substring(15, rgb.length()-1)
				.replaceAll("[rgb=]", "")
				.replaceAll(",", ", ");
		EmbedBuilder ColorEmd = new EmbedBuilder()
				.setTitle("Color")
				.setDescription("**Hex:** #" + hex + "\n**RGB:** " + rgb)
				.setColor(Integer.parseInt(hex, 16));

		return ColorEmd.build();
	}

	public static MessageEmbed colorrgb(String r, String g, String b) {
		String hex = String.format("%02X%02X%02X", 
				Integer.parseInt(r), 
				Integer.parseInt(g), 
				Integer.parseInt(b));
		EmbedBuilder ColorEmd = new EmbedBuilder()
				.setTitle("Color")
				.setDescription("**Hex:** #" + hex + "\n**RGB:** " + r + ", " + g + ", " + b)
				.setColor(Integer.parseInt(hex, 16));

		return ColorEmd.build();
	}
	
	public static Color randColor() {
		Random rand = new Random();
		int r = rand.nextInt(256), g = rand.nextInt(256), b = rand.nextInt(256);
		return new Color(r, g, b);
	}

	public static String getHelp() {
		return "`color` = Get a random color in hex and rgb\n" +
				"`color <r><g><b>` = Get the hex value of that color\n" +
				"`color <hex>` = Get the rgb value of that color";
	}
}
