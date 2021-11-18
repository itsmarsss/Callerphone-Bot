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

		if(args[0].toLowerCase().startsWith("c?play")) {
			MESSAGE.reply("Callerphone no longer can play music, however I've created a new bot called **Tunes**...\nJoin <" + Bot.tunessupport + "> for more information!").queue();
			return;
		}
		

		// Utils
		utils : switch(args[0].toLowerCase().replace(Bot.Prefix, "")) {



		case "help":
			if(!event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
				MESSAGE.reply("I need `Embed Links` permission for this command to work").queue();
				break;
			}
			boolean admin = false;
			if(Bot.admin.contains(event.getAuthor().getId())) {
				admin = true;
			}
			if(args.length > 1) {
				
				MESSAGE.replyEmbeds(Help.help(args[1], admin)).queue();
				break;
			}

			MESSAGE.replyEmbeds(Help.help("", admin)).queue();
			break;



		case "botinfo":
			if(!event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
				MESSAGE.reply("I need `Embed Links` permission for this command to work").queue();
				break;
			}
			MESSAGE.replyEmbeds(BotInfo.botinfo()).queue();
			break;



		case "search":
			if(!event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
				MESSAGE.reply("I need `Embed Links` permission for this command to work").queue();
				break;
			}
			if(CONTENT.substring(8, CONTENT.length()).equals("")) {
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
			if(!event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
				MESSAGE.reply("I need `Embed Links` permission for this command to work").queue();
				break;
			}
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
			if(!event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
				MESSAGE.reply("I need `Embed Links` permission for this command to work").queue();
				break;
			}
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
			if(!event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
				MESSAGE.reply("I need `Embed Links` permission for this command to work").queue();
				break;
			}
			MESSAGE.replyEmbeds(ServerInfo.serverinfo(event.getGuild())).queue();
			break;



		case "userinfo":
			if(!event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
				MESSAGE.reply("I need `Embed Links` permission for this command to work").queue();
				break;
			}
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
			if(!event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
				MESSAGE.reply("I need `Embed Links` permission for this command to work").queue();
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
			if(!event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
				MESSAGE.reply("I need `Embed Links` permission for this command to work").queue();
				break;
			}
			MESSAGE.replyEmbeds(About.about()).queue();
			break;



		case "donate":
			if(!event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
				MESSAGE.reply("I need `Embed Links` permission for this command to work").queue();
				break;
			}
			MESSAGE.reply(Donate.donate()).queue();
			break;



		case "invite":
			if(!event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
				MESSAGE.reply("I need `Embed Links` permission for this command to work").queue();
				break;
			}
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



		case "color":
			if(!event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
				MESSAGE.reply("I need `Embed Links` permission for this command to work").queue();
				break;
			}
			MESSAGE.replyEmbeds(Colour.color()).queue();
			break;



		case "colorhex":
			if(!event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
				MESSAGE.reply("I need `Embed Links` permission for this command to work").queue();
				break;
			}
			if(args.length < 2) {
				MESSAGE.reply("Please provide hex value").queue();
			}
			MESSAGE.replyEmbeds(Colour.colorhex(args[1])).queue();
			break;



		case "colorrgb":
			if(!event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
				MESSAGE.reply("I need `Embed Links` permission for this command to work").queue();
				break;
			}
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
			if(!qst.equals("")) {
				MESSAGE.reply(EightBall.eightball(qst)).queue();
				break;
			}
			MESSAGE.reply("Please specify a question!").queue();
			break;



		}
		


		if(Bot.admin.contains(event.getAuthor().getId())) {
			try {
				String id = args[1];
				switch (args[0].toLowerCase().replace(Bot.Prefix, "")) {

				case "blacklist":
					if(Bot.blacklist.contains(id)) {
						MESSAGE.reply("ID blacklisted already").queue();
					}else {
						Bot.blacklist.add(id);
						StringBuilder sb = new StringBuilder();
						try {
							for(String m : Bot.blacklist) {
								sb.append(m + "\n");
							}
							FileWriter myWriter = new FileWriter(Bot.parent + "/blacklist.txt");
							myWriter.write(sb.toString());
							myWriter.close();
							MESSAGE.reply("ID: `" + id + "` added to blacklist").queue();
						} catch (IOException e) {
							MESSAGE.reply("An error occured").queue();
						}
					}
					break;

				case "prefix":
					if(Bot.prefix.containsKey(id)) {
						MESSAGE.reply("ID has prefix already").queue();
					}else {
						String prefix = args[2];
						if(prefix.length() > 15) {
							MESSAGE.reply("Prefix too long (max. length = 15)").queue();
							break;
						}
						Bot.prefix.put(id, prefix);
						StringBuilder sb = new StringBuilder();
						try {
							for(String key : Bot.prefix.keySet()) {
								sb.append(key + "|" + Bot.prefix.get(key) + "\n");
							}
							FileWriter myWriter = new FileWriter(Bot.parent + "/prefix.txt");
							myWriter.write(sb.toString());
							myWriter.close();
							MESSAGE.reply("ID: `" + id + "` now has prefix `" + prefix + "`").queue();
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
						StringBuilder sb = new StringBuilder();
						try {
							for(String m : Bot.admin) {
								sb.append(m + "\n");
							}
							FileWriter myWriter = new FileWriter(Bot.parent + "/admin.txt");
							myWriter.write(sb.toString());
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
							PrintWriter myWriter = new PrintWriter(Bot.parent + "/blacklist.txt");
							myWriter.print(sb.toString());
							myWriter.close();
							MESSAGE.reply("ID: `" + id + "` removed from blacklist").queue();
						} catch (IOException e) {
							MESSAGE.reply("An error occured").queue();
						}
					}
					break;

				case "rprefix":
					if(!Bot.prefix.containsKey(id)) {
						MESSAGE.reply("ID does not have a prefix").queue();
					}else {
						Bot.prefix.remove(id);
						StringBuilder sb = new StringBuilder();
						try {
							for(String key : Bot.prefix.keySet()) {
								sb.append(key + "|" + Bot.prefix.get(key) + "\n");
							}
							PrintWriter myWriter = new PrintWriter(Bot.parent + "/prefix.txt");
							myWriter.print(sb.toString());
							myWriter.close();
							MESSAGE.reply("ID: `" + id + "` no longer has a prefix").queue();
						} catch (IOException e) {
							MESSAGE.reply("An error occured").queue();
						}
					}
					break;

				case "rmod":
					if(!Bot.admin.contains(id)) {
						MESSAGE.reply("ID is not a mod").queue();
					}else {
						if(id.equals("841028865995964477")) {
							MESSAGE.reply("You cannot remove this mod").queue();
							break;
						}
						Bot.admin.remove(id);
						StringBuilder sb = new StringBuilder();
						try {
							for(String m : Bot.admin) {
								sb.append(m + "\n");
							}
							PrintWriter myWriter = new PrintWriter(Bot.parent + "/admin.txt");
							myWriter.print(sb.toString());
							myWriter.close();
							MESSAGE.reply("ID: `" + id + "` removed from mod list").queue();
						} catch (IOException e) {
							MESSAGE.reply("An error occured").queue();
						}
					}
					break;
				}
			} catch(Exception e) {}
		}
	}
	
	public static String supportHelp() {
		return "`" + Bot.Prefix + "prefix <id> <prefix>` - Give user a prefix.\n" +
				"`" + Bot.Prefix + "rprefix <id>` - Removes user prefix.";
	}
	
	public static String adminHelp() {
		return "`" + Bot.Prefix + "mod <id>` - Adds id to mod list.\n" +
				"`" + Bot.Prefix + "rmod <id>` - Removes id from mod list.";
	}
	
	public static String blacklistHelp() {
		return "`" + Bot.Prefix + "blacklist <id>` - Adds id to blacklist.\n" +
				"`" + Bot.Prefix + "rblacklist <id>` - Removes id from blacklist.";
	}
	
}
