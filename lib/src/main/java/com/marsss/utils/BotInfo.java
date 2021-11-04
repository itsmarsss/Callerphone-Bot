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
