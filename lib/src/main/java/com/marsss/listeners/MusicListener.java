package com.marsss.listeners;

import com.marsss.Bot;
import com.marsss.music.*;
import com.marsss.music.lavaplayer.PlayerManager;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MusicListener extends ListenerAdapter {
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		
		if(!event.getChannel().canTalk())
			return;
		
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
			MESSAGE.reply("`Tip:` Enjoying music commands? Join <https://discord.gg/9uDv5wwXjk> for a 100% free music bot in development!").queue();
			Play.play(event);
			break;
		
		case "playsc":
			MESSAGE.reply("`Tip:` Enjoying music commands? Join <https://discord.gg/9uDv5wwXjk> for a 100% free music bot in development!").queue();
			Playsc.playsc(event);
			break;
			
		case "playlist":
			MESSAGE.reply("`Tip:` Enjoying music commands? Join <https://discord.gg/9uDv5wwXjk> for a 100% free music bot in development!").queue();
			Playlist.playlist(event);
			break;

		case "playlistsc":
			MESSAGE.reply("`Tip:` Enjoying music commands? Join <https://discord.gg/9uDv5wwXjk> for a 100% free music bot in development!").queue();
			Playlistsc.playlistsc(event);
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
			
		case "seek":
			Seek.seek(event);
			break;

		case "volume":
			Volume.volume(event);
			break;

		case "remove":
			Remove.remove(event);
			break;
		
		case "back":
			Back.back(event);
			break;
			
		case "fastforward":
			FastForward.fastforward(event);
			break;
			
		case "rewind":
			Rewind.rewind(event);
			break;
			
		case "shuffle":
			Shuffle.shuffle(event);
			break;
			
		case "jump":
			Jump.jump(event);
			break;
			
		case "announce":
			boolean a = PlayerManager.getInstance().getMusicManager(event.getGuild()).scheduler.announce;
			if(a) {
				PlayerManager.getInstance().getMusicManager(event.getGuild()).scheduler.announce = false;
			}else {
				PlayerManager.getInstance().getMusicManager(event.getGuild()).scheduler.announce = true;
			}
			MESSAGE.reply("Set announce to `" + PlayerManager.getInstance().getMusicManager(event.getGuild()).scheduler.announce + "`").queue();
			break;
		
		case "loop":
			boolean l = PlayerManager.getInstance().getMusicManager(event.getGuild()).scheduler.loop;
			if(l) {
				PlayerManager.getInstance().getMusicManager(event.getGuild()).scheduler.loop = false;
			}else {
				PlayerManager.getInstance().getMusicManager(event.getGuild()).scheduler.loop = true;
			}
			MESSAGE.reply("Set loop to `" + PlayerManager.getInstance().getMusicManager(event.getGuild()).scheduler.loop + "`").queue();
			break;
			
		}

	}
	public static String announceHelp() {
		return "`" + Bot.Prefix + "announce` - Toggle announce playing song.";
	}
	public static String loopHelp() {
		return "`" + Bot.Prefix + "loop` - Toggle looping queue.";
	}
}
