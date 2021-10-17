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

public class Jump {
	public static void jump(GuildMessageReceivedEvent event) {
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
		int index;
		try{
			index = Integer.parseInt(MESSAGE.getContentRaw().split("\\s+")[1]);
		}catch(Exception e) {
			MESSAGE.replyEmbeds(Help.help("jump")).queue();
			return;
		}
		
		final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
		final AudioPlayer audioPlayer = musicManager.audioPlayer;

		if(index < 1 || index >= musicManager.scheduler.queue.size()) {
			MESSAGE.reply("Index out of bounds!").queue();
			return;
		}
		audioPlayer.playTrack(musicManager.scheduler.queue.get(0));
		MESSAGE.addReaction(Bot.ThumbsUp).queue();
		MESSAGE.reply("Playing track `" + index + "`").queue();

	}
}
