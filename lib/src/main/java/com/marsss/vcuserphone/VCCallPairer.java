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

					for(Member m : jda.getGuildById(audio.callerGuildID).getSelfMember().getVoiceState().getChannel().getMembers()) {
						if(!(m.getUser() == jda.getSelfUser())) {
							MEMBERSCALLER += m.getAsMention() + ", ";
						}
					}
					if (MEMBERSCALLER.length() > 0) {
						MEMBERSCALLER = replaceLast(MEMBERSCALLER.substring(0, MEMBERSCALLER.length()-2), ",", ", and");
					}else
						MEMBERSCALLER = "no one :(";

					jda.getTextChannelById(audio.callerChannelID).sendMessage("Someone picked up the phone!").queue();
					jda.getTextChannelById(audio.callerChannelID).sendMessage("You are in a call with " + MEMBERSRECEIVER).queue();

					message.reply("Calling...").queue();
					message.getChannel().sendMessage("Someone picked up the phone!").queue();
					message.getChannel().sendMessage("You are in a call with " + MEMBERSCALLER).queue();
					System.out.println("From Guild: " + audio.callerChannelID + " - To Guild: " + audio.receiverGuildID);
					System.out.println("From Channel: " + audio.callerGuildID + " - To Channel: " + audio.receiverGuildID);
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

	private static String replaceLast(final String text, final String regex, final String replacement) {
		return text.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
	}
}
