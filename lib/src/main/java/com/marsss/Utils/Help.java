package com.marsss.Utils;

import java.awt.Color;
import java.util.Random;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import com.marsss.Entertainments.*;

public class Help {
	public static MessageEmbed help(String name) {
		String desc = "I don't recognize that command :(";
		name = name.toLowerCase();
		switch(name) {
		case "clap":
			desc = Clap.getHelp();
			break;
		case "color":
			desc = Colour.getHelp();
			break;
		case "echo":
			desc = Echo.getHelp();
			break;
		case "eightball":
			desc = EightBall.getHelp();
			break;
		case "poll":
			desc = Polls.getHelp();
			break;
		case "read":
			desc = Read.getHelp();
			break;
		case "rps":
			desc = RPS.getHelp();
			break;
		case "ping":
			desc = Ping.getHelp();
			break;
			case "help":
				desc = Help.getHelp();
				break;
		}
		
		Random rand = new Random();
		int r = rand.nextInt(256), g = rand.nextInt(256), b = rand.nextInt(256);
		Color color = new Color(r, g, b);
		EmbedBuilder HelpEmd = new EmbedBuilder()
				.setTitle("Help is here!")
				.setDescription(desc)
				.setFooter("Hope you found this useful!")
				.setColor(color);

		return HelpEmd.build();
	}
	
	public static String getHelp() {
		return "`help` - help help help";
	}
}
