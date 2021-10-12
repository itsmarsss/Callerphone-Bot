package com.marsss.vccallerphone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.marsss.Bot;
import com.marsss.vccallerphone.AudioStorage.Audio;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

public class VCCallPairer {

	public static void onCallCommand(VoiceChannel vcchannel, Message message) {
		Logger logger = LoggerFactory.getLogger(VCCallPairer.class);
		String CHANNELID = message.getChannel().getId();
		JDA jda = Bot.jda;
		for(int i = 0; i < AudioStorage.audio.length; i++) {
			Audio audio = AudioStorage.audio[i];
			if(!audio.getConnected()) {
				if(!audio.getCallerVCID().equals("empty")) {
					audio.setReceiverVCID(vcchannel.getId());
					audio.setReceiverChannelID(CHANNELID);
					audio.setConnected(true);
					connectTo(vcchannel, i);

					String MEMBERSRECEIVER = "";

					for(Member m : vcchannel.getMembers()) {
						if(!(m.getUser() == jda.getSelfUser())) {
							MEMBERSRECEIVER += "**" + m.getUser().getAsTag() + "**, ";
						}
					}
					if (MEMBERSRECEIVER.length() > 0) {
						MEMBERSRECEIVER = replaceLast(MEMBERSRECEIVER.substring(0, MEMBERSRECEIVER.length()-2), ",", ", and");
					}else
						MEMBERSRECEIVER = "No members.";

					String MEMBERSCALLER = "";

					for(Member m : jda.getVoiceChannelById(audio.getCallerVCID()).getGuild().getSelfMember().getVoiceState().getChannel().getMembers()) {
						if(!(m.getUser() == jda.getSelfUser())) {
							MEMBERSCALLER += "**" + m.getUser().getAsTag() + "**, ";
						}
					}
					if (MEMBERSCALLER.length() > 0) {
						MEMBERSCALLER = replaceLast(MEMBERSCALLER.substring(0, MEMBERSCALLER.length()-2), ",", ", and");
					}else
						MEMBERSCALLER = "no one :(";

					jda.getTextChannelById(audio.getCallerChannelID()).sendMessage(Bot.Userphone + "Someone picked up the phone!").queue();
					jda.getTextChannelById(audio.getCallerChannelID()).sendMessage(Bot.Userphone + "You are in a call with " + MEMBERSRECEIVER).queue();
					
					message.reply(Bot.Userphone + "Calling...").queue();
					message.getChannel().sendMessage(Bot.Userphone + "Someone picked up the phone!").queue();
					message.getChannel().sendMessage(Bot.Userphone + "You are in a call with " + MEMBERSCALLER).queue();

					logger.info("From VC: " + audio.getCallerVCID() + " - To VC: " + audio.getReceiverVCID());
					logger.info("From Guild: " + jda.getVoiceChannelById(audio.getCallerChannelID()).getGuild() + " - To Guild: " + jda.getVoiceChannelById(audio.getReceiverChannelID()).getGuild());
					
					return;
				}else if(audio.getCallerVCID().equals("empty")) {
					audio.setCallerVCID(vcchannel.getId());
					audio.setCallerChannelID(CHANNELID);
					message.reply(Bot.Userphone + "Calling...").queue();
					return;
				}
			}
		}
		message.reply(Bot.Userphone + "Hmmm, I was unable to find an open port!").queue();
		logger.warn("Port not found");
	}

	private static void connectTo(VoiceChannel channel, int port) {
		Guild guild = channel.getGuild();
		AudioManager audioManager = guild.getAudioManager();
		CallerAudioHandler callerhandler = new CallerAudioHandler();
		ReceiverAudioHandler receiverhandler = new ReceiverAudioHandler();

		callerhandler.setPort(port);
		receiverhandler.setPort(port);

		Bot.jda.getVoiceChannelById(AudioStorage.audio[port].getCallerVCID()).getGuild().getAudioManager()
		.setSendingHandler(callerhandler);

		audioManager
		.setSendingHandler(receiverhandler);

		Bot.jda.getVoiceChannelById(AudioStorage.audio[port].getCallerVCID()).getGuild().getAudioManager()
		.setReceivingHandler(callerhandler);

		audioManager
		.setReceivingHandler(receiverhandler);

	}

	private static String replaceLast(final String text, final String regex, final String replacement) {
		return text.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
	}

	public static String callHelp() {
		return "`u?call` - Call someone from another server.";

	}

	public static String hangupHelp() {
		return "`u?hangup` - Hangup a pending call or existing call.";

	}

}
