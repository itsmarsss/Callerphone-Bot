package com.marsss.listeners;

import com.marsss.Bot;
import com.marsss.vcuserphone.AudioStorage;
import com.marsss.vcuserphone.AudioStorage.Audio;
import com.marsss.vcuserphone.VCCallPairer;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

public class VCUserphoneListener extends ListenerAdapter {
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {

		Message MESSAGE = event.getMessage();
		String args[] = MESSAGE.getContentRaw().toLowerCase().split("\\s+");
		SWITCH : switch (args[0].toLowerCase()) {



		case "u?hangup":
			String VC;

			if(event.getMember().getVoiceState().inVoiceChannel()) {
				VC = event.getMember().getVoiceState().getChannel().getId();
			}else {
				MESSAGE.reply("You are not in the call channel.").queue();
				return;
			}
			Guild g = event.getGuild();

			if(!hasCall(g.getId())) {
				MESSAGE.reply("There is no call in this server.").queue();
				break;
			}

			if(!inCallChannel(VC)) {
				MESSAGE.reply("You are not in the call channel.").queue();
				break;
			}

			JDA jda = Bot.jda;


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
						jda.getTextChannelById(a.getReceiverChannelID()).sendMessage("The other party hung up the phone.").queue();
					}

					MESSAGE.reply("You hung up the phone.").queue();
					a.resetAudio();
					break SWITCH;
				}else if(RECEIVER.getId().equals(g.getId())) {
					AudioManager RECEIVERAM = RECEIVER.getAudioManager();
					RECEIVERAM.closeAudioConnection();

					if(CALLER != null) {
						AudioManager CALLERAM = CALLER.getAudioManager();
						CALLERAM.closeAudioConnection();
						jda.getTextChannelById(a.getCallerChannelID()).sendMessage("The other party hung up the phone.").queue();
					}

					MESSAGE.reply("You hung up the phone.").queue();
					a.resetAudio();
					break SWITCH;
				}
			}



			MESSAGE.reply("I was not able to find the call...").queue();
			break;



		case "u?call":
			if(event.getGuild().getSelfMember().getVoiceState().inVoiceChannel()) {
				event.getMessage().reply("Sorry, I am currently connected to " + event.getGuild().getSelfMember().getVoiceState().getChannel().getAsMention()).queue();
				break;
			}
			GuildVoiceState GVS = event.getMember().getVoiceState();
			if(!GVS.inVoiceChannel()) {
				event.getMessage().reply("You have to be in a voicechannel that I have access to.").queue();
				break;
			}
			if(!event.getGuild().getSelfMember().hasPermission(GVS.getChannel(), Permission.VOICE_CONNECT)) {
				event.getMessage().reply("I do not have access to " + GVS.getChannel().getAsMention()).queue();
				break;
			}
			if(!event.getGuild().getSelfMember().hasPermission(GVS.getChannel(), Permission.VOICE_SPEAK)) {
				event.getMessage().reply("I do not have access to speak in" + GVS.getChannel().getAsMention()).queue();
				break;
			}
			AudioManager audioManager = event.getGuild().getAudioManager();

			//		if(audioManager.isAttemptingToConnect()) {
			//			event.getMessage().reply("I'm already trying to connect! Chill out...").queue();
			//			return;
			//		}

			audioManager.openAudioConnection(GVS.getChannel());
			event.getMessage().reply("Connected to " + GVS.getChannel().getAsMention()).queue();
			VCCallPairer.onCallCommand(event.getMember().getVoiceState().getChannel(), event.getMessage());
			break;



		}
	}

	private boolean hasCall(String g) {
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
