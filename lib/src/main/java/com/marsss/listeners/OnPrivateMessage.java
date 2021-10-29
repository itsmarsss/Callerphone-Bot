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
