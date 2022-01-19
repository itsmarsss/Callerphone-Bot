package com.marsss.utils;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import com.marsss.Bot;
import com.marsss.bot.*;
import com.marsss.entertainments.*;
import com.marsss.tccallerphone.*;
import com.marsss.vccallerphone.*;

public class Help {
	public static MessageEmbed help(String name, boolean admin) {
		if(name == "") {
			return helpCategories(admin);
		}

		String TITLE = "Sorry.";

		String DESC = "I don't recognize that category/command :(";
		name = name.toLowerCase();


		// Categories

		switch(name) {



		case "bot":
			TITLE = "Bot Commands";
			DESC = About.getHelp() + "\n"
					+ Donate.getHelp() + "\n"
					+ Invite.getHelp() + "\n"
					+ Ping.getHelp() + "\n"
					+ Uptime.getHelp();
			break;



		case "entertainment":
			TITLE = "Entertaiment Commands";
			DESC = Clap.getHelp() + "\n"
					+ Colour.getHelp() + "\n"
					+ Echo.getHelp() + "\n"
					+ EightBall.getHelp();
			break;



		case "utils":
			TITLE = "Util Commands";
			DESC = BotInfo.getHelp() + "\n"
					+ ChannelInfo.getHelp() + "\n"
					+ Help.getHelp() + "\n"
					+ Polls.getHelp() + "\n"
					+ RoleInfo.getHelp() + "\n"
					+ Search.getHelp() + "\n"
					+ ServerInfo.getHelp() + "\n"
					+ UserInfo.getHelp();
			break;



		case "tccall":
			TITLE = "TCCall Commands";
			DESC = TCCallPairer.callHelp() + "\n"
					+ TCCallPairer.uncenscallHelp() + "\n"
					+ TCCallPairer.hangupHelp() + "\n"
					+ TCCallPairer.reportHelp();
			break;



		case "vccall":
			TITLE = "VCCall Commands";
			DESC = VCCallPairer.callHelp() + "\n"
					+ VCCallPairer.hangupHelp() + "\n"
					+ VCCallPairer.muteHelp() + "\n"
					+ VCCallPairer.unmuteHelp() + "\n"
					+ VCCallPairer.deafenHelp() + "\n"
					+ VCCallPairer.undeafenHelp() + "\n"
					+ VCCallPairer.reportHelp();
			break;
			
			
		case "report":
			TITLE = "Report Commands";
			DESC = TCCallPairer.reportHelp() + "\n"
					+ VCCallPairer.reportHelp();
			break;
			
			
		case "music":
			TITLE = "Music Commands";
			DESC = "Callerphone no longer can play music, however I've created a new bot called **Tunes**...\nJoin [this](" + Bot.tunessupport + ") server for more information!";
			break;

			
		}


		// Bot

		switch(name) {



		case "donate":
			TITLE = "Donate";
			DESC = Donate.getHelp();
			break;
		case "invite":
			TITLE = "Invite";
			DESC = Invite.getHelp();
			break;
		case "ping":
			TITLE = "Ping";
			DESC = Ping.getHelp();
			break;
		case "Uptime":
			TITLE = "Uptime";
			DESC = Uptime.getHelp();
			break;



		}



		// Entertainments

		switch(name) {



		case "clap":
			TITLE = "Clap";
			DESC = Clap.getHelp();
			break;
		case "color":
			TITLE = "Color";
			DESC = Colour.getHelp();
			break;
		case "echo":
			TITLE = "Echo";
			DESC = Echo.getHelp();
			break;
		case "eightball":
			TITLE = "8Ball";
			DESC = EightBall.getHelp();
			break;



		}



		// Utils

		switch(name) {



		case "botinfo":
			TITLE = "Botinfo";
			DESC = BotInfo.getHelp();
			break;
		case "channelinfo":
			TITLE = "Channelinfo";
			DESC = ChannelInfo.getHelp();
			break;
		case "help":
			TITLE = "Help";
			DESC = Help.getHelp();
			break;
		case "poll":
			TITLE = "Poll";
			DESC = Polls.getHelp();
			break;
		case "search":
			TITLE = "Search";
			DESC = Search.getHelp() + "\nWe use Duckduckgo, so click [here](https://help.duckduckgo.com/duckduckgo-help-pages/results/syntax/) for searching syntax!";
			break;
		case "roleinfo":
			TITLE = "Roleinfo";
			DESC = RoleInfo.getHelp();
			break;
		case "serverinfo":
			TITLE = "Serverinfo";
			DESC = ServerInfo.getHelp();
			break;
		case "userinfo":
			TITLE = "Userinfo";
			DESC = UserInfo.getHelp();
			break;



		}



		// Voicephone

		switch(name) {



		case "call":
			TITLE = "Call";
			DESC = VCCallPairer.callHelp();
			break;
		case "hangup":
			TITLE = "Hangup";
			DESC = VCCallPairer.hangupHelp();
			break;
		case "mute":
			TITLE = "Mute";
			DESC = VCCallPairer.muteHelp();
			break;
		case "unmute":
			TITLE = "Unmute";
			DESC = VCCallPairer.unmuteHelp();
			break;
		case "deafen":
			TITLE = "Deafen";
			DESC = VCCallPairer.deafenHelp();
			break;
		case "undeafen":
			TITLE = "Undeafen";
			DESC = VCCallPairer.undeafenHelp();
			break;
		case "reportcall":
			TITLE = "Report";
			DESC = VCCallPairer.reportHelp();
			break;


		}




		// Textphone

		switch(name) {



		case "chat":
			TITLE = "Chat";
			DESC = TCCallPairer.callHelp();
			break;
		case "chatuncens":
			TITLE = "UncensoredChatCall";
			DESC = TCCallPairer.uncenscallHelp();
			break;
		case "endchat":
			TITLE = "Endchat";
			DESC = TCCallPairer.hangupHelp();
			break;
		case "reportchat":
			TITLE = "Reportchat";
			DESC = TCCallPairer.reportHelp();
			break;

		}


		EmbedBuilder HelpEmd = new EmbedBuilder()
				.setTitle(TITLE)
				.setDescription(DESC)
				.setFooter("Hope you found this useful!", Bot.jda.getSelfUser().getAvatarUrl())
				.setColor(new Color(114, 137, 218));

		return HelpEmd.build();
	}

