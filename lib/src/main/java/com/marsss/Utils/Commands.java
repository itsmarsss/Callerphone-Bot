package com.marsss.Utils;

import com.marsss.Entertainments.Clap;
import com.marsss.Entertainments.Colour;
import com.marsss.Entertainments.Echo;
import com.marsss.Entertainments.EightBall;
import com.marsss.Entertainments.Polls;
import com.marsss.Entertainments.RPS;
import com.marsss.Entertainments.Read;

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
						+ Polls.getHelp()
						+ SPLIT
						+ Read.getHelp()
						+ SPLIT
						+ RPS.getHelp(), true
						);

		return CmdEmd.build();
	}

	public static String getHelp() {
		return "`commands` - Gives you a list of commands";
	}
}
