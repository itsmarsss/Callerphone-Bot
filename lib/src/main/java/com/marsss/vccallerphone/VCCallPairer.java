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
	private static final String Callerphone = Bot.Callerphone;
	public static void onCallCommand(VoiceChannel vcchannel, Message message) {
		final Logger logger = LoggerFactory.getLogger(VCCallPairer.class);
		final String CHANNELID = message.getChannel().getId();
		final JDA jda = Bot.jda;
		for(int i = 0; i < AudioStorage.audio.length; i++) {
			final Audio audio = AudioStorage.audio[i];
			if(!audio.getConnected()) {
				if(!audio.getCallerVCID().equals("empty")) {
					
					audio.setReceiverVCID(vcchannel.getId());
					audio.setReceiverChannelID(CHANNELID);
					audio.setConnected(true);
					connectTo(vcchannel, i);

					String MEMBERSRECEIVER = "";

					for(Member m : vcchannel.getMembers()) {
						if(!(m.getUser() == jda.getSelfUser())) {
							if(Bot.admin.contains(m.getId())) {
								MEMBERSRECEIVER += "**[Moderator] " + m.getUser().getAsTag() + "**, ";
							}else if(Bot.supporter.contains(m.getId())) {
								MEMBERSRECEIVER += "**[Supporter] " + m.getUser().getAsTag() + "**, ";
							}else {
								MEMBERSRECEIVER += "**" + m.getUser().getAsTag() + "**, ";
							}
						}
					}
					if (MEMBERSRECEIVER.length() > 0) {
						MEMBERSRECEIVER = replaceLast(MEMBERSRECEIVER.substring(0, MEMBERSRECEIVER.length()-2), ",", ", and");
					}else
						MEMBERSRECEIVER = "No members.";

					String MEMBERSCALLER = "";

					for(Member m : jda.getVoiceChannelById(audio.getCallerVCID()).getGuild().getSelfMember().getVoiceState().getChannel().getMembers()) {
						if(!(m.getUser() == jda.getSelfUser())) {
							if(Bot.admin.contains(m.getId())) {
								MEMBERSCALLER += "**[Moderator] " + m.getUser().getAsTag() + "**, ";
							}else if(Bot.supporter.contains(m.getId())) {
								MEMBERSCALLER += "**[Supporter] " + m.getUser().getAsTag() + "**, ";
							}else {
								MEMBERSCALLER += "**" + m.getUser().getAsTag() + "**, ";
							}
						}
					}
					if (MEMBERSCALLER.length() > 0) {
						MEMBERSCALLER = replaceLast(MEMBERSCALLER.substring(0, MEMBERSCALLER.length()-2), ",", ", and");
					}else
						MEMBERSCALLER = "no one :(";

					jda.getTextChannelById(audio.getCallerChannelID()).sendMessage(Callerphone + "Someone picked up the phone!").queue();
					jda.getTextChannelById(audio.getCallerChannelID()).sendMessage(Callerphone + "You are in a call with " + MEMBERSRECEIVER).queue();
					
					message.reply(Callerphone + "Calling...").queue();
					message.getChannel().sendMessage(Callerphone + "Someone picked up the phone!").queue();
					message.getChannel().sendMessage(Callerphone + "You are in a call with " + MEMBERSCALLER).queue();

					logger.info("From VC: " + audio.getCallerVCID() + " - To VC: " + audio.getReceiverVCID());
					logger.info("From Guild: " + jda.getVoiceChannelById(audio.getCallerVCID()).getGuild() + " - To Guild: " + jda.getVoiceChannelById(audio.getReceiverVCID()).getGuild());
					
					return;
				}else if(audio.getCallerVCID().equals("empty")) {
					audio.setCallerVCID(vcchannel.getId());
					audio.setCallerChannelID(CHANNELID);
					message.reply(Callerphone + "Calling...").queue();
					return;
				}
			}
		}
		message.reply(Callerphone + "Hmmm, I was unable to find an open port!").queue();
		logger.warn("Port not found");
	}

	private static void connectTo(VoiceChannel channel, int port) {
		final Guild guild = channel.getGuild();
		final AudioManager audioManager = guild.getAudioManager();
		final CallerAudioHandler callerhandler = new CallerAudioHandler();
		final ReceiverAudioHandler receiverhandler = new ReceiverAudioHandler();

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
		return "`" + Bot.Prefix + "call` - Voice call someone from another server.";

	}

	public static String hangupHelp() {
		return "`" + Bot.Prefix + "hangup` - Hangup a pending or existing call.";

	}
	
	public static String muteHelp() {
		return "`" + Bot.Prefix + "mute` - Mute the bot.";

	}
	
	public static String unmuteHelp() {
		return "`" + Bot.Prefix + "unmute` - Unmute the bot.";

	}
	
	public static String deafenHelp() {
		return "`" + Bot.Prefix + "deafen` - Deafen the bot.";

	}
	
	public static String undeafenHelp() {
		return "`" + Bot.Prefix + "undeafen` - Undeafen the bot.";

	}

	public static String reportHelp() {
		return "`" + Bot.Prefix + "reportcall` - Report a call, make sure to report during a call.";

	}
	
}
