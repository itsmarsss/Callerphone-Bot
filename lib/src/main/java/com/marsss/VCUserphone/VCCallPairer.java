package com.marsss.VCUserphone;

import com.marsss.Bot;
import com.marsss.VCUserphone.AudioStorage.Audio;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

public class VCCallPairer {
	static int port;

	public static void onCallCommand(VoiceChannel vcchannel, Message message) {
		Guild GUILD = vcchannel.getGuild();
		String GUILDID = GUILD.getId();
		String CHANNELID = message.getChannel().getId();
		JDA jda = Bot.jda;
		for(int i = 0; i < AudioStorage.audio.length; i++) {
			Audio audio = AudioStorage.audio[i];
			if(!audio.connected) {
				if(!audio.callerGuildID.equals("empty")) {
					audio.receiverGuildID = GUILDID;
					audio.receiverChannelID = CHANNELID;
					audio.connected = true;
					connectTo(vcchannel, i, true);
					jda.getTextChannelById(audio.callerChannelID).sendMessage("Someone picked up the phone!").queue();
					message.reply("Calling...").queue();
					message.getChannel().sendMessage("Someone picked up the phone!").queue();
					System.out.println(audio.callerChannelID + "-" + audio.receiverGuildID);
					System.out.println(audio.callerGuildID + "-" + audio.receiverGuildID);
					return;
				}else if(audio.callerGuildID.equals("empty")) {
					audio.callerGuildID = GUILDID;
					audio.callerChannelID = CHANNELID;
					connectTo(vcchannel, i, false);
					message.reply("Calling...").queue();
					return;
				}
			}
		}
		message.reply("Hmmm, I was unable to find an open port!");
	}

	private static void connectTo(VoiceChannel channel, int inport, boolean receiver) {
		Guild guild = channel.getGuild();
		AudioManager audioManager = guild.getAudioManager();
		port = inport;
		if(receiver) {
			CallerAudioHandler callerhandler = new CallerAudioHandler();
			ReceiverAudioHandler receiverhandler = new ReceiverAudioHandler();
			
			Bot.jda.getGuildById(AudioStorage.audio[port].callerGuildID).getAudioManager().setSendingHandler(callerhandler);
			audioManager.setSendingHandler(receiverhandler);
			
			Bot.jda.getGuildById(AudioStorage.audio[port].callerGuildID).getAudioManager().setReceivingHandler(callerhandler);
			audioManager.setReceivingHandler(receiverhandler);
		}
	}
}
