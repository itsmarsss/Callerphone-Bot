package com.marsss.tccallerphone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.marsss.Bot;
import com.marsss.tccallerphone.ConvoStorage.Convo;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class TCCallPairer {

	public static void onCallCommand(TextChannel tcchannel, Message message) {
		Logger logger = LoggerFactory.getLogger(TCCallPairer.class);
		String CHANNELID = tcchannel.getId();
		JDA jda = Bot.jda;
		for(int i = 0; i < ConvoStorage.convo.length; i++) {
			Convo convo = ConvoStorage.convo[i];
			if(!convo.getConnected()) {
				if(!convo.getCallerTCID().equals("empty")) {
					convo.setReceiverTCID(CHANNELID);
					convo.setConnected(true);

					jda.getTextChannelById(convo.getCallerTCID()).sendMessage(Bot.Userphone + "Someone picked up the phone!").queue();

					message.reply(Bot.Userphone + "Calling...").queue();
					message.getChannel().sendMessage(Bot.Userphone + "Someone picked up the phone!").queue();
					
					logger.info("From TC: " + convo.getCallerTCID() + " - To TC: " + convo.getReceiverTCID());
					logger.info("From Guild: " + jda.getTextChannelById(convo.getCallerTCID()).getGuild().getId() + " - To Guild: " + jda.getTextChannelById(convo.getReceiverTCID()).getGuild().getId());
				
					return;
				}else if(convo.getCallerTCID().equals("empty")) {
					convo.setCallerTCID(CHANNELID);
					message.reply(Bot.Userphone + "Calling...").queue();
					return;
				}
			}
		}
		message.reply(Bot.Userphone + "Hmmm, I was unable to find an open port!").queue();
		logger.warn("Port not found");
	}
}
