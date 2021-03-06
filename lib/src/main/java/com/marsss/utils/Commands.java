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

import com.marsss.bot.*;
import com.marsss.entertainments.*;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class Commands {
	public static MessageEmbed commands() {
		String SPLIT = "\n—\n";
		EmbedBuilder CmdEmd = new EmbedBuilder()
				.setTitle("**All Commands**")
				.addField("**Utils**",
						Commands.getHelp()
						+ SPLIT
						+ Help.getHelp()
						+ SPLIT
						+ Ping.getHelp()
						+ SPLIT
						+ Uptime.getHelp()
						+ SPLIT
						+ UserInfo.getHelp()
						+ SPLIT
						+ BotInfo.getHelp(), true)
				.addField("**Entertainments**",
						Clap.getHelp()
						+ SPLIT
						+ Colour.getHelp()
						+ SPLIT
						+ Echo.getHelp()
						+ SPLIT
						+ EightBall.getHelp()
						+ SPLIT
						+ Polls.getHelp(), true
						);

		return CmdEmd.build();
	}

	public static String getHelp() {
		return "`commands` - Gives you a list of commands";
	}
}
