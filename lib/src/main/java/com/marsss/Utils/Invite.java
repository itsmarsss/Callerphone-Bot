package com.marsss.Utils;

import java.awt.Color;

import com.marsss.Entertainments.Colour;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class Invite {

	public static MessageEmbed invite() {
		Color COLOR = Colour.randColor();
		EmbedBuilder InvEmd = new EmbedBuilder()
				.setColor(COLOR)
				.addField("Add me to your server", "[Invite](https://discord.com/api/oauth2/authorize?client_id=849713468348956692&permissions=0&scope=bot%20applications.commands)", true)
				.addField("Join the Support Server", "[Server](https://discord.gg/jcYKsfw48p)", true)
				.addField("Support Us", "[Patreon](https://large-type.com/#Got%20No%20Patreon%20At%20The%20Moment%20LOL)", true)
				.setFooter("Have a nice day");
		return InvEmd.build();
	}

	public static String getHelp() {
		return "`invite` - Get an invite for me or to the support server. And more";
	}
	
}
