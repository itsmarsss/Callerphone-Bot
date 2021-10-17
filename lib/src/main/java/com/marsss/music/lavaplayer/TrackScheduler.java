package com.marsss.music.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.LinkedList;

public class TrackScheduler extends AudioEventAdapter {
	public int index;
	public final AudioPlayer player;
	public final LinkedList<AudioTrack> queue;

	public TrackScheduler(AudioPlayer player) {
		this.index = 0;
		this.player = player;
		this.queue = new LinkedList<>();
	}

	public void queue(AudioTrack track) {
		this.queue.add(track);
	}

	public void nextTrack() {
		this.player.startTrack(this.queue.get(index), false);
		index++;
	}
	
	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		if (endReason.mayStartNext) {
			nextTrack();
		}
	}
}