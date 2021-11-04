/*
 * Copyright 2021 Marsss (itsmarsss).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.marsss.utils;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import com.marsss.Bot;
import com.marsss.bot.*;
import com.marsss.entertainments.*;
import com.marsss.listeners.CommandListener;
import com.marsss.listeners.MusicListener;
import com.marsss.music.*;
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



		case "music":
			TITLE = "Music Commands";
			DESC = Join.getHelp() + "\n"
					+ Leave.getHelp() + "\n"
					+ Play.getHelp() + "\n"
					+ Playsc.getHelp() + "\n"
					+ Playlist.getHelp() + "\n"
					+ Playlistsc.getHelp() + "\n"
					+ Pause.getHelp() + "\n"
					+ Resume.getHelp() + "\n"
					+ Skip.getHelp() + "\n"
					+ Back.getHelp() + "\n"
					+ NowPlaying.getHelp() + "\n"
					+ Queue.getHelp() + "\n"
					+ Remove.getHelp() + "\n"
					+ Jump.getHelp() + "\n"
					+ Shuffle.getHelp() + "\n"
					+ Clear.getHelp() + "\n"
					+ Volume.getHelp() + "\n"
					+ Seek.getHelp() + "\n"
					+ FastForward.getHelp() + "\n"
					+ Rewind.getHelp() + "\n"
					+ MusicListener.announceHelp() + "\n"
					+ MusicListener.loopHelp();
			break;
			
			
			
		case "report":
			TITLE = "Report Commands";
			DESC = TCCallPairer.reportHelp() + "\n"
					+ VCCallPairer.reportHelp();
			break;

			
		case "mod":
			TITLE = "Mod";
			if(admin) {
				DESC = CommandListener.supportHelp() + "\n"
						+ CommandListener.blacklistHelp();
				break;
			}
			DESC = "You do not have permission to access this category.";
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
			DESC = Search.getHelp() + "\nWe use Duckduckgo, so [here](https://help.duckduckgo.com/duckduckgo-help-pages/results/syntax/) are the syntax for searching!";
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




		// Music

		switch(name) {

		case "join":
			TITLE = "Join";
			DESC = Join.getHelp();
			break;
		case "leave":
			TITLE = "Leave";
			DESC = Leave.getHelp();
			break;
		case "play":
			TITLE = "Play";
			DESC = Play.getHelp();
			break;
		case "playsc":
			TITLE = "Playsc";
			DESC = Playsc.getHelp();
		case "playlist":
			TITLE = "Playlist";
			DESC = Playlist.getHelp();
			break;
		case "playlistsc":
			TITLE = "Playlistsc";
			DESC = Playlistsc.getHelp();
			break;
		case "pause":
			TITLE = "Pause";
			DESC = Pause.getHelp();
			break;
		case "resume":
			TITLE = "Resume";
			DESC = Resume.getHelp();
			break;
		case "skip":
			TITLE = "Skip";
			DESC = Skip.getHelp();
			break;
		case "back":
			TITLE = "Back";
			DESC = Back.getHelp();
			break;
		case "nowplaying":
			TITLE = "NowPlaying";
			DESC = NowPlaying.getHelp();
			break;
		case "queue":
			TITLE = "Queue";
			DESC = Queue.getHelp();
			break;
		case "remove":
			TITLE = "Remove";
			DESC = Remove.getHelp();
			break;
		case "jump":
			TITLE = "Jump";
			DESC = Jump.getHelp();
			break;
		case "shuffle":
			TITLE = "Shuffle";
			DESC = Shuffle.getHelp();
			break;
		case "clear":
			TITLE = "Clear";
			DESC = Clear.getHelp();
			break;
		case "volume":
			TITLE = "Volume";
			DESC = Volume.getHelp();
			break;
		case "seek":
			TITLE = "Seek";
			DESC = Seek.getHelp();
			break;
		case "fastforward":
			TITLE = "FastForward";
			DESC = FastForward.getHelp();
			break;
		case "rewind":
			TITLE = "Rewind";
			DESC = Rewind.getHelp();
			break;
		case "announce":
			TITLE = "Announce";
			DESC = MusicListener.announceHelp();
			break;
		case "loop":
			TITLE = "Loop";
			DESC = MusicListener.loopHelp();
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
				.addField("Music", "all music commands will be in this category, do `" + Bot.Prefix + "help music` for more information", false)
				.setFooter("Type `" + Bot.Prefix + "help <category name>` to see category commands");
		if(admin) {
			CateEmd.addField("Moderator only", "all moderator commands will be in this category, do `" + Bot.Prefix + "help mod` for more information", false);
		}
		return CateEmd.build();
	}

	public static String getHelp() {
		return "`" + Bot.Prefix + "help` - help help help";
	}
}
