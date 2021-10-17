package com.marsss.listeners;

import com.marsss.Bot;
import com.marsss.music.*;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MusicListener extends ListenerAdapter {
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		final Message MESSAGE = event.getMessage();
		final String CONTENT = MESSAGE.getContentRaw().toLowerCase();

		if(!CONTENT.toLowerCase().startsWith(Bot.Prefix) || MESSAGE.getAuthor().isBot())
			return;
		

		final String args[] = CONTENT.toLowerCase().split("\\s+");
		switch (args[0].toLowerCase().replace(Bot.Prefix, "")) {

		case "clear":
			Clear.clear(event);
			break;

		case "leave":
			Leave.leave(event);
			break;
			
		case "join":
			Join.join(event);
			break;

		case "play":
			Play.play(event);
			break;

		case "pause":
			Pause.pause(event);
			break;

		case "resume":
			Resume.resume(event);
			break;

		case "nowplaying":
			NowPlaying.nowplaying(event);
			break;

		case "queue":
			Queue.queue(event);
			break;

		case "skip":
			Skip.skip(event);
			break;
			
		case "setmarker":
			Marker.marker(event);
			break;

		case "setvolume":
			Volume.volume(event);
			break;

		case "remove":
			Remove.remove(event);
			break;
		
		case "back":
			Back.back(event);
			break;
		}

	}
}