package com.marsss.bot;

import java.awt.Color;
import java.lang.management.ManagementFactory;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import com.marsss.Bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class About {
	public static MessageEmbed about() {
		JDA jda = Bot.jda;

		StringBuilder desc = new StringBuilder()
				.append("[Invite link](https://discord.com/api/oauth2/authorize?client_id=849713468348956692&permissions=274914888704&scope=bot%20applications.commands)")
				.append("\n[Support server](https://discord.gg/jcYKsfw48p)")
				.append("\n[Bot listing](https://top.gg/bot/849713468348956692)")
				.append("\n[Upvote bot](https://top.gg/bot/849713468348956692/vote)")
				.append("\n[Upvote support server](https://top.gg/servers/798428155907801089/vote)")

				.append("\n");

		jda.getShardInfo();
		EmbedBuilder AbtEmd = new EmbedBuilder()
				.setAuthor("By " + jda.getUserById("841028865995964477").getAsTag(), null, jda.getUserById("841028865995964477").getAvatarUrl())
				.setColor(Color.cyan)
				.setTitle("**About:**")
				.setDescription(desc)
				.addField("Servers", 
						jda.getGuilds().size() + " servers\n" +
								jda.getShardInfo().getShardTotal() + " shards\n", true)

				.addField("Channels", 
						jda.getTextChannels().size() + jda.getVoiceChannels().size() + " total\n" +
								jda.getTextChannels().size() + " text channels\n" +
								jda.getVoiceChannels().size() + " voice channels", true)

				.addField("Users",
						jda.getUsers().size() + " users", true)

				.addField("CPU Usage",
						(String.valueOf(ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage()).startsWith("-")) ? ("Unavailable") : (ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage() + "%") + "\n" +
								ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors() + " processors", true)

				.addField("Memory Usage",
						convert(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + "\n" + 
								convert(Runtime.getRuntime().maxMemory()) + " max\n", true)

				.addField("Uptime",
						Uptime.uptimeabt(), true)
				
				.addField("Info",
						"Made with Java <:Java:899050421572739072>" +
				"and Java Discord Api <:JDA:899083802989695037>", false)

				.setFooter("One of the many bots in the sequel...");


		return AbtEmd.build();
	}

	// https://programming.guide/java/formatting-byte-size-to-human-readable-format.html {

	public static String convert(long bytes) {
		if (-1000 < bytes && bytes < 1000) {
			return bytes + " B";
		}
		CharacterIterator ci = new StringCharacterIterator("kMGTPE");
		while (bytes <= -999_950 || bytes >= 999_950) {
			bytes /= 1000;
			ci.next();
		}
		return String.format("%.1f %cB", bytes / 1000.0, ci.current());
	}

	// }

	public static String getHelp() {
		return "`u?about` - Introduces you to this bot";

	}

}
