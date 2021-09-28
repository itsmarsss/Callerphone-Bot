package com.marsss.listeners;

import com.marsss.Bot;
import com.marsss.vcuserphone.AudioStorage;
import com.marsss.vcuserphone.AudioStorage.Audio;
import com.marsss.vcuserphone.VCCallPairer;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

public class VCUserphoneListener extends ListenerAdapter {
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {

		String args[] = event.getMessage().getContentRaw().toLowerCase().split("\\s+");
		switch (args[0]) {
		
		
		
		case ";hangup":
			Guild g = event.getGuild();
			JDA jda = Bot.jda;
			for(Audio a : AudioStorage.audio) {
				Guild CALLER = null;
				Guild RECEIVER = null;
				try {
					CALLER = jda.getGuildById(a.getCallerGuildID());
					RECEIVER = jda.getGuildById(a.getReceiverGuildID());
				}catch(Exception e) {}
				if(CALLER == null || RECEIVER == null)
					continue;

				if(CALLER.getId().equals(g.getId())) {
					a.setConnected(false);
					AudioManager CALLERAM = CALLER.getAudioManager();
					CALLERAM.closeAudioConnection();

					AudioManager RECEIVERAM = RECEIVER.getAudioManager();
					RECEIVERAM.closeAudioConnection();

					jda.getTextChannelById(a.getCallerChannelID()).sendMessage("You hung up the phone.").queue();
					jda.getTextChannelById(a.getReceiverChannelID()).sendMessage("The other party hung up the phone.").queue();
				}else if(RECEIVER.getId().equals(g.getId())) {
					a.setConnected(false);
					AudioManager CALLERAM = CALLER.getAudioManager();
					CALLERAM.closeAudioConnection();

					AudioManager RECEIVERAM = RECEIVER.getAudioManager();
					RECEIVERAM.closeAudioConnection();

					jda.getTextChannelById(a.getReceiverChannelID()).sendMessage("You hung up the phone.").queue();
					jda.getTextChannelById(a.getCallerChannelID()).sendMessage("The other party hung up the phone.").queue();
				}
			}
			
			
			
		case ";call":
			if(event.getGuild().getSelfMember().getVoiceState().inVoiceChannel()) {
				event.getMessage().reply("I am currently connected to " + event.getGuild().getSelfMember().getVoiceState().getChannel().getAsMention()).queue();
				return;
			}
			GuildVoiceState GVS = event.getMember().getVoiceState();
			if(!GVS.inVoiceChannel()) {
				event.getMessage().reply("You have to be in a voicechannel that I have access to.").queue();
				return;
			}
			if(!event.getGuild().getSelfMember().hasPermission(GVS.getChannel(), Permission.VOICE_CONNECT)) {
				event.getMessage().reply("I do not have access to " + GVS.getChannel().getAsMention()).queue();
				return;
			}
			if(!event.getGuild().getSelfMember().hasPermission(GVS.getChannel(), Permission.VOICE_SPEAK)) {
				event.getMessage().reply("I do not have access to speak in" + GVS.getChannel().getAsMention()).queue();
				return;
			}
			AudioManager audioManager = event.getGuild().getAudioManager();

			//		if(audioManager.isAttemptingToConnect()) {
			//			event.getMessage().reply("I'm already trying to connect! Chill out...").queue();
			//			return;
			//		}

			audioManager.openAudioConnection(GVS.getChannel());
			event.getMessage().reply("Connected to " + GVS.getChannel().getAsMention()).queue();
			VCCallPairer.onCallCommand(event.getMember().getVoiceState().getChannel(), event.getMessage());
			
			
			
		}
	}
}
