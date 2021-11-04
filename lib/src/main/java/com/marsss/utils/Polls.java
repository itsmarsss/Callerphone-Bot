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

import com.marsss.Bot;
import com.marsss.entertainments.Colour;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class Polls {
	public static MessageEmbed newpoll(String qst) {
		final Color COLOR = Colour.randColor();
		EmbedBuilder PollEmd = new EmbedBuilder()
				.setTitle("**POLL**")
				.setColor(COLOR)
				.setDescription(qst.toString());

		return PollEmd.build();
	}

	public static String getHelp() {
		return "`" + Bot.Prefix + "poll <msg>` - Create a poll for server members to vote.";
	}
}
