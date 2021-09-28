package com.marsss.utils;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import com.marsss.bot.Ping;
import com.marsss.entertainments.*;

public class Help {
	public static MessageEmbed help(String name) {
		String DESC = "I don't recognize that command :(";
		name = name.toLowerCase();
		switch(name) {
		case "clap":
			DESC = Clap.getHelp();
			break;
		case "color":
			DESC = Colour.getHelp();
			break;
		case "echo":
			DESC = Echo.getHelp();
			break;
		case "eightball":
			DESC = EightBall.getHelp();
			break;
		case "poll":
			DESC = Polls.getHelp();
			break;
		case "ping":
			DESC = Ping.getHelp();
			break;
			case "help":
				DESC = Help.getHelp();
				break;
		}
		
		Color COLOR = Colour.randColor();
		EmbedBuilder HelpEmd = new EmbedBuilder()
				.setTitle("Help is here!")
				.setDescription(DESC)
				.setFooter("Hope you found this useful!")
				.setColor(COLOR);

		return HelpEmd.build();
	}
	
	public static String getHelp() {
		return "`help` - help help help";
	}
}
