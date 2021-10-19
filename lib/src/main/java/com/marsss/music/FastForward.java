package com.marsss.music;

import com.marsss.Bot;
import com.marsss.music.lavaplayer.GuildMusicManager;
import com.marsss.music.lavaplayer.PlayerManager;
import com.marsss.utils.Help;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class FastForward {
	public static void fastforward(GuildMessageReceivedEvent event) {
		final Member self = event.getGuild().getSelfMember();
		final GuildVoiceState selfVoiceState = self.getVoiceState();
		final Message MESSAGE = event.getMessage();

		if (!selfVoiceState.inVoiceChannel()) {
			MESSAGE.reply("I need to be in a voice channel for this command to work").queue();
			return;
		}

		final Member member = event.getMember();
		final GuildVoiceState memberVoiceState = member.getVoiceState();

		if (!memberVoiceState.inVoiceChannel()) {
			MESSAGE.reply("You need to be in a voice channel for this command to work").queue();
			return;
		}

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

		long t;
		try {
			t = Math.abs(Long.parseLong(MESSAGE.getContentRaw().split("\\s+")[1]))*1000;
		}catch(Exception e) {
			MESSAGE.replyEmbeds(Help.help("fastforward")).queue();
			return;
		}

		if(audioPlayer.getPlayingTrack().getPosition()+t > audioPlayer.getPlayingTrack().getDuration()) {
			MESSAGE.reply("Too many seconds").queue();
			return;
		}

		audioPlayer.getPlayingTrack().setPosition(audioPlayer.getPlayingTrack().getPosition()+t);
		MESSAGE.addReaction(Bot.ThumbsUp).queue();
		MESSAGE.reply("Fast forwarded `" + t/1000 + "` seconds").queue();
	}
	public static String getHelp() {
		return "`" + Bot.Prefix + "fastforward <seconds>` - Fastforwards a specific number of seconds on current track.";
	}
}
