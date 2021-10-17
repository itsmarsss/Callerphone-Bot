package com.marss.music;

import com.marss.music.lavaplayer.GuildMusicManager;
import com.marss.music.lavaplayer.PlayerManager;
import com.marsss.Bot;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Leave {
	public static void leave(GuildMessageReceivedEvent event) {
		final Member self = event.getGuild().getSelfMember();
		final GuildVoiceState selfVoiceState = self.getVoiceState();
		final Message MESSAGE = event.getMessage();

		if (!selfVoiceState.inVoiceChannel()) {
			MESSAGE.reply("I need to be in a voice channel for this command to work").queue();
			return;
		}

		final Member member = event.getMember();
		final GuildVoiceState memberVoiceState = member.getVoiceState();

		if (!memberVoiceState.getChannel().equals(selfVoiceState.getChannel())) {
			MESSAGE.reply("You need to be in the same voice channel as me for this command to work").queue();
			return;
		}

		final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
		musicManager.scheduler.queue.clear();
		musicManager.scheduler.index = 0;
		
		event.getGuild().getAudioManager().closeAudioConnection();
		event.getMessage().addReaction(Bot.ThumbsUp).queue();
		event.getMessage().reply("Left Voice channel!").queue();
	}
}
