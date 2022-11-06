package com.marsss.callerphone.utils;

import java.awt.Color;

import com.marsss.callerphone.Bot;
import com.marsss.entertainments.Colour;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class Polls {
	public static MessageEmbed newpoll(String qst) {
		final Color COLOR = Colour.randColor();
		EmbedBuilder PollEmd = new EmbedBuilder()
				.setTitle("**POLL**")
				.setColor(COLOR)
				.setDescription(qst.toString());

		return PollEmd.build();
	}

	public static String getHelp() {
		return "`" + Bot.Prefix + "poll <msg>` - Create a poll for server members to vote.";
	}
}
