package com.marsss.Entertainments;

import java.util.Random;
import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class Colour {
	static MessageEmbed colour(String []args){
		if(args.length == 1) {
			Random rand = new Random();
			int r = rand.nextInt(256), g = rand.nextInt(256), b = rand.nextInt(256);
			Color color = new Color(r, g, b);
			String hex = String.format("%02X%02X%02X", r, g, b);
			EmbedBuilder ColorEmd = new EmbedBuilder()
					.setTitle("Color")
					.setDescription("Hex: #" + hex + "\nRGB: " + r + ", " + g + ", " + b)
					.setColor(color);

			return ColorEmd.build();
		}else if(args.length < 4) {
			String rgb = Color.decode("#" + Integer.parseInt(args[1], 16)).toString();
			rgb = rgb.substring(15, rgb.length()-1)
					.replaceAll("[rgb=]", "")
					.replaceAll(",", ", ");
			EmbedBuilder ColorEmd = new EmbedBuilder()
					.setTitle("Color")
					.setDescription("Hex: #" + args[1] + "\nRGB: " + rgb)
					.setColor(Integer.parseInt(args[1], 16));

			return ColorEmd.build();
		}else {
			args[1] = args[1].replaceAll(",", "");
			args[2] = args[2].replaceAll(",", "");
			args[3] = args[3].replaceAll(",", "");
			int r = Integer.parseInt(args[1]);
			int g = Integer.parseInt(args[2]);
			int b = Integer.parseInt(args[3]);
			String hex = String.format("%02X%02X%02X", r, g, b);
			EmbedBuilder ColorEmd = new EmbedBuilder()
					.setTitle("Color")
					.setDescription("Hex: #" + hex + "\nRGB: " + r + ", " + g + ", " + b)
					.setColor(Integer.parseInt(hex, 16));

			return ColorEmd.build();
		}
	}

	static String getHelp() {
		return "`color` = Get a random color in hex and rgb\n" +
				"`color <r><g><b>` = Get the hex value of that color\n" +
				"`color <hex>` = Get the rgb value of that color";
	}
}
