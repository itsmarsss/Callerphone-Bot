package com.marsss.vcuserphone;

import com.marsss.Bot;
import com.marsss.vcuserphone.AudioStorage.Audio;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

public class VCCallPairer {

	public static void onCallCommand(VoiceChannel vcchannel, Message message) {
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
							MEMBERSRECEIVER += m.getAsMention() + ", ";
						}
					}
					if (MEMBERSRECEIVER.length() > 0) {
						MEMBERSRECEIVER = replaceLast(MEMBERSRECEIVER.substring(0, MEMBERSRECEIVER.length()-2), ",", ", and");
					}else
						MEMBERSRECEIVER = "No members.";

					String MEMBERSCALLER = "";

					for(Member m : jda.getVoiceChannelById(audio.getCallerVCID()).getGuild().getSelfMember().getVoiceState().getChannel().getMembers()) {
						if(!(m.getUser() == jda.getSelfUser())) {
							MEMBERSCALLER += m.getUser().getAsTag() + ", ";
						}
					}
					if (MEMBERSCALLER.length() > 0) {
						MEMBERSCALLER = replaceLast(MEMBERSCALLER.substring(0, MEMBERSCALLER.length()-2), ",", ", and");
					}else
						MEMBERSCALLER = "no one :(";

					jda.getTextChannelById(audio.getCallerChannelID()).sendMessage("Someone picked up the phone!").queue();
					jda.getTextChannelById(audio.getCallerChannelID()).sendMessage("You are in a call with " + MEMBERSRECEIVER).queue();

					message.reply("Calling...").queue();
					message.getChannel().sendMessage("Someone picked up the phone!").queue();
					message.getChannel().sendMessage("You are in a call with " + MEMBERSCALLER).queue();

					System.out.println("From Guild: " + audio.getCallerVCID() + " - To Guild: " + audio.getReceiverVCID());
					System.out.println("From Channel: " + audio.getCallerChannelID() + " - To Channel: " + audio.getReceiverChannelID());
					return;
				}else if(audio.getCallerVCID().equals("empty")) {
					audio.setCallerVCID(vcchannel.getId());
					audio.setCallerChannelID(CHANNELID);
					message.reply("Calling...").queue();
					return;
				}
			}
		}
		message.reply("Hmmm, I was unable to find an open port!");
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
