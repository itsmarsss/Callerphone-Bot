package com.marsss.listeners;

import java.io.IOException;
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

			if(MEMBER.getUser().isBot() || MEMBER.getUser().isSystem())
				return;

		}catch(Exception e) {}

		if(CONTENT.startsWith("<@!" + Bot.jda.getSelfUser().getId() + ">")) {
			MESSAGE.reply("My prefix is `" + Bot.Prefix + "`, do `" + Bot.Prefix + "help` for a list of commands!").queue();
			return;
		}

		if(!args[0].toLowerCase().startsWith(Bot.Prefix))
			return;

		if(args[0].toLowerCase().startsWith(Bot.Prefix + "play")) {
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



		}
	}
	
	public static String adminHelp() {
		return "`" + Bot.Prefix + "mod <id>` - Adds id to mod list.\n" +
				"`" + Bot.Prefix + "rmod <id>` - Removes id from mod list.";
	}
	
	public static String blacklistHelp() {
		return "`" + Bot.Prefix + "blacklist <id>` - Adds id to blacklist.\n" +
				"`" + Bot.Prefix + "rblacklist <id>` - Removes id from blacklist.";
	}
	
	public static String supportHelp() {
		return "`" + Bot.Prefix + "prefix <id> <prefix>` - Give user a prefix.\n" +
				"`" + Bot.Prefix + "rprefix <id>` - Removes user prefix.";
	}

	public static String showItemsHelp() {
		return "`" + Bot.Prefix + "blackedlist` - Shows all black listed users.\n" +
				"`" + Bot.Prefix + "prefixlist` - Shows all prefixes for users.\n" +
				"`" + Bot.Prefix + "infolist` - Shows all info for startup.\n" +
				"`" + Bot.Prefix + "modlist` - Shows all moderators.\n" +
				"`" + Bot.Prefix + "filterlist` - Shows all chat filters.";
	}

}
