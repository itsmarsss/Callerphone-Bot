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
package com.marsss.listeners;

import java.net.URL;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.marsss.Bot;

import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class OnPrivateMessage extends ListenerAdapter {
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {

		final 	Logger logger = LoggerFactory.getLogger(OnPrivateMessage.class);
		String msg = event.getMessage().getContentRaw();
		msg = msg.replaceAll("\\s+", "%20");
		
		try {
			final URL url = new URL(Bot.brainURL.replace("[uid]", event.getChannel().getId()).replace("[msg]", msg));
			final Scanner sc = new Scanner(url.openStream());

			String s = sc.nextLine();

			s = s.replace("<a href=\\\"", "");
			s = s.replace("\">", " ");
			s = s.replace("<\\/a>", "");
			
			event.getChannel().sendMessage(replaceLast(s.replace("{\"cnt\":\"", ""), "\"}", "")).queue();
		} catch (Exception e) {
			logger.error(e.toString());
		}

	}

	private static String replaceLast(final String text, final String regex, final String replacement) {
		return text.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
	}

}
