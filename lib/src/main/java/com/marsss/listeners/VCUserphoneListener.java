package com.marsss.listeners;

import com.marsss.vcuserphone.VCCallPairer;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

public class VCUserphoneListener extends ListenerAdapter {
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		
		if(!event.getMessage().getContentRaw().startsWith(";call")) {
			return;
		}
		
		
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
