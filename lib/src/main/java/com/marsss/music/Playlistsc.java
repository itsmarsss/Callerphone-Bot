/*
 * Copyright 2021 Marsss (itsmarsss).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

public class Playlistsc {
	public static void playlistsc(GuildMessageReceivedEvent event) {
		
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

		CONTENT = CONTENT.replace(Bot.Prefix + "playlistsc ", "");

		if (!isUrl(CONTENT)) {
			CONTENT = "scsearch:" + CONTENT;
		}
		
		MESSAGE.addReaction("🔎").queue();
		MESSAGE.addReaction("☁️").queue();
		PlayerManager.getInstance().loadAndPlay(MESSAGE, CONTENT, false);
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
		return "`" + Bot.Prefix + "playlistsc <query/link>` - Searches for a playlist with the query on SoundCloud and plays it.";
	}
}
