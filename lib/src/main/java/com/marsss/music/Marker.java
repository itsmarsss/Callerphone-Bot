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

import com.marsss.Bot;
import com.marsss.music.lavaplayer.GuildMusicManager;
import com.marsss.music.lavaplayer.PlayerManager;
import com.marsss.utils.Help;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Marker {
	public static void marker(GuildMessageReceivedEvent event) {
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
					MESSAGE.replyEmbeds(Help.help("setmarker")).queue();
					return;
					
				}
			}
		}catch(Exception e) {
			MESSAGE.replyEmbeds(Help.help("setmarker")).queue();
			return;
		}

		long position = hour*3600000 + minute*60000 + second*1000;

		if(position == 0) {
			position = 1;
		}else if(position >= musicManager.audioPlayer.getPlayingTrack().getDuration()) {
			position = musicManager.audioPlayer.getPlayingTrack().getDuration()-1;
		}

		audioPlayer.getPlayingTrack().setPosition(position);
		MESSAGE.addReaction(Bot.ThumbsUp).queue();
		MESSAGE.reply("Set marker to `" + event.getMessage().getContentRaw().toLowerCase().split("\\s+")[1] + "`").queue();


	}
}
