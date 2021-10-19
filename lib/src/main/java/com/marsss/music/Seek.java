package com.marsss.music;

import java.util.concurrent.TimeUnit;

import com.marsss.Bot;
import com.marsss.music.lavaplayer.GuildMusicManager;
import com.marsss.music.lavaplayer.PlayerManager;
import com.marsss.utils.Help;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Seek {
	public static void seek(GuildMessageReceivedEvent event) {
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

		String []time = event.getMessage().getContentRaw().toLowerCase().split("\\s+")[1].split(":");
		// 1h2m3s123ms

		int hour = 0;
		int minute = 0;
		int second = 0;

		int count = -1;
		try {
			for(int i = time.length-1; i > -1; i--) {
				count++;
				switch (count) {

				case 0:
					second = Integer.parseInt(time[i]);
					continue;
				case 1:
					minute = Integer.parseInt(time[i]);
					continue;
				case 2:
					hour = Integer.parseInt(time[i]);
					continue;
				default:
					MESSAGE.replyEmbeds(Help.help("seek")).queue();
					return;
					
				}
			}
		}catch(Exception e) {
			MESSAGE.replyEmbeds(Help.help("seek")).queue();
			return;
		}

		long position = hour*3600000 + minute*60000 + second*1000;

		if(position == 0) {
			position = 1;
		}else if(position >= musicManager.audioPlayer.getPlayingTrack().getDuration()) {
			MESSAGE.reply("Position `" + formatTime(position) + "` does not exist").queue();
			return;
		}

		audioPlayer.getPlayingTrack().setPosition(position);
		MESSAGE.addReaction(Bot.ThumbsUp).queue();
		MESSAGE.reply("Set position to `" + event.getMessage().getContentRaw().toLowerCase().split("\\s+")[1] + "`").queue();
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
		return "`" + Bot.Prefix + "seek <h:m:s>` - Change the position where you want to play the track.";
	}
}
