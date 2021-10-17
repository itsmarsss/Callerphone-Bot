package com.marsss.listeners;

import com.marss.music.*;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MusicListener extends ListenerAdapter {
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		Message MESSAGE = event.getMessage();
		String CONTENT = MESSAGE.getContentRaw().toLowerCase();

		if(!CONTENT.toLowerCase().startsWith("u?") || MESSAGE.getAuthor().isBot())
			return;
		

		String args[] = CONTENT.toLowerCase().split("\\s+");

		switch (args[0].toLowerCase()) {

		case "u?clear":
			Clear.clear(event);
			break;

		case "u?leave":
			Leave.leave(event);
			break;
			
		case "u?join":
			Join.join(event);
			break;

		case "u?play":
			Play.play(event);
			break;

		case "u?pause":
			Pause.pause(event);
			break;

		case "u?resume":
			Resume.resume(event);
			break;

		case "u?nowplaying":
			NowPlaying.nowplaying(event);
			break;

		case "u?queue":
			Queue.queue(event);
			break;

		case "u?skip":
			Skip.skip(event);
			break;
			
		case "u?setmarker":
			Marker.marker(event);
			break;

		case"u?setvolume":
			Volume.volume(event);
			break;

		case"u?remove":
			Remove.remove(event);
			break;
		
		case"u?back":
			Back.back(event);
			break;
		}

	}
}