package com.marsss.Listeners;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class OnGuildMessage extends ListenerAdapter {
	public void onGuildMessageRecieved(GuildMessageReceivedEvent event) {
		User author = event.getAuthor();
		String msg = event.getMessage().getContentRaw();

		if(author.isBot() || !msg.startsWith(msg))
			return;

		String prefix = "get prefix from database";

		if(!msg.startsWith(prefix))
			return;

		msg = msg.replaceFirst(prefix, "");
		
	}
}
