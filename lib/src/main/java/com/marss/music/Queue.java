package com.marss.music;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import com.marss.music.lavaplayer.GuildMusicManager;
import com.marss.music.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

public class Queue {
	public static void queue(GuildMessageReceivedEvent event) {
		final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
		final LinkedList<AudioTrack> queue = musicManager.scheduler.queue;
		final Message MESSAGE = event.getMessage();

		if (queue.isEmpty()) {
			MESSAGE.reply("The queue is currently empty").queue();
			return;
		}

		final MessageAction messageAction = MESSAGE.reply("**Current Queue:**\n");

		int index = 0;
		
		for (AudioTrack track : queue) {
			final AudioTrackInfo info = track.getInfo();

			index++;
			
			messageAction.append("**#")
			.append(String.valueOf(index) + "**")
			.append(" `")
			.append(String.valueOf(info.title))
			.append(" by ")
			.append(info.author)
			.append("` [`")
			.append(formatTime(track.getDuration()))
			.append("`]\n");
		}

		messageAction.queue();
	}

	private static String formatTime(long timeInMillis) {
		final long hours = timeInMillis / TimeUnit.HOURS.toMillis(1);
		final long minutes = timeInMillis / TimeUnit.MINUTES.toMillis(1);
		final long seconds = timeInMillis % TimeUnit.MINUTES.toMillis(1) / TimeUnit.SECONDS.toMillis(1);

		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}

}
