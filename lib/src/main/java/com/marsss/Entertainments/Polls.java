package com.marsss.Entertainments;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class Polls {
	static void poll(String args[]) {
		//Nothing to see here

	}

	static MessageEmbed newpoll(String args[]) {
		StringBuilder question = new StringBuilder();
		for(int i = 1; i < args.length; i++) {
			question.append(" ").append(args[i]);
		}

		EmbedBuilder PollEmd = new EmbedBuilder()
				.setTitle("POLL")
				.setColor(0x7289DA)
				.setDescription(question.toString());

		return PollEmd.build();
	}

	static String getHelp() {
		return "`poll <msg>` - Create a poll for server members to vote!";
	}
}
