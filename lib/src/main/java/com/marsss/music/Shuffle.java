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

import java.util.Random;

import com.marsss.Bot;
import com.marsss.music.lavaplayer.GuildMusicManager;
import com.marsss.music.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Shuffle {
	public static void shuffle(GuildMessageReceivedEvent event) {
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

		AudioTrack[] array = new AudioTrack[musicManager.scheduler.queue.size()];
		for(int i = 0; i < musicManager.scheduler.queue.size(); i++) {
			array[i] = musicManager.scheduler.queue.get(i);
		}
		shuffleQueue(array);
		musicManager.scheduler.queue.clear();
		for(AudioTrack t : array) {
			musicManager.scheduler.queue.add(t);
		}
		musicManager.scheduler.index = 0;
		MESSAGE.addReaction(Bot.ThumbsUp).queue();
		MESSAGE.reply("Queue shuffled").queue();
		musicManager.scheduler.nextTrack();
	}

	private static void shuffleQueue(AudioTrack[] q) {
		Random rand = new Random();
        for (int i = 0; i < q.length - 1; i++) {
            int index = rand.nextInt(i + 1);
            AudioTrack track = q[index];
            q[index] = q[i];
            q[i] = track;
        }
	}
	public static String getHelp() {
		return "`" + Bot.Prefix + "shuffle` - Shuffles queue and plays from beginning.";
	}
}
