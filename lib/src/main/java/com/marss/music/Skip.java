package com.marss.music;

import com.marss.music.lavaplayer.GuildMusicManager;
import com.marss.music.lavaplayer.PlayerManager;
import com.marsss.Bot;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Skip {
	public static void skip(GuildMessageReceivedEvent event) {
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
		final AudioPlayer audioPlayer = musicManager.audioPlayer;

		if (audioPlayer.getPlayingTrack() == null) {
			MESSAGE.reply("There is no track playing currently").queue();
			return;
		}

		if (musicManager.scheduler.index+1 > musicManager.scheduler.queue.size()) {
			audioPlayer.playTrack(musicManager.scheduler.queue.get(0).makeClone());
			musicManager.scheduler.index = 1;
			MESSAGE.addReaction(Bot.ThumbsUp).queue();
			MESSAGE.reply("The next track is empty, so I'm playing the first track in queue!").queue();
			return;
		}
		
		audioPlayer.playTrack(musicManager.scheduler.queue.get(musicManager.scheduler.index).makeClone());
		musicManager.scheduler.index++;
		System.out.println(musicManager.scheduler.index);

		MESSAGE.addReaction(Bot.ThumbsUp).queue();
		MESSAGE.reply("Skipped the current track").queue();
	}

}
