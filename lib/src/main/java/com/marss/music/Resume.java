package com.marss.music;

import com.marss.music.lavaplayer.GuildMusicManager;
import com.marss.music.lavaplayer.PlayerManager;
import com.marsss.Bot;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Resume {
	public static void resume(GuildMessageReceivedEvent event) {

		final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());

		musicManager.scheduler.player.setPaused(false);

		event.getMessage().addReaction(Bot.ThumbsUp).queue();
	}
}
