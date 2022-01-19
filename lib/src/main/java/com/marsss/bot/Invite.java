package com.marsss.bot;

import java.awt.Color;

import com.marsss.Bot;
import com.marsss.entertainments.Colour;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class Invite {

	public static MessageEmbed invite() {
		final Color COLOR = Colour.randColor();
		EmbedBuilder InvEmd = new EmbedBuilder()
				.setColor(COLOR)
				.addField("Add me to your server", "[Invite Link](" + Bot.invite + ")", true)
				.addField("Join the Community and Support Server", "[Server Link](" + Bot.support + ")", true)
				.addField("Support Us", "[Patreon Link](" + Bot.donate + ")", true)
				.setFooter("Have a nice day");
		return InvEmd.build();
	}

	public static String getHelp() {
		return "`" + Bot.Prefix + "invite` - Get invites and links related to this bot.";
	}
	
}
