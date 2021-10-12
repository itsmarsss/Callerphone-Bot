package com.marsss.listeners;

import com.marsss.Bot;
import com.marsss.vccallerphone.AudioStorage;
import com.marsss.vccallerphone.AudioStorage.Audio;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMuteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class OnMuted extends ListenerAdapter {

	public void onGuildVoiceMute(GuildVoiceMuteEvent event) {
		if(event.getMember().getUser() != Bot.jda.getSelfUser())
			return;

		GuildVoiceState VS = event.getGuild().getSelfMember().getVoiceState();
		String VC;
		if(VS.inVoiceChannel()) {
			VC = VS.getChannel().getId();
		} else {
			return;
		}

		String prefix = "";
		String can = "";
		if(!event.isMuted()) {
			prefix = "un";
		}
		if(event.isMuted()) {
			can = "no longer ";
		}

		for(Audio a : AudioStorage.audio) {
			if(a.getCallerVCID().equals(VC)) { 
				Bot.jda.getTextChannelById(a.getCallerChannelID()).sendMessage(Bot.Userphone + "I've been " + prefix + "deafened for the call (I am " + prefix + "muted so you " + can + "can hear the other party.)").queue();
				try {
					Bot.jda.getTextChannelById(a.getReceiverChannelID()).sendMessage(Bot.Userphone + "The other party " + prefix + "deafened").queue();
				}catch(Exception e) {}
				break;
			}

			if(a.getReceiverVCID().equals(VC)) { 
				Bot.jda.getTextChannelById(a.getReceiverChannelID()).sendMessage(Bot.Userphone + "I've been " + prefix + "deafened for the call (I am " + prefix + "muted so you " + can + "can hear the other party.)").queue();
				try{
					Bot.jda.getTextChannelById(a.getCallerChannelID()).sendMessage(Bot.Userphone + "The other party " + prefix + "deafened").queue();
				}catch(Exception e) {}
				break;
			}
		}
	}
}
