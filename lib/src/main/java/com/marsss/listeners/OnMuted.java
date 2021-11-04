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
package com.marsss.listeners;

import com.marsss.Bot;
import com.marsss.vccallerphone.AudioStorage;
import com.marsss.vccallerphone.AudioStorage.Audio;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMuteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class OnMuted extends ListenerAdapter {
	private static final String Callerphone = Bot.Callerphone;
	public void onGuildVoiceMute(GuildVoiceMuteEvent event) {
		if(event.getMember().getUser() != Bot.jda.getSelfUser())
			return;

		final GuildVoiceState VS = event.getGuild().getSelfMember().getVoiceState();
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
				Bot.jda.getTextChannelById(a.getCallerChannelID()).sendMessage(Callerphone + "I've been " + prefix + "deafened for the call (I am " + prefix + "muted so you " + can + "can hear the other party.)").queue();
				try {
					Bot.jda.getTextChannelById(a.getReceiverChannelID()).sendMessage(Callerphone + "The other party " + prefix + "deafened").queue();
				}catch(Exception e) {}
				break;
			}

			if(a.getReceiverVCID().equals(VC)) { 
				Bot.jda.getTextChannelById(a.getReceiverChannelID()).sendMessage(Callerphone + "I've been " + prefix + "deafened for the call (I am " + prefix + "muted so you " + can + "can hear the other party.)").queue();
				try{
					Bot.jda.getTextChannelById(a.getCallerChannelID()).sendMessage(Callerphone + "The other party " + prefix + "deafened").queue();
				}catch(Exception e) {}
				break;
			}
		}
	}
}
