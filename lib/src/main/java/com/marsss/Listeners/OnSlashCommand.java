package com.marsss.Listeners;

import com.marsss.Entertainments.Clap;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class OnSlashCommand extends ListenerAdapter {
	public void onSlashCommand(SlashCommandEvent event) {
		if(event.getName().equals("clap")) {
			String msg = event.getOption("message").getAsString();
			event.reply(Clap.clap(msg.split("\\s+"))).setEphemeral(true).queue();
		}
		if(event.getName().equals("colorrgb")) {
			String msg = event.getop
		}
	}
}
