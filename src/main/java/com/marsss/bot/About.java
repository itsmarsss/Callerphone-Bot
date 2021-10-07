package com.marsss.bot;

import java.awt.Color;

import com.marsss.Bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class About {
	public static MessageEmbed about() {
		JDA jda = Bot.jda;
		String invlink = "\n" + "Join our Community and Support Server [here](https://discord.gg/jcYKsfw48p)"
				+ "\n" + "[invite](https://discord.com/api/oauth2/authorize?client_id=849713468348956692&permissions=8&scope=bot%20applications.commands) me to your server" + "!";

		StringBuilder descr = new StringBuilder()
				.append("Hello! I'm **").append(jda.getSelfUser().getName()).append("**")
				.append("\nType `").append("u?help").append("` to see my commands!")
				.append(invlink).append("\n\nSome of my features include: ```");

		//for (String feature : features)
		//	descr.append("\n").append(REPLACEMENT_ICON).append(" ").append(feature);
		descr.append("to be added later ```");

		descr.append("\n");
		descr.append("Total memory: ").append(Runtime.getRuntime().totalMemory()).append("\n");
		descr.append("Free memory: ").append(Runtime.getRuntime().freeMemory()).append("\n");
		descr.append("Max memory: ").append(Runtime.getRuntime().maxMemory()).append("\n");
		descr.append("Memory Usage: ").append(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());

		jda.getShardInfo();
		EmbedBuilder AbtEmd = new EmbedBuilder()
				.setColor(Color.cyan)
				.setDescription(descr)
				.addField("Stats", (jda.getGuilds().size() + " Servers\nShard " + (jda.getShardInfo().getShardId() + 1)
						+ "/" + jda.getShardInfo().getShardTotal()), true)
				.addField("This shard", jda.getUsers().size() + " Users\n" + jda.getGuilds().size() + " Servers", true)
				.addField("", jda.getTextChannels().size() + " Text Channels\n" + jda.getVoiceChannels().size() + " Voice Channels", true)
				.setFooter("Support me on patreon with https://www.patreon.com/itsmarsss", null);

				return AbtEmd.build();
	}
	
	public static String getHelp() {
		return "`u?about` - Introduces you to this bot";
		
	}
	
}
