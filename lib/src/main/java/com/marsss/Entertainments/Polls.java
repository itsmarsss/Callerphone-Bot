package com.marsss.Entertainments;

import java.awt.Color;
import java.util.Random;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class Polls {
	static void poll(String args[]) {
		//Nothing to see here

	}

	public static MessageEmbed newpoll(String qst) {
		Random rand = new Random();
		int r = rand.nextInt(256), g = rand.nextInt(256), b = rand.nextInt(256);
		Color color = new Color(r, g, b);
		EmbedBuilder PollEmd = new EmbedBuilder()
				.setTitle("**POLL**")
				.setColor(color)
				.setDescription(qst.toString());

		return PollEmd.build();
	}

	static String getHelp() {
		return "`poll <msg>` - Create a poll for server members to vote!";
	}
}
