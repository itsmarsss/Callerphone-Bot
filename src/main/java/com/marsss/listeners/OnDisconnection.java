package com.marsss.listeners;

import com.marsss.Bot;
import com.marsss.vcuserphone.AudioStorage;
import com.marsss.vcuserphone.AudioStorage.Audio;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

public class OnDisconnection extends ListenerAdapter {
	public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
		if(event.getMember().getUser() == Bot.jda.getSelfUser()) {
			disconnect(event.getGuild(), "I was disconnected.");
			return;
		}

		if(event.getChannelLeft().getMembers().size() == 1)
			disconnect(event.getGuild(), "no one was in call");

	}


	public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
		if(event.getMember().getUser() == Bot.jda.getSelfUser()) {
			disconnect(event.getGuild(), "I was moved to another channel");
			return;
		}

		if(event.getChannelLeft().getMembers().size() == 1)
			disconnect(event.getGuild(), "no one was in call");
	}

	private void disconnect(Guild G, String S) {
		JDA jda = Bot.jda;
		Guild g = G;
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
				AudioManager CALLERAM = CALLER.getAudioManager();
				CALLERAM.closeAudioConnection();

				if(RECEIVER != null) {
					AudioManager RECEIVERAM = RECEIVER.getAudioManager();
					RECEIVERAM.closeAudioConnection();
					jda.getTextChannelById(a.getReceiverChannelID()).sendMessage("The other party hung up the phone. (" + S + " on the other server)").queue();
				}

				jda.getTextChannelById(a.getCallerChannelID()).sendMessage("You hung up the phone. (" + S + ")").queue();
				a.resetAudio();
				return;
			}else if(RECEIVER.getId().equals(g.getId())) {
				AudioManager RECEIVERAM = RECEIVER.getAudioManager();
				RECEIVERAM.closeAudioConnection();

				if(CALLER != null) {
					AudioManager CALLERAM = CALLER.getAudioManager();
					CALLERAM.closeAudioConnection();
					jda.getTextChannelById(a.getCallerChannelID()).sendMessage("The other party hung up the phone. (" + S + " on ther other server)").queue();
				}

				jda.getTextChannelById(a.getReceiverChannelID()).sendMessage("You hung up the phone. (" + S + ")").queue();
				a.resetAudio();
			}
		}
	}

}
