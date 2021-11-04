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

		if (!memberVoiceState.inVoiceChannel()) {
			MESSAGE.reply("You need to be in a voice channel for this command to work").queue();
			return;
		}

		if (!memberVoiceState.getChannel().equals(selfVoiceState.getChannel())) {
			MESSAGE.reply("You need to be in the same voice channel as me for this command to work").queue();
			return;
		}

		final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
		musicManager.audioPlayer.destroy();
		musicManager.scheduler.queue.clear();
		musicManager.scheduler.index = 0;

		event.getGuild().getAudioManager().closeAudioConnection();
		event.getMessage().addReaction(Bot.ThumbsUp).queue();
		event.getMessage().reply("Left voice channel").queue();
	}
	public static String getHelp() {
		return "`" + Bot.Prefix + "leave` - Leaves voice channel.";
	}
}
