package com.marsss.utils;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import com.marsss.bot.*;
import com.marsss.entertainments.*;
import com.marsss.vcuserphone.*;

public class Help {
	public static MessageEmbed help(String name) {
		if(name == "") {
			return helpCategories();
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
					+ ServerInfo.getHelp() + "\n"
					+ UserInfo.getHelp();
			break;



		case "vccall":
			TITLE = "VCCall Commands";
			DESC = VCCallPairer.callHelp() + "\n"
					+ VCCallPairer.hangupHelp();
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



		// Userphone

		switch(name) {



		case "call":
			TITLE = "Call";
			DESC = VCCallPairer.callHelp();
			break;
		case "hangup":
			TITLE = "Hangup";
			DESC = VCCallPairer.hangupHelp();
			break;

			
			
		}



		Color COLOR = Colour.randColor();
		EmbedBuilder HelpEmd = new EmbedBuilder()
				.setTitle(TITLE)
				.setDescription(DESC)
				.setFooter("Hope you found this useful!")
				.setColor(COLOR);

		return HelpEmd.build();
	}

	private static MessageEmbed helpCategories() {
		Color COLOR = Colour.randColor();
		EmbedBuilder CateEmd = new EmbedBuilder()
				.setColor(COLOR)
				.setTitle("Categories")
				.addField("Bot", "all commands related to the bot will be here, do `u?help bot` for more information", false)
				.addField("Entertainment", "all entertainment commands will be in this category, do `u?help entertainment` for more information", false)
				.addField("Utils", "all utility commands will be in this category, do `u?help utils` for more information", false)
				.addField("VCUserphone", "all voice call userphone commands will be in this category, do `u?help vccall` for more information", false)
				.setFooter("Type `u?help <category name>` to see their commands");

		return CateEmd.build();
	}

	public static String getHelp() {
		return "`help` - help help help";
	}
}
