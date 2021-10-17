package com.marsss.listeners;

import java.io.IOException;
import java.util.List;

import com.marsss.Bot;
import com.marsss.bot.*;
import com.marsss.entertainments.*;
import com.marsss.utils.*;

import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandListener extends ListenerAdapter {
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {

		if(!event.getChannel().canTalk())
			return;

		Member MEMBER = event.getMember();
		Message MESSAGE = event.getMessage();

		String CONTENT = MESSAGE.getContentRaw();

		String args[] = CONTENT.split("\\s+");

		try {

			if(MEMBER.getUser().isBot())
				return;

		}catch(Exception e) {}

		if(CONTENT.startsWith("<@!" + Bot.jda.getSelfUser().getId() + ">")) {
			MESSAGE.reply("My prefix is `u?`, do `u?help` for a list of commands!").queue();
			return;
		}

		if(!args[0].toLowerCase().startsWith("u?"))
			return;


		// Utils
		utils : switch(args[0].toLowerCase()) {



		case "u?help":
			if(args.length > 1) {
				MESSAGE.replyEmbeds(Help.help(args[1])).queue();
				break;
			}
			MESSAGE.replyEmbeds(Help.help("")).queue();
			break;



		case "u?botinfo":
			MESSAGE.replyEmbeds(BotInfo.botinfo()).queue();
			break;

			
			
		case "u?search":
			
			if(CONTENT.substring(8, CONTENT.length()).isBlank()) {
				MESSAGE.reply("Please enter a search query!").queue();
				break;
			}
			
			try {
				MESSAGE.replyEmbeds(Search.search(CONTENT.substring(8, CONTENT.length()))).queue();
				break;
			} catch (IOException e1) {
				e1.printStackTrace();
				MESSAGE.reply("Error getting links").queue();
				break;
			}


		case "u?channelinfo":
			List<TextChannel> CHANNELS = MESSAGE.getMentionedChannels();
			GuildChannel CHANNEL;

			try {  
				CHANNEL = Bot.jda.getGuildChannelById(Long.parseLong(args[1]));
			} catch(Exception e){  
				CHANNEL = null; 
			} 

			if(CHANNELS.size() > 0) {
				if(CHANNEL == null) 
					CHANNEL = CHANNELS.get(0);
			}else if(CHANNEL == null) {
				//				MESSAGE.reply("Getting info").queue(message -> {
				//					message.editMessage("").queue();
				//					message.editMessageEmbeds(ChannelInfo.textchannelinfo(event.getChannel())).queue();
				//				});
				MESSAGE.replyEmbeds(ChannelInfo.textchannelinfo(event.getChannel())).queue();
				break;
			}

			switch(CHANNEL.getType()) {

			case TEXT:
				MESSAGE.replyEmbeds(ChannelInfo.textchannelinfo(Bot.jda.getTextChannelById(CHANNEL.getId()))).queue();
				break utils;
			case VOICE:
				MESSAGE.replyEmbeds(ChannelInfo.voicechannelinfo(Bot.jda.getVoiceChannelById(CHANNEL.getId()))).queue();
				break utils;
			case CATEGORY:
				MESSAGE.replyEmbeds(ChannelInfo.categorychannelinfo(Bot.jda.getCategoryById(CHANNEL.getId()))).queue();
				break utils;
			default:
				MESSAGE.reply("Channel not recognized").queue();
				break utils;

			}

		case "u?roleinfo":
			List<Role> ROLES = MESSAGE.getMentionedRoles();
			Role ROLE;

			try {
				ROLE = Bot.jda.getRoleById(Long.parseLong(args[1]));
			}catch(Exception e) {
				ROLE = null;
			}

			if(ROLES.size() > 0) {
				if(ROLE == null)
					ROLE = ROLES.get(0);
			}else if(ROLE == null) {
				MESSAGE.reply("Please specify a role").queue();
				break;
			}

			MESSAGE.replyEmbeds(RoleInfo.roleinfo(ROLE)).queue();
			break;



		case "u?serverinfo":
			MESSAGE.replyEmbeds(ServerInfo.serverinfo(event.getGuild())).queue();
			break;



		case "u?userinfo":
			List<Member> USERS = MESSAGE.getMentionedMembers();
			Member USER;

			try {
				USER = event.getGuild().getMemberById(Long.parseLong(args[1]));
			}catch(Exception e) {
				USER = null;
			}

			if(USERS.size() > 0) {
				if(USER == null)
					USER = USERS.get(0);
			}else if(USER == null) {
				MESSAGE.replyEmbeds(UserInfo.userinfo(MEMBER)).queue();
				break;
			}

			MESSAGE.replyEmbeds(UserInfo.userinfo(USER)).queue();
			break;



		case "u?poll":
			CONTENT = CONTENT.substring(7, CONTENT.length());
			event.getChannel().sendMessage(event.getAuthor().getName() + " launched a poll:").complete();
			event.getChannel().sendMessageEmbeds(Polls.newpoll(CONTENT)).queue(message -> {
				message.addReaction("✅").queue();
				message.addReaction("❎").queue();
			});
			break;




		}


		// Bot
		switch (args[0].toLowerCase()) {



		case "u?about":
			MESSAGE.replyEmbeds(About.about()).queue();
			break;



		case "u?donate":
			MESSAGE.reply(Donate.donate()).queue();
			break;



		case "u?invite":
			MESSAGE.replyEmbeds(Invite.invite()).queue();
			break;



		case "u?ping":
			Bot.jda.getRestPing().queue(
					(ping) -> MESSAGE.replyFormat("**Reset ping:** %sms \n**WS ping:** %sms", ping, Bot.jda.getGatewayPing()).queue());
			break;



		case "u?uptime":
			MESSAGE.reply(Uptime.uptime()).queue();
			break;



		}


		// Entertainments
		switch (args[0].toLowerCase()) {



		case "u?clap":
			MESSAGE.reply(Clap.clap(args)).queue();
			break;



		case "u?color":
			MESSAGE.replyEmbeds(Colour.color()).queue();
			break;



		case "u?colorhex":
			MESSAGE.replyEmbeds(Colour.colorhex(args[1])).queue();
			break;



		case "u?colorrgb":
			if(args.length < 4) {
				MESSAGE.reply("Please provide r g b values").queue();
				break;
			}
			MESSAGE.replyEmbeds(Colour.colorrgb(args[1], args[2], args[3])).queue();
			break;



		case "u?echo":
			args[0] = "";
			MESSAGE.reply(Echo.echo(args)).queue();
			break;



		case "u?8ball":
			String qst = CONTENT.substring(7, CONTENT.length()).trim();
			if(!qst.isBlank()) {
				MESSAGE.reply(EightBall.eightball(qst)).queue();
				break;
			}
			MESSAGE.reply("Please specify a question!").queue();
			break;



		}
	}
}
