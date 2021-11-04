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
package com.marsss.music;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import com.marsss.Bot;
import com.marsss.music.lavaplayer.GuildMusicManager;
import com.marsss.music.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Queue {
	public static void queue(GuildMessageReceivedEvent event) {
		final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
		final LinkedList<AudioTrack> queue = musicManager.scheduler.queue;
		final Message MESSAGE = event.getMessage();

		if (queue.isEmpty()) {
			MESSAGE.reply("The queue is currently empty").queue();
			return;
		}

		final StringBuilder message = new StringBuilder("> Current Queue:\n");

		int index = 0;
		
		for (AudioTrack track : queue) {
			final AudioTrackInfo info = track.getInfo();
			
			index++;
			
			message.append("#")
			.append(String.valueOf(index) + ": ")
			.append(" \"")
			.append(String.valueOf(info.title).replace("\"", "'"))
			.append(" by ")
			.append(info.author.replace("\"", "'"))
			.append("\" [")
			.append(formatTime(track.getDuration()))
			.append("]\n");
		}

		MESSAGE.reply(message.toString().getBytes(), "Queue.java").queue();
	}

	private static String formatTime(long millis) {

		return String.format("%02d:%02d:%02d", 
				TimeUnit.MILLISECONDS.toHours(millis),
				TimeUnit.MILLISECONDS.toMinutes(millis) -  
				TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
				TimeUnit.MILLISECONDS.toSeconds(millis) - 
				TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))); 
	}
	public static String getHelp() {
		return "`" + Bot.Prefix + "queue` - Shows audio player queue";
	}
}
