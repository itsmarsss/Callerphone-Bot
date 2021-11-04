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

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerManager {
	private static PlayerManager INSTANCE;

	public final Map<Long, GuildMusicManager> musicManagers;

	private final AudioPlayerManager audioPlayerManager;

	public PlayerManager() {
		this.musicManagers = new HashMap<>();
		this.audioPlayerManager = new DefaultAudioPlayerManager();

		AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
		AudioSourceManagers.registerLocalSource(this.audioPlayerManager);
	}

	public void resetHandler(Guild guild) {
		final GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager);

		guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());

		musicManagers.put(guild.getIdLong(), guildMusicManager);
	}

	public GuildMusicManager getMusicManager(Guild guild) {
		return this.musicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
			final GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager);

			guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());

			return guildMusicManager;
		});
	}

	public void loadAndPlay(Message message, String trackUrl, boolean first) {
		final GuildMusicManager musicManager = this.getMusicManager(message.getGuild());

		this.audioPlayerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
			@Override
			public void trackLoaded(AudioTrack track) {
				musicManager.scheduler.queue(track);
				musicManager.scheduler.channel = message.getTextChannel();

				message.reply("Adding to queue: `")
				.append(track.getInfo().title)
				.append("` by `")
				.append(track.getInfo().author)
				.append('`')
				.queue();

				if(musicManager.scheduler.index == 0) {
					musicManager.scheduler.nextTrack();
				}
			}

			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				final List<AudioTrack> tracks = playlist.getTracks();
				musicManager.scheduler.channel = message.getTextChannel();

				if(first) {
					musicManager.scheduler.queue(tracks.get(0));
					message.reply("Adding to queue: `")
					.append(tracks.get(0).getInfo().title)
					.append("` by `")
					.append(tracks.get(0).getInfo().author)
					.append('`')
					.queue();

					if(musicManager.scheduler.index == 0) {
						musicManager.scheduler.nextTrack();
					}
				}else {
					for (final AudioTrack track : tracks) {
						musicManager.scheduler.queue(track);
					}

					message.reply("Adding to queue: `")
					.append(String.valueOf(tracks.size()))
					.append("` tracks from playlist `")
					.append(playlist.getName().replace("Search results for: ", ""))
					.append('`')
					.queue();

					if(musicManager.scheduler.index == 0) {
						musicManager.scheduler.nextTrack();
					}
				}
			}

			@Override
			public void noMatches() {
				message.reply("No results.").queue();
			}

			@Override
			public void loadFailed(FriendlyException exception) {
				message.getChannel().sendMessage("I encountered a minor problem playing the track...").queue();
			}
		});
	}

	public static PlayerManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new PlayerManager();
		}

		return INSTANCE;
	}

}
