package com.marsss.bot;

import java.awt.Color;

import com.marsss.entertainments.Colour;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class Invite {

	public static MessageEmbed invite() {
		final Color COLOR = Colour.randColor();
		EmbedBuilder InvEmd = new EmbedBuilder()
				.setColor(COLOR)
				.addField("Add me to your server", "[Invite Link](https://discord.com/api/oauth2/authorize?client_id=849713468348956692&permissions=274914888704&scope=bot%20applications.commands)", true)
				.addField("Join the Community and Support Server", "[Server Link](https://discord.gg/jcYKsfw48p)", true)
				.addField("Support Us", "[Patreon Link](https://www.patreon.com/itsmarsss)", true)
				.setFooter("Have a nice day");
		return InvEmd.build();
	}

	public static String getHelp() {
		return "`u?invite` - Get invites and links related to this bot.";
	}
	
}