	private static MessageEmbed helpCategories(boolean admin) {
		EmbedBuilder CateEmd = new EmbedBuilder()
				.setColor(new Color(114, 137, 218))
				.setTitle("Categories")
				.addField("Bot", "all commands related to the bot will be here, do `" + Bot.Prefix + "help bot` for more information", false)
				.addField("Entertainment", "all entertainment commands will be in this category, do `" + Bot.Prefix + "help entertainment` for more information", false)
				.addField("Utils", "all utility commands will be in this category, do `" + Bot.Prefix + "help utils` for more information", false)
				.addField("TC Callerphone", "all text call callerphone commands will be in this category, do `" + Bot.Prefix + "help tccall` for more information", false)
				.addField("VC Callerphone", "all voice call callerphone commands will be in this category, do `" + Bot.Prefix + "help vccall` for more information", false)
				.addField("Music", "Callerphone no longer can play music, however I've created a new bot called **Tunes**... Join [this](https:discord.gg/TyHaxtWAmX) server for more information!", false)
				.setFooter("Type `" + Bot.Prefix + "help <category name>` to see category commands");
		if(admin) {
			CateEmd.addField("Moderator only", "all moderator commands will be in this category, do `" + Bot.Prefix + "help mod` in dm for more information", false);
		}
		return CateEmd.build();
	}

	public static String getHelp() {
		return "`" + Bot.Prefix + "help` - help help help";
	}
}
