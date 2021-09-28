package com.marsss.utils;

import com.marsss.bot.*;
import com.marsss.entertainments.*;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class Commands {
	public static MessageEmbed commands() {
		String SPLIT = "\n—\n";
		EmbedBuilder CmdEmd = new EmbedBuilder()
				.setTitle("**All Commands**")
				.addField("**Utils**",
						Commands.getHelp()
						+ SPLIT
						+ Help.getHelp()
						+ SPLIT
						+ Ping.getHelp()
						+ SPLIT
						+ Uptime.getHelp()
						+ SPLIT
						+ UserInfo.getHelp()
						+ SPLIT
						+ BotInfo.getHelp(), true)
				.addField("**Entertainments**",
						Clap.getHelp()
						+ SPLIT
						+ Colour.getHelp()
						+ SPLIT
						+ Echo.getHelp()
						+ SPLIT
						+ EightBall.getHelp()
						+ SPLIT
						+ Polls.getHelp(), true
						);

		return CmdEmd.build();
	}

	public static String getHelp() {
		return "`commands` - Gives you a list of commands";
	}
}
