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
import com.marsss.vccallerphone.VCCallPairer;
import com.marsss.vccallerphone.AudioStorage.Audio;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

public class VCCallerphoneListener extends ListenerAdapter {
	private static final String Callerphone = Bot.Callerphone;
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {

		if(!event.getChannel().canTalk())
			return;

		final Message MESSAGE = event.getMessage();
		final String args[] = MESSAGE.getContentRaw().toLowerCase().split("\\s+");
		final Member selfmember = event.getGuild().getSelfMember();
		final AudioManager am = event.getGuild().getAudioManager();
		final Guild g = event.getGuild();

		if(!args[0].toLowerCase().startsWith(Bot.Prefix))
			return;

		SWITCH : switch (args[0].replace(Bot.Prefix, "")) {

		case "reportcall":
			if(!hasCall(event.getGuild().getId())) {
				MESSAGE.reply(Callerphone + "There isn't a call going on!").queue();
				break;
			}
			for (Audio a : AudioStorage.audio) {
				if(!a.getConnected()) {
					MESSAGE.reply("No call to report").queue();
					break SWITCH;
				}
				if(a.getCallerChannelID().equals(event.getChannel().getId())) {
					final StringBuilder members = new StringBuilder();
					for(Member m : 
						Bot.jda.getVoiceChannelById(a.getReceiverVCID()).getMembers()) {
						members.append(m.getUser().getAsTag())
						.append("(" + m.getId() + ")")
						.append("\n");
					}
					Bot.jda.getTextChannelById("897290511000404008").sendMessage("**Reported users:**").addFile(members.toString().getBytes(), "reported.txt").queue();
					MESSAGE.reply(Callerphone + "Call reported!").queue();
					break SWITCH;
				}

				if(a.getReceiverChannelID().equals(event.getChannel().getId())) {
					final StringBuilder members = new StringBuilder();
					for(Member m : 
						Bot.jda.getVoiceChannelById(a.getReceiverVCID()).getMembers()) {
						members.append(m.getUser().getAsTag())
						.append("(" + m.getId() + ")")
						.append("\n");
					}
					Bot.jda.getTextChannelById("897290511000404008").sendMessage("**Reported users:**").addFile(members.toString().getBytes(), "reported.txt").queue();
					MESSAGE.reply(Callerphone + "Call reported!").queue();
					break SWITCH;
				}
			}
			MESSAGE.reply(Callerphone + "Something went wrong, couldn't report call.").queue();
			break;


		case "hangup":
			String VC;

			if(event.getMember().getVoiceState().inVoiceChannel()) {
				VC = event.getMember().getVoiceState().getChannel().getId();
			}else {
				MESSAGE.reply(Callerphone + "You are not in the call channel.").queue();
				return;
			}

			if(!hasCall(g.getId())) {
				MESSAGE.reply(Callerphone + "There is no call in this server.").queue();
				break;
			}

			if(!inCallChannel(VC)) {
				MESSAGE.reply(Callerphone + "You are not in the call channel.").queue();
				break;
			}

			final JDA jda = Bot.jda;


			for(Audio a : AudioStorage.audio) {

				Guild CALLER = null;
				Guild RECEIVER = null;

				try {
					CALLER = jda.getVoiceChannelById(a.getCallerVCID()).getGuild();
					RECEIVER = jda.getVoiceChannelById(a.getReceiverVCID()).getGuild();
				}catch(Exception e) {}

				if((CALLER == null && RECEIVER == null) || (CALLER == null)) {
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
						jda.getTextChannelById(a.getReceiverChannelID()).sendMessage(Callerphone + "The other party hung up the phone.").queue();
					}

					MESSAGE.reply(Callerphone + "You hung up the phone.").queue();
					a.resetAudio();
					break SWITCH;
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
						jda.getTextChannelById(a.getCallerChannelID()).sendMessage(Callerphone + "The other party hung up the phone.").queue();
					}

					MESSAGE.reply(Callerphone + "You hung up the phone.").queue();
					a.resetAudio();
					break SWITCH;
				}
			}



			MESSAGE.reply(Callerphone + "I was not able to find the call...").queue();
			break;



		case "call":
			if(Bot.blacklist.contains(event.getAuthor().getId())) {
				MESSAGE.reply("Sorry you are blacklisted, submit an appeal at our support server").queue();
				break;
			}
			if(event.getGuild().getSelfMember().getVoiceState().inVoiceChannel()) {
				MESSAGE.reply(Callerphone + "Sorry, I am currently connected to " + event.getGuild().getSelfMember().getVoiceState().getChannel().getAsMention()).queue();
				break;
			}
			final GuildVoiceState GVS = event.getMember().getVoiceState();
			if(!GVS.inVoiceChannel()) {
				MESSAGE.reply(Callerphone + "You have to be in a voicechannel that I have access to.").queue();
				break;
			}
			if(!event.getGuild().getSelfMember().hasPermission(GVS.getChannel(), Permission.VOICE_CONNECT)) {
				MESSAGE.reply(Callerphone + "I do not have access to " + GVS.getChannel().getAsMention()).queue();
				break;
			}
			if(!event.getGuild().getSelfMember().hasPermission(GVS.getChannel(), Permission.VOICE_SPEAK)) {
				MESSAGE.reply(Callerphone + "I do not have access to speak in" + GVS.getChannel().getAsMention()).queue();
				break;
			}
			final AudioManager audioManager = event.getGuild().getAudioManager();

