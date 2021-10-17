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

		final Message MESSAGE = event.getMessage();
		final String args[] = MESSAGE.getContentRaw().toLowerCase().split("\\s+");
		final Member selfmember = event.getGuild().getSelfMember();
		final AudioManager am = event.getGuild().getAudioManager();
		final Guild g = event.getGuild();

		SWITCH : switch (args[0].toLowerCase().replaceFirst(Bot.Prefix, "")) {


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

				if(CALLER == null && RECEIVER == null) {
					a.resetAudio();
					continue;
				}


				if(CALLER.getId().equals(g.getId())) {
					final AudioManager CALLERAM = CALLER.getAudioManager();
					CALLERAM.closeAudioConnection();

					if(RECEIVER != null) {
						final AudioManager RECEIVERAM = RECEIVER.getAudioManager();
						RECEIVERAM.closeAudioConnection();
						jda.getTextChannelById(a.getReceiverChannelID()).sendMessage(Callerphone + "The other party hung up the phone.").queue();
					}

					MESSAGE.reply(Callerphone + "You hung up the phone.").queue();
					a.resetAudio();
					break SWITCH;
				}else if(RECEIVER.getId().equals(g.getId())) {
					final AudioManager RECEIVERAM = RECEIVER.getAudioManager();
					RECEIVERAM.closeAudioConnection();

					if(CALLER != null) {
						final AudioManager CALLERAM = CALLER.getAudioManager();
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



		case "voicecall":
			if(event.getGuild().getSelfMember().getVoiceState().inVoiceChannel()) {
				event.getMessage().reply(Callerphone + "Sorry, I am currently connected to " + event.getGuild().getSelfMember().getVoiceState().getChannel().getAsMention()).queue();
				break;
			}
			final GuildVoiceState GVS = event.getMember().getVoiceState();
			if(!GVS.inVoiceChannel()) {
				event.getMessage().reply(Callerphone + "You have to be in a voicechannel that I have access to.").queue();
				break;
			}
			if(!event.getGuild().getSelfMember().hasPermission(GVS.getChannel(), Permission.VOICE_CONNECT)) {
				event.getMessage().reply(Callerphone + "I do not have access to " + GVS.getChannel().getAsMention()).queue();
				break;
			}
			if(!event.getGuild().getSelfMember().hasPermission(GVS.getChannel(), Permission.VOICE_SPEAK)) {
				event.getMessage().reply(Callerphone + "I do not have access to speak in" + GVS.getChannel().getAsMention()).queue();
				break;
			}
			final AudioManager audioManager = event.getGuild().getAudioManager();

			//		if(audioManager.isAttemptingToConnect()) {
			//			event.getMessage().reply("I'm already trying to connect! Chill out...").queue();
			//			return;
			//		}

			audioManager.openAudioConnection(GVS.getChannel());
			event.getMessage().reply(Callerphone + "Connected to " + GVS.getChannel().getAsMention()).queue();
			VCCallPairer.onCallCommand(event.getMember().getVoiceState().getChannel(), event.getMessage());
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

	static boolean hasCall(String g) {
		for(Audio a : AudioStorage.audio) {
			try {
				if(Bot.jda.getVoiceChannelById(a.callerVCID).getGuild().getId().equals(g) || 
						Bot.jda.getVoiceChannelById(a.receiverVCID).getGuild().getId().equals(g)) {
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
