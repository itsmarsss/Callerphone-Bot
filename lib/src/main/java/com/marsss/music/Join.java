package com.marsss.music;

import com.marsss.Bot;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;

public class Join {
	public static void join(GuildMessageReceivedEvent event) {
		final Member self = event.getGuild().getSelfMember();
		final GuildVoiceState selfVoiceState = self.getVoiceState();
		final Message MESSAGE = event.getMessage();

		if (selfVoiceState.inVoiceChannel()) {
			MESSAGE.reply("I'm already in a voice channel").queue();
			return;
		}

		final Member member = event.getMember();
		final GuildVoiceState memberVoiceState = member.getVoiceState();

		if (!memberVoiceState.inVoiceChannel()) {
			MESSAGE.reply("You need to be in a voice channel for me to join").queue();
			return;
		}

		final AudioManager audioManager = event.getGuild().getAudioManager();
		final VoiceChannel memberChannel = memberVoiceState.getChannel();

		audioManager.openAudioConnection(memberChannel);
		audioManager.setSelfDeafened(true);
		MESSAGE.addReaction(Bot.ThumbsUp).queue();
		MESSAGE.reply("Connected to " + memberChannel.getAsMention()).queue();
	}
	public static String getHelp() {
		return "`" + Bot.Prefix + "join` - Joins voice channel.";
	}
}
