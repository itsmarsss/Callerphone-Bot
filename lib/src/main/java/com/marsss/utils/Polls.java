package com.marsss.utils;

import java.awt.Color;

import com.marsss.entertainments.Colour;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class Polls {
	public static MessageEmbed newpoll(String qst) {
		Color COLOR = Colour.randColor();
		EmbedBuilder PollEmd = new EmbedBuilder()
				.setTitle("**POLL**")
				.setColor(COLOR)
				.setDescription(qst.toString());

		return PollEmd.build();
	}

	public static String getHelp() {
		return "`poll <msg>` - Create a poll for server members to vote!";
	}
}
