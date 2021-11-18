package com.marsss.utils;

import java.awt.Color;
import java.time.format.DateTimeFormatter;

import com.marsss.Bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class BotInfo {

	public static MessageEmbed botinfo() {
		final JDA jda = Bot.jda;
		final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		final String DESC = "**Tag of the bot:** " + jda.getSelfUser().getAsTag() + 
				"\n**Avatar url:** [link](" + jda.getSelfUser().getAvatarUrl() + ")"+ 
				"\n**Time created:** " + dtf.format(jda.getSelfUser().getTimeCreated()) + 
				"\n**Id:** " + jda.getSelfUser().getId() +
				"\n**Shard info:** [" + (jda.getShardInfo().getShardId() + 1) + "/" + jda.getShardInfo().getShardTotal() + "]" + 
				"\n**Servers:** " + jda.getGuilds().size();

		EmbedBuilder BotInfo = new EmbedBuilder()
				.setColor(Color.cyan)
				.setTitle("**Bot Info**")
				.setDescription(DESC);

		return BotInfo.build();

	}

	public static String getHelp() {
		return "`" + Bot.Prefix + "botinfo` - Get information about the bot.";
	}

}
