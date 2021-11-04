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
package com.marsss.music.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import net.dv8tion.jda.api.entities.TextChannel;

import java.util.LinkedList;

public class TrackScheduler extends AudioEventAdapter {
	public TextChannel channel;
	public boolean announce;
	public boolean loop;
	public int index;
	public final AudioPlayer player;
	public final LinkedList<AudioTrack> queue;

	public TrackScheduler(AudioPlayer player) {
		this.announce = true;
		this.loop = false;
		this.index = 0;
		this.player = player;
		this.queue = new LinkedList<>();
	}

	public void queue(AudioTrack track) {
		this.queue.add(track);
	}

	public void nextTrack() {
		if(index == this.queue.size() && loop) {
			index = 0;
		}else if(index < this.queue.size()){
			this.player.startTrack(this.queue.get(index).makeClone(), false);
			index++;
			if(this.announce) {
				final AudioTrackInfo info = this.player.getPlayingTrack().getInfo();
				channel.sendMessageFormat("Now playing `%s` by `%s` *(Link: <%s>)*", info.title, info.author, info.uri).queue();
			}
		}
	}

	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		if (endReason.mayStartNext) {
			nextTrack();
		}
	}
}
