package com.marsss.listeners;

import com.marsss.Bot;
import com.marsss.music.lavaplayer.GuildMusicManager;
import com.marsss.music.lavaplayer.PlayerManager;
import com.marsss.vccallerphone.AudioStorage;
import com.marsss.vccallerphone.AudioStorage.Audio;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

public class OnDisconnection extends ListenerAdapter {
	private static final String Callerphone = Bot.Callerphone;
	public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {

		if(event.getMember().getUser() == Bot.jda.getSelfUser()) {
			AudioManager audioManager = event.getGuild().getAudioManager();
			audioManager.setSelfDeafened(false);
			final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
			musicManager.audioPlayer.destroy();
			musicManager.scheduler.queue.clear();
			musicManager.scheduler.index = 0;
			PlayerManager.getInstance().resetHandler(event.getGuild());

		}

		if(!VCCallerphoneListener.hasCall(event.getGuild().getId())) {
			return;
		}

		if(event.getMember().getUser() == Bot.jda.getSelfUser()) {
			disconnect(event.getGuild(), "I was disconnected.");
			return;
		}

		if(event.getChannelLeft().getMembers().size() == 1)
			disconnect(event.getGuild(), "no one was in call");

	}


	public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
		if(!VCCallerphoneListener.hasCall(event.getGuild().getId())) {
			return;
		}		

		if(event.getMember().getUser() == Bot.jda.getSelfUser()) {
			disconnect(event.getGuild(), "I was moved to another channel");
			return;
		}

		if(event.getChannelLeft().getMembers().size() == 1)
			disconnect(event.getGuild(), "no one was in call");
	}

	private void disconnect(Guild G, String S) {
		final JDA jda = Bot.jda;
		final Guild g = G;
		for(Audio a : AudioStorage.audio) {
			Guild CALLER = null;
			Guild RECEIVER = null;
			try {
				CALLER = jda.getVoiceChannelById(a.getCallerVCID()).getGuild();
				RECEIVER = jda.getVoiceChannelById(a.getReceiverVCID()).getGuild();
			}catch(Exception e) {}

			if(CALLER == null && RECEIVER == null) {
				a.resetAudio();
				continue;
			}

			if(CALLER.getId().equals(g.getId())) {
				final AudioManager CALLERAM = CALLER.getAudioManager();
				CALLERAM.setSendingHandler(null);
				CALLERAM.setReceivingHandler(null);
				CALLERAM.closeAudioConnection();

				if(RECEIVER != null) {
					final AudioManager RECEIVERAM = RECEIVER.getAudioManager();				
					RECEIVERAM.setSendingHandler(null);
					RECEIVERAM.setReceivingHandler(null);
					RECEIVERAM.closeAudioConnection();
					jda.getTextChannelById(a.getReceiverChannelID()).sendMessage(Callerphone + "The other party hung up the phone. (" + S + " on the other server)").queue();
				}

				jda.getTextChannelById(a.getCallerChannelID()).sendMessage(Callerphone + "You hung up the phone. (" + S + ")").queue();
				a.resetAudio();
				return;
			}else if(RECEIVER.getId().equals(g.getId())) {
				final AudioManager RECEIVERAM = RECEIVER.getAudioManager();
				RECEIVERAM.setSendingHandler(null);
				RECEIVERAM.setReceivingHandler(null);
				RECEIVERAM.closeAudioConnection();

				if(CALLER != null) {
					final AudioManager CALLERAM = CALLER.getAudioManager();
					CALLERAM.setSendingHandler(null);
					CALLERAM.setReceivingHandler(null);
					CALLERAM.closeAudioConnection();
					jda.getTextChannelById(a.getCallerChannelID()).sendMessage(Callerphone + "The other party hung up the phone. (" + S + " on the other server)").queue();
				}

				jda.getTextChannelById(a.getReceiverChannelID()).sendMessage(Callerphone + "You hung up the phone. (" + S + ")").queue();
				a.resetAudio();
			}
		}
	}

}
