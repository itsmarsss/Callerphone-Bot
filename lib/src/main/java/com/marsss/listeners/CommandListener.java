package com.marsss.listeners;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import com.marsss.Bot;
import com.marsss.bot.*;
import com.marsss.entertainments.*;
import com.marsss.utils.*;

import net.dv8tion.jda.api.Permission;
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

		final Member MEMBER = event.getMember();
		final Message MESSAGE = event.getMessage();

		String CONTENT = MESSAGE.getContentRaw();

		final String args[] = CONTENT.split("\\s+");

		try {

			if(MEMBER.getUser().isBot())
				return;

		}catch(Exception e) {}

		if(CONTENT.startsWith("<@!" + Bot.jda.getSelfUser().getId() + ">")) {
			MESSAGE.reply("My prefix is `" + Bot.Prefix + "`, do `" + Bot.Prefix + "help` for a list of commands!").queue();
			return;
		}

		if(!args[0].toLowerCase().startsWith(Bot.Prefix))
			return;


		// Utils
		utils : switch(args[0].toLowerCase().replace(Bot.Prefix, "")) {



		case "help":
			if(args.length > 1) {
				MESSAGE.replyEmbeds(Help.help(args[1])).queue();
				break;
			}
			MESSAGE.replyEmbeds(Help.help("")).queue();
			break;



		case "botinfo":
			MESSAGE.replyEmbeds(BotInfo.botinfo()).queue();
			break;



		case "search":

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


		case "channelinfo":
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

		case "roleinfo":
			final List<Role> ROLES = MESSAGE.getMentionedRoles();
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



		case "serverinfo":
			MESSAGE.replyEmbeds(ServerInfo.serverinfo(event.getGuild())).queue();
			break;



		case "userinfo":
			final List<Member> USERS = MESSAGE.getMentionedMembers();
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



		case "poll":
			if(!event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_ADD_REACTION)) {
				MESSAGE.reply("I need `Add Reaction` permission for this command to work").queue();
				break;
			}
			CONTENT = CONTENT.substring(7, CONTENT.length());
			event.getChannel().sendMessage(event.getAuthor().getName() + " launched a poll:").complete();
			event.getChannel().sendMessageEmbeds(Polls.newpoll(CONTENT)).queue(message -> {
				message.addReaction("✅").queue();
				message.addReaction("❎").queue();
			});
			break;




		}


		// Bot
		switch (args[0].toLowerCase().replace(Bot.Prefix, "")) {



		case "about":
			MESSAGE.replyEmbeds(About.about()).queue();
			break;



		case "donate":
			MESSAGE.reply(Donate.donate()).queue();
			break;



		case "invite":
			MESSAGE.replyEmbeds(Invite.invite()).queue();
			break;



		case "ping":
			Bot.jda.getRestPing().queue(
					(ping) -> MESSAGE.replyFormat("**Reset ping:** %sms \n**WS ping:** %sms", ping, Bot.jda.getGatewayPing()).queue());
			break;



		case "uptime":
			MESSAGE.reply(Uptime.uptime()).queue();
			break;



		}


		// Entertainments
		switch (args[0].toLowerCase().replace(Bot.Prefix, "")) {



		case "clap":
			MESSAGE.reply(Clap.clap(args)).queue();
			break;



		case "u?color":
			MESSAGE.replyEmbeds(Colour.color()).queue();
			break;



		case "colorhex":
			MESSAGE.replyEmbeds(Colour.colorhex(args[1])).queue();
			break;



		case "colorrgb":
			if(args.length < 4) {
				MESSAGE.reply("Please provide r g b values").queue();
				break;
			}
			MESSAGE.replyEmbeds(Colour.colorrgb(args[1], args[2], args[3])).queue();
			break;



		case "echo":
			args[0] = "";
			MESSAGE.reply(Echo.echo(args)).queue();
			break;



		case "8ball":
			final String qst = CONTENT.substring(7, CONTENT.length()).trim();
			if(!qst.isBlank()) {
				MESSAGE.reply(EightBall.eightball(qst)).queue();
				break;
			}
			MESSAGE.reply("Please specify a question!").queue();
			break;



		}


		if(event.getChannel().getId().equals("798693573616205855")) {
			String id = args[1];
			switch (args[0].toLowerCase().replace(Bot.Prefix, "")) {

			case "blacklist":
				if(Bot.blacklist.contains(id)) {
					MESSAGE.reply("ID blacklisted already").queue();
				}else {
					Bot.blacklist.add(id);
					try {
						FileWriter myWriter = new FileWriter(Bot.parent + "\\blacklist.txt");
						myWriter.write(id);
						myWriter.close();
						MESSAGE.reply("ID: `" + id + "` added to blacklist").queue();
					} catch (IOException e) {
						MESSAGE.reply("An error occured").queue();
					}
				}
				break;

			case "support":
				if(Bot.supporter.contains(id)) {
					MESSAGE.reply("ID is supporter already").queue();
				}else {
					Bot.supporter.add(id);
					try {
						FileWriter myWriter = new FileWriter(Bot.parent + "\\support.txt");
						myWriter.write(id);
						myWriter.close();
						MESSAGE.reply("ID: `" + id + "` added to supporter list").queue();
					} catch (IOException e) {
						MESSAGE.reply("An error occured").queue();
					}
				}
				break;

			case "mod":
				if(Bot.admin.contains(id)) {
					MESSAGE.reply("ID is mod already").queue();
				}else {
					Bot.admin.add(id);
					try {
						FileWriter myWriter = new FileWriter(Bot.parent + "\\admin.txt");
						myWriter.write(id);
						myWriter.close();
						MESSAGE.reply("ID: `" + id + "` added to mod list").queue();
					} catch (IOException e) {
						MESSAGE.reply("An error occured").queue();
					}
				}
				break;
				
				
				
				
			case "rblacklist":
				if(!Bot.blacklist.contains(id)) {
					MESSAGE.reply("ID not on blacklist").queue();
				}else {
					Bot.blacklist.remove(id);
					StringBuilder sb = new StringBuilder();
					try {
						for(String m : Bot.blacklist) {
							sb.append(m + "\n");
						}
						PrintWriter myWriter = new PrintWriter(Bot.parent + "\\blacklist.txt");
						myWriter.print(sb.toString());
						myWriter.close();
						MESSAGE.reply("ID: `" + id + "` removed from blacklist").queue();
					} catch (IOException e) {
						MESSAGE.reply("An error occured").queue();
					}
				}
				break;

			case "rsupport":
				if(!Bot.supporter.contains(id)) {
					MESSAGE.reply("ID is not a supporter").queue();
				}else {
					Bot.supporter.remove(id);
					StringBuilder sb = new StringBuilder();
					try {
						for(String m : Bot.supporter) {
							sb.append(m + "\n");
						}
						PrintWriter myWriter = new PrintWriter(Bot.parent + "\\support.txt");
						myWriter.print(sb.toString());
						myWriter.close();
						MESSAGE.reply("ID: `" + id + "` removed from supporter list").queue();
					} catch (IOException e) {
						MESSAGE.reply("An error occured").queue();
					}
				}
				break;

			case "rmod":
				if(!Bot.admin.contains(id)) {
					MESSAGE.reply("ID is not a mod").queue();
				}else {
					Bot.admin.remove(id);
					StringBuilder sb = new StringBuilder();
					try {
						for(String m : Bot.admin) {
							sb.append(m + "\n");
						}
						PrintWriter myWriter = new PrintWriter(Bot.parent + "\\admin.txt");
						myWriter.print(sb.toString());
						myWriter.close();
						MESSAGE.reply("ID: `" + id + "` removed from mod list").queue();
					} catch (IOException e) {
						MESSAGE.reply("An error occured").queue();
					}
				}
				break;
			}
		}
	}
}
