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
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Remove {
	public static void remove(GuildMessageReceivedEvent event) {
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

		int index;

		try {
			index = Integer.parseInt(MESSAGE.getContentRaw().split("\\s+")[1]);
		}catch(Exception e) {
			MESSAGE.replyEmbeds(Help.help("remove", false)).queue();
			return;
		}

		if(index < 1 || index > musicManager.scheduler.queue.size()) {
			MESSAGE.reply("Index out of bounds!");
			return;
		}

		AudioTrackInfo info = musicManager.scheduler.queue.get(index-1).getInfo();

		String title = info.title;
		String author = info.author;

		musicManager.scheduler.queue.remove(index-1);
		if(index <= musicManager.scheduler.index) {
			musicManager.scheduler.index--;
		}

		if(index == musicManager.scheduler.index+1) {
			musicManager.scheduler.nextTrack();
		}

		MESSAGE.addReaction(Bot.ThumbsUp).queue();
		MESSAGE.reply("Removed index `" + index + "`: `" + title + "` by `" + author + "`").queue();
	}
	public static String getHelp() {
		return "`" + Bot.Prefix + "remove <track index>` - Removes the indexed track.";
	}
}
