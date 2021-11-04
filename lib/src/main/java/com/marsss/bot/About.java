/*
 * Copyright 2021 Marsss (itsmarsss).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.marsss.bot;

import java.awt.Color;
import java.lang.management.ManagementFactory;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import com.marsss.Bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class About {
	public static MessageEmbed about() {
		final JDA jda = Bot.jda;

		final StringBuilder desc = new StringBuilder()
				.append("[Invite link](https://discord.com/api/oauth2/authorize?client_id=849713468348956692&permissions=49663040&scope=bot%20applications.commands)")
				.append("\n[Support server](https://discord.gg/jcYKsfw48p)")
				.append("\n[Bot listing (top.gg)](https://top.gg/bot/849713468348956692)")
				.append("\n[Upvote bot (top.gg)](https://top.gg/bot/849713468348956692/vote)")
				.append("\n[Bot listing (dbl)](https://discordbotlist.com/bots/callerphone)")
				.append("\n[Upvote bot (dbl)](https://discordbotlist.com/bots/callerphone/upvote)")
				.append("\n[Upvote support server (top.gg)](https://top.gg/servers/798428155907801089/vote)")
				.append("\n[Upvote support server (dbl)](https://discordbotlist.com/servers/legendary-bot-official-server/upvote)")

				.append("\n");

		jda.getShardInfo();
		long users = 0;
		for(Guild g : jda.getGuilds()) {
			users += g.getMemberCount();
		}
		EmbedBuilder AbtEmd = new EmbedBuilder()
				.setAuthor("by " + jda.getUserById("841028865995964477").getAsTag(), null, jda.getUserById("841028865995964477").getAvatarUrl())
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
						users + " users\n" +
						jda.getUsers().size() + " unique users", true)

				.addField("CPU Usage",
						(String.valueOf(ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage()).startsWith("-")) ? ("Unavailable") : (ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage() + "%") + "\n" +
								ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors() + " processors", true)

				.addField("Memory Usage",
						convert(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + "\n" + 
								convert(Runtime.getRuntime().maxMemory()) + " max\n", true)

				.addField("Uptime",
						Uptime.uptimeabt(), true)
				
				.addField("Info",
						"Made in Java <:Java:899050421572739072>" +
				" with Java Discord Api <:JDA:899083802989695037>", false)

				.setFooter("One of the many bots in the sequel...");


		return AbtEmd.build();
	}

	// https://programming.guide/java/formatting-byte-size-to-human-readable-format.html {

	public static String convert(long bytes) {
		if (-1000 < bytes && bytes < 1000) {
			return bytes + " B";
		}
		final CharacterIterator ci = new StringCharacterIterator("kMGTPE");
		while (bytes <= -999_950 || bytes >= 999_950) {
			bytes /= 1000;
			ci.next();
		}
		return String.format("%.1f %cB", bytes / 1000.0, ci.current());
	}

	// }

	public static String getHelp() {
		return "`" + Bot.Prefix + "about` - Introduces you to this bot";

	}

}
