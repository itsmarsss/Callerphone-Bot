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
		this.loop = true;
		this.index = 0;
		this.player = player;
		this.queue = new LinkedList<>();
	}

	public void queue(AudioTrack track) {
		this.queue.add(track);
	}

	public void nextTrack() {
		if(index == this.queue.size()+1 && loop) {
			index = 0;
		}else {
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