			//		if(audioManager.isAttemptingToConnect()) {
			//			MESSAGE.reply("I'm already trying to connect! Chill out...").queue();
			//			return;
			//		}

			boolean anon = false;
			if(args.length >= 2) {
				if(args[1].equalsIgnoreCase("anon")) {
					anon = true;
				}
			}

			audioManager.openAudioConnection(GVS.getChannel());
			MESSAGE.reply(Callerphone + "Connected to " + GVS.getChannel().getAsMention()).queue();
			VCCallPairer.onCallCommand(event.getMember().getVoiceState().getChannel(), MESSAGE, anon);
			break;



		case "deafen":
			if(!selfmember.getVoiceState().inVoiceChannel()) {
				MESSAGE.reply(Callerphone + "I am not in a voice channel.").queue();
				break;
			}

			if(!hasCall(g.getId())) {
				MESSAGE.reply(Callerphone + "There is no call in this server.").queue();
				break;
			}

			if(!selfmember.getVoiceState().getChannel().getId().equals(event.getMember().getVoiceState().getChannel().getId())) {
				MESSAGE.reply(Callerphone + "You are not in the same voice channel as me.").queue();
				break;
			}

			if(!am.isSelfDeafened()) {
				am.setSelfDeafened(true);
				break;
			}



		case "undeafen":
			if(!selfmember.getVoiceState().inVoiceChannel()) {
				MESSAGE.reply(Callerphone + "I am not in a voice channel.").queue();
				break;
			}

			if(!hasCall(g.getId())) {
				MESSAGE.reply(Callerphone + "There is no call in this server.").queue();
				break;
			}

			if(!selfmember.getVoiceState().getChannel().getId().equals(event.getMember().getVoiceState().getChannel().getId())) {
				MESSAGE.reply(Callerphone+ "You are not in the same voice channel as me.").queue();
				break;
			}

			if(selfmember.getVoiceState().isGuildDeafened() && !selfmember.hasPermission(Permission.VOICE_DEAF_OTHERS)) {
				MESSAGE.reply(Callerphone + "I do not have permission to undeafen!").queue();
				break;
			}else if(!am.isSelfDeafened()) {
				am.setSelfDeafened(false);
				break;
			}



		case "mute":
			if(!selfmember.getVoiceState().inVoiceChannel()) {
				MESSAGE.reply(Callerphone + "I am not in a voice channel.").queue();
				break;
			}

			if(!hasCall(g.getId())) {
				MESSAGE.reply(Callerphone + "There is no call in this server.").queue();
				break;
			}

			if(!selfmember.getVoiceState().getChannel().getId().equals(event.getMember().getVoiceState().getChannel().getId())) {
				MESSAGE.reply(Callerphone + "You are not in the same voice channel as me.").queue();
				break;
			}

			if(!am.isSelfMuted()) {
				am.setSelfMuted(true);
				break;
			}



		case "unmute":
			if(!selfmember.getVoiceState().inVoiceChannel()) {
				MESSAGE.reply(Callerphone + "I am not in a voice channel.").queue();
				break;
			}

			if(!hasCall(g.getId())) {
				MESSAGE.reply(Callerphone + "There is no call in this server.").queue();
				break;
			}

			if(!selfmember.getVoiceState().getChannel().getId().equals(event.getMember().getVoiceState().getChannel().getId())) {
				MESSAGE.reply(Callerphone + "You are not in the same voice channel as me.").queue();
				break;
			}

			if(selfmember.getVoiceState().isGuildDeafened() && !selfmember.hasPermission(Permission.VOICE_MUTE_OTHERS)) {
				MESSAGE.reply(Callerphone + "I do not have permission to unmute!").queue();
				break;
			}else if(!am.isSelfMuted()) {
				am.setSelfDeafened(false);
				break;
			}



		}
	}

	public static boolean hasCall(String g) {
		for(Audio a : AudioStorage.audio) {
			try {
				if((Bot.jda.getVoiceChannelById(a.callerVCID).getGuild().getId().equals(g) || 
						Bot.jda.getVoiceChannelById(a.receiverVCID).getGuild().getId().equals(g))) {
					return true;
				}
			}catch(Exception e) {}
		}
		return false;
	}

	private boolean inCallChannel(String VC) {
		for(Audio a : AudioStorage.audio) {
			if(a.callerVCID.equals(VC) || a.receiverVCID.equals(VC)) {
				return true;
			}
		}
		return false;
	}
}
