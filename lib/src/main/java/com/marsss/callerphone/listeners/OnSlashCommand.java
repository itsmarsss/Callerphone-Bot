package com.marsss.callerphone.listeners;

import java.io.IOException;

import com.marsss.callerphone.Bot;
import com.marsss.callerphone.bot.*;
import com.marsss.entertainments.*;
import com.marsss.callerphone.utils.BotInfo;
import com.marsss.callerphone.utils.Help;
import com.marsss.callerphone.utils.Polls;
import com.marsss.callerphone.utils.Search;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class OnSlashCommand extends ListenerAdapter {
	public void onSlashCommand(SlashCommandEvent event) {
		if(!event.isFromGuild()) {
			privateChannel(event);
			return;
		}
		final Member MEMBER = event.getMember();

		try {

			if(MEMBER.getUser().isBot())
				return;

		}catch(Exception e) {}

		// Utils
		switch(event.getName()) {



		case "help":
			if(!event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
				event.reply("I need `Embed Links` permission for this command to work").setEphemeral(true).queue();
				break;
			}
			boolean admin = false;
			if(Bot.admin.contains(MEMBER.getId())) {
				admin = true;
			}
			try {
				event.replyEmbeds(Help.help(event.getOption("command").getAsString(), admin)).queue();
			}catch(Exception e) {
				event.replyEmbeds(Help.help("", admin)).queue();
			}
			break;



		case "botinfo":
			if(!event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
				event.reply("I need `Embed Links` permission for this command to work").setEphemeral(true).queue();
				break;
			}
			event.replyEmbeds(BotInfo.botinfo()).queue();
			break;



		case "search":
			if(!event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
				event.reply("I need `Embed Links` permission for this command to work").setEphemeral(true).queue();
				break;
			}

			try {
				event.replyEmbeds(Search.search(" " + event.getOption("query").getAsString())).queue();
				break;
			} catch (IOException e1) {
				e1.printStackTrace();
				event.reply("Error getting links").setEphemeral(true).queue();
				break;
			}

		case "poll":
			if(!event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_ADD_REACTION)) {
				event.reply("I need `Add Reaction` permission for this command to work").setEphemeral(true).queue();
				break;
			}
			if(!event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
				event.reply("I need `Embed Links` permission for this command to work").setEphemeral(true).queue();
				break;
			}
			event.reply(MEMBER.getEffectiveName() + " launched a poll:").complete();
			event.getChannel().sendMessageEmbeds(Polls.newpoll(event.getOption("question").getAsString())).queue(message -> {
				message.addReaction("✅").queue();
				message.addReaction("❎").queue();
			});
			break;


		case "about":
			if(!event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
				event.reply("I need `Embed Links` permission for this command to work").setEphemeral(true).queue();
				break;
			}
			event.replyEmbeds(About.about()).queue();
			break;



		case "donate":
			if(!event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
				event.reply("I need `Embed Links` permission for this command to work").setEphemeral(true).queue();
				break;
			}
			event.reply(Donate.donate()).queue();
			break;



		case "invite":
			if(!event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
				event.reply("I need `Embed Links` permission for this command to work").setEphemeral(true).queue();
				break;
			}
			event.replyEmbeds(Invite.invite()).queue();
			break;



		case "ping":
			Bot.jda.getRestPing().queue(
					(ping) -> event.replyFormat("**Reset ping:** %sms \n**WS ping:** %sms", ping, Bot.jda.getGatewayPing()).queue());
			break;



		case "uptime":
			event.reply(Uptime.uptime()).queue();
			break;
			
			

		case "color":
			if(!event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
				event.reply("I need `Embed Links` permission for this command to work").setEphemeral(true).queue();
				break;
			}
			event.replyEmbeds(Colour.color()).queue();
			break;



		case "colorhex":
			if(!event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
				event.reply("I need `Embed Links` permission for this command to work").setEphemeral(true).queue();
				break;
			}

			event.replyEmbeds(Colour.colorhex(event.getOption("hex").getAsString())).queue();
			break;



		case "colorrgb":
			if(!event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
				event.reply("I need `Embed Links` permission for this command to work").setEphemeral(true).queue();
				break;
			}

			event.replyEmbeds(Colour.colorrgb(event.getOption("r").getAsString(), event.getOption("g").getAsString(), event.getOption("b").getAsString())).queue();
			break;

			
		}
	}
	private static void privateChannel(SlashCommandEvent event) {
		final Member MEMBER = event.getMember();

		try {

			if(MEMBER.getUser().isBot())
				return;

		}catch(Exception e) {}

		// Utils
		switch(event.getName()) {



		case "help":
			boolean admin = false;
			if(Bot.admin.contains(MEMBER.getId())) {
				admin = true;
			}
			event.replyEmbeds(Help.help(event.getOption("command").getAsString(), admin)).queue();
			break;



		case "botinfo":
			event.replyEmbeds(BotInfo.botinfo()).queue();
			break;



		case "search":
			try {
				event.replyEmbeds(Search.search(" " + event.getOption("query").getAsString())).queue();
				break;
			} catch (IOException e1) {
				e1.printStackTrace();
				event.reply("Error getting links").setEphemeral(true).queue();
				break;
			}



		case "poll":
			event.reply("Poll all by yourself ;-;").queue();
			break;


		case "about":
			event.replyEmbeds(About.about()).queue();
			break;



		case "donate":
			event.reply(Donate.donate()).queue();
			break;



		case "invite":
			event.replyEmbeds(Invite.invite()).queue();
			break;



		case "ping":
			Bot.jda.getRestPing().queue(
					(ping) -> event.replyFormat("**Reset ping:** %sms \n**WS ping:** %sms", ping, Bot.jda.getGatewayPing()).queue());
			break;



		case "uptime":
			event.reply(Uptime.uptime()).queue();
			break;


			

		case "color":
			event.replyEmbeds(Colour.color()).queue();
			break;



		case "colorhex":
			event.replyEmbeds(Colour.colorhex(event.getOption("hex").getAsString())).queue();
			break;



		case "colorrgb":
			event.replyEmbeds(Colour.colorrgb(event.getOption("r").getAsString(), event.getOption("g").getAsString(), event.getOption("b").getAsString())).queue();
			break;



		}
	}
}
