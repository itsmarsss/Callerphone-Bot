package com.marsss.music;

import com.marsss.Bot;
import com.marsss.music.lavaplayer.GuildMusicManager;
import com.marsss.music.lavaplayer.PlayerManager;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Resume {
	public static void resume(GuildMessageReceivedEvent event) {

		final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());

		musicManager.scheduler.player.setPaused(false);

		event.getMessage().addReaction(Bot.ThumbsUp).queue();
	}
}
