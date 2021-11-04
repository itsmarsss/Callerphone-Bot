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
package com.marsss.bot;

import java.awt.Color;

import com.marsss.Bot;
import com.marsss.entertainments.Colour;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class Invite {

	public static MessageEmbed invite() {
		final Color COLOR = Colour.randColor();
		EmbedBuilder InvEmd = new EmbedBuilder()
				.setColor(COLOR)
				.addField("Add me to your server", "[Invite Link](https://discord.com/api/oauth2/authorize?client_id=849713468348956692&permissions=49663040&scope=bot%20applications.commands)", true)
				.addField("Join the Community and Support Server", "[Server Link](https://discord.gg/jcYKsfw48p)", true)
				.addField("Support Us", "[Patreon Link](https://www.patreon.com/itsmarsss)", true)
				.setFooter("Have a nice day");
		return InvEmd.build();
	}

	public static String getHelp() {
		return "`" + Bot.Prefix + "invite` - Get invites and links related to this bot.";
	}
	
}
