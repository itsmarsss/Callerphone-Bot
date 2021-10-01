package com.marsss.listeners;

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

		}catch(NullPointerException npe) {}

		if(!args[0].startsWith(";"))
			return;

		// Utils
		utils : switch(args[0]) {



		case ";help":
			MESSAGE.replyEmbeds(Help.help(args[1])).queue();
			break;



		case ";botinfo":
			MESSAGE.replyEmbeds(BotInfo.botinfo()).queue();
			break;



		case ";channelinfo":
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

		case ";roleinfo":
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



		case ";serverinfo":
			MESSAGE.replyEmbeds(ServerInfo.serverinfo(event.getGuild())).queue();
			break;



		case ";userinfo":
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



		case ";poll":
			CONTENT = CONTENT.replace(";poll ", "");
			event.getChannel().sendMessage(event.getAuthor().getName() + " launched a poll:").complete();
			event.getChannel().sendMessageEmbeds(Polls.newpoll(CONTENT)).queue(message -> {
				message.addReaction("✅").queue();
				message.addReaction("❌").queue();
			});
			break;




		}


		// Bot
		switch (args[0]) {



		case ";donate":
			MESSAGE.reply(Donate.donate()).queue();
			break;



		case ";invite":
			MESSAGE.replyEmbeds(Invite.invite()).queue();
			break;



		case ";ping":
			Bot.jda.getRestPing().queue(
					(ping) -> MESSAGE.replyFormat("**Reset ping:** %sms \n**WS ping:** %sms", ping, Bot.jda.getGatewayPing()).queue());
			break;



		case ";uptime":
			MESSAGE.reply(Uptime.uptime()).queue();
			break;



		}


		// Entertainments
		switch (args[0]) {



		case ";clap":
			args[0] = "";
			MESSAGE.reply(Clap.clap(args)).queue();
			break;



		case ";color":
			MESSAGE.replyEmbeds(Colour.color()).queue();
			break;



		case ";colorhex":
			MESSAGE.replyEmbeds(Colour.colorhex(args[1])).queue();
			break;



		case ";colorrgb":
			MESSAGE.replyEmbeds(Colour.colorrgb(args[1], args[2], args[3])).queue();
			break;




		case ";echo":
			args[0] = "";
			MESSAGE.reply(Echo.echo(args)).queue();
			break;



		case ";eightball":
			CONTENT = CONTENT.replace(";eightball ", "");
			MESSAGE.reply(EightBall.eightball(CONTENT)).queue();
			break;



		}
	}
}
