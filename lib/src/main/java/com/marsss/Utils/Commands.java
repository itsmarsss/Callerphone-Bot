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
		String split = "\n-=-\n";
		EmbedBuilder CmdEmd = new EmbedBuilder()
				.setTitle("**All Commands**")
				.setDescription(
						"**Utils**\n"
								+ Commands.getHelp()
								+ split
								+ Help.getHelp()
								+ split
								+ Ping.getHelp()
								+ "\n\n**Entertainments**\n"
								+ Clap.getHelp()
								+ split
								+ Colour.getHelp()
								+ split
								+ Echo.getHelp()
								+ split
								+ EightBall.getHelp()
								+ split
								+ Polls.getHelp()
								+ split
								+ Read.getHelp()
								+ split
								+ RPS.getHelp()
						);

		return CmdEmd.build();
	}

	public static String getHelp() {
		return "`commands` - Gives you a list of commands";
	}
}
