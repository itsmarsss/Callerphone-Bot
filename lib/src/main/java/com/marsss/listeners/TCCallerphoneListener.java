package com.marsss.listeners;

import java.time.LocalDateTime;

import com.marsss.Bot;
import com.marsss.tccallerphone.ConvoStorage;
import com.marsss.tccallerphone.TCCallPairer;
import com.marsss.tccallerphone.ConvoStorage.Convo;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class TCCallerphoneListener extends ListenerAdapter {
	private static final String Callerphone = Bot.Callerphone;
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {

		final Message MESSAGE = event.getMessage();
		final String MESSAGERAW = MESSAGE.getContentRaw();
		final String args[] = MESSAGERAW.toLowerCase().split("\\s+");
		SWITCH : switch (args[0].toLowerCase().replace(Bot.Prefix, "")) {



		case "endchat":
			if(!hasCall(event.getChannel().getId())) {
				MESSAGE.reply(Callerphone + "There is no call to end!").queue();
				break;
			}

			final JDA jda = Bot.jda;

			for(Convo c : ConvoStorage.convo) {
				TextChannel CALLER = null;
				TextChannel RECEIVER = null;

				try {
					CALLER = jda.getTextChannelById(c.getCallerTCID());
					RECEIVER = jda.getTextChannelById(c.getReceiverTCID());
				}catch(Exception e) {}

				if(CALLER == null && RECEIVER == null) {
					c.resetMessage();
					continue;
				}

				if(CALLER.getId().equals(event.getChannel().getId())) {
					if(RECEIVER != null) {
						RECEIVER.sendMessage(Callerphone + "The other party hung up the phone.").queue();
					}

					final String callerID = c.getCallerTCID();
					final String receiverID = c.getReceiverTCID();

					String data = "";
					for(String m : c.getMessages())
						data += m + "\n";

					c.resetMessage();

					MESSAGE.reply(Callerphone + "You hung up the phone.").queue();

					LocalDateTime now = LocalDateTime.now();
					final String month = String.valueOf(now.getMonthValue());
					final String day = String.valueOf(now.getDayOfMonth());
					final String hour = String.valueOf(now.getHour());
					final String minute = String.valueOf(now.getMinute());
					final String ID = month + day + hour + minute + callerID + receiverID;			

					final String DATA = data;

					if(c.report) {
						jda.getTextChannelById("897290511000404008").sendMessage("**ID:** " + ID).addFile(DATA.getBytes(), ID + ".txt").queue();
					}

					break SWITCH;
				}else if(RECEIVER.getId().equals(event.getChannel().getId())) {
					if(CALLER != null) {
						CALLER.sendMessage(Callerphone + "The other party hung up the phone.").queue();
					}

					final String callerID = c.getCallerTCID();
					final String receiverID = c.getReceiverTCID();

					String data = "";
					for(String m : c.getMessages())
						data += m + "\n";

					c.resetMessage();

					MESSAGE.reply(Callerphone + "You hung up the phone.").queue();
					LocalDateTime now = LocalDateTime.now();
					String month = String.valueOf(now.getMonthValue());
					String day = String.valueOf(now.getDayOfMonth());
					String hour = String.valueOf(now.getHour());
					String minute = String.valueOf(now.getMinute());
					String ID = month + day + hour + minute + callerID + receiverID;			

					final String DATA = data;

					if(c.report) {
						jda.getTextChannelById("897290511000404008").sendMessage("**ID:** " + ID).addFile(DATA.getBytes(), ID + ".txt").queue();
					}

					break SWITCH;
				}
			}



			MESSAGE.reply(Callerphone + "I was not able to find the call...").queue();
			break;



		case "chatcall":
			if(Bot.blacklist.contains(event.getAuthor().getId())) {
				MESSAGE.reply("Sorry you are blacklisted, submit an appeal at our support server").queue();
				break;
			}
			if(hasCall(event.getChannel().getId())) {
				MESSAGE.reply(Callerphone + "There is already a call going on!").queue();
				break;
			}

			TCCallPairer.onCallCommand(event.getChannel(), MESSAGE);
			break;

		case "reportchat":
			if(!hasCall(event.getChannel().getId())) {
				MESSAGE.reply(Callerphone + "There isn't a chat going on!").queue();
				break;
			}
			for (Convo c : ConvoStorage.convo) {
				if(!c.getConnected()) {
					break SWITCH;
				}
				if(c.getCallerTCID().equals(event.getChannel().getId()) || c.getReceiverTCID().equals(event.getChannel().getId())) {
					c.report = true;
					MESSAGE.reply(Callerphone + "Call reported!").queue();
					break SWITCH;
				}
			}
			MESSAGE.reply(Callerphone + "Something went wrong, couldn't report call.").queue();
			break;


		default:
			if(Bot.blacklist.contains(event.getAuthor().getId())) {
				break;
			}

			if(!hasCall(event.getChannel().getId())) {
				break;
			}


			if(MESSAGE.getAuthor().isBot()) {
				break;
			}

			for (Convo c : ConvoStorage.convo) {
				if(!c.getConnected()) {
					break SWITCH;
				}
				if(c.getCallerTCID().equals(event.getChannel().getId())) {
					c.addMessage("Caller " + MESSAGE.getAuthor().getAsTag() + "(" + MESSAGE.getAuthor().getId() + ")" + ": " + MESSAGERAW);
					c.lastMessage = System.currentTimeMillis();
					try {
						if(Bot.admin.contains(event.getAuthor().getId())) {
							Bot.jda.getTextChannelById(c.getReceiverTCID()).sendMessage("***[Moderator]* " + MESSAGE.getAuthor().getAsTag() + "**: " + MESSAGERAW).queue();
						}else if(Bot.supporter.contains(event.getAuthor().getId())) {
							Bot.jda.getTextChannelById(c.getReceiverTCID()).sendMessage("***[Supporter]* " + MESSAGE.getAuthor().getAsTag() + "**: " + MESSAGERAW).queue();
						}else {
							Bot.jda.getTextChannelById(c.getReceiverTCID()).sendMessage("**" + MESSAGE.getAuthor().getAsTag() + "**: " + MESSAGERAW).queue();
						}
					}catch(Exception e) {
						final String callerID = c.getCallerTCID();
						final String receiverID = c.getReceiverTCID();

						String data = "";
						for(String m : c.getMessages())
							data += m + "\n";

						c.resetMessage();
						try {
							Bot.jda.getTextChannelById(receiverID).sendMessage(Callerphone + "The other party hung up.").queue();
						}catch(Exception ex) {}
						LocalDateTime now = LocalDateTime.now();
						String month = String.valueOf(now.getMonthValue());
						String day = String.valueOf(now.getDayOfMonth());
						String hour = String.valueOf(now.getHour());
						String minute = String.valueOf(now.getMinute());
						String ID = month + day + hour + minute + callerID + receiverID;			

						final String DATA = data;
						if(c.report) {
							Bot.jda.getTextChannelById("897290511000404008").sendMessage("**ID:** " + ID).addFile(DATA.getBytes(), ID + ".txt").queue();
						}
					}
					break SWITCH;
				}else if(c.getReceiverTCID().equals(event.getChannel().getId())) {
					c.addMessage("Receiver " + MESSAGE.getAuthor().getAsTag() + "(" + MESSAGE.getAuthor().getId() + ")" + ": " + MESSAGERAW);
					c.lastMessage = System.currentTimeMillis();
					try {
						if(Bot.admin.contains(event.getAuthor().getId())) {
							Bot.jda.getTextChannelById(c.getCallerTCID()).sendMessage("***[Moderator]* " + MESSAGE.getAuthor().getAsTag() + "**: " + MESSAGERAW).queue();
						}else if(Bot.supporter.contains(event.getAuthor().getId())) {
							Bot.jda.getTextChannelById(c.getCallerTCID()).sendMessage("***[Admin]* " + MESSAGE.getAuthor().getAsTag() + "**: " + MESSAGERAW).queue();
						}else {
							Bot.jda.getTextChannelById(c.getCallerTCID()).sendMessage("**" + MESSAGE.getAuthor().getAsTag() + "**: " + MESSAGERAW).queue();
						}
					}catch(Exception e) {
						final String callerID = c.getCallerTCID();
						final String receiverID = c.getReceiverTCID();

						String data = "";
						for(String m : c.getMessages())
							data += m + "\n";

						c.resetMessage();
						try {
							Bot.jda.getTextChannelById(callerID).sendMessage(Callerphone + "The other party hung up.").queue();
						}catch(Exception ex) {}
						LocalDateTime now = LocalDateTime.now();
						String month = String.valueOf(now.getMonthValue());
						String day = String.valueOf(now.getDayOfMonth());
						String hour = String.valueOf(now.getHour());
						String minute = String.valueOf(now.getMinute());
						String ID = month + day + hour + minute + callerID + receiverID;			

						final String DATA = data;
						if(c.report) {
							Bot.jda.getTextChannelById("897290511000404008").sendMessage("**ID:** " + ID).addFile(DATA.getBytes(), ID + ".txt").queue();
						}
					}
					break SWITCH;
				}
			}
		}
	}

	private boolean hasCall(String tc) {
		for(Convo c : ConvoStorage.convo) {
			try {
				if((tc.equals(c.getCallerTCID()) || tc.equals(c.getReceiverTCID())) && c.isConnected) {
					return true;
				}
			}catch(Exception e) {}
		}
		return false;
	}

}
