package com.marsss.music;

import java.net.URI;
import java.net.URISyntaxException;

import com.marsss.Bot;
import com.marsss.listeners.VCCallerphoneListener;
import com.marsss.music.lavaplayer.PlayerManager;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Play {
	public static void play(GuildMessageReceivedEvent event) {
		
		if(VCCallerphoneListener.hasCall(event.getGuild().getId())) {
			event.getMessage().reply("You cannot play music during a voice call").queue();
			return;
		}
		
		final Member self = event.getGuild().getSelfMember();
		final GuildVoiceState selfVoiceState = self.getVoiceState();
		final Message MESSAGE = event.getMessage();
		String CONTENT = MESSAGE.getContentRaw();

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

		CONTENT = CONTENT.replace(Bot.Prefix + "play ", "");

		if (!isUrl(CONTENT)) {
			CONTENT = "scsearch:" + CONTENT;
		}
		
		MESSAGE.addReaction("🔎").queue();
		PlayerManager.getInstance().loadAndPlay(MESSAGE, CONTENT, true);
	}

	private static boolean isUrl(String url) {
		try {
			new URI(url);
			return true;
		} catch (URISyntaxException e) {
			return false;
		}
	}
	
	public static String getHelp() {
		return "`" + Bot.Prefix + "play <query>` - Searches for a video with the query and plays it.";
	}
}
