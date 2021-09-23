package com.marsss.Listeners;

import com.marsss.Entertainments.*;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class OnSlashCommand extends ListenerAdapter {
	public void onSlashCommand(SlashCommandEvent event) {
		if(event.getName().equals("clap")) {
			String msg = event.getOption("message").getAsString();
			String []args = msg.split("\\s+");
			event.reply("Clapped!").setEphemeral(true).queue();
			event.getChannel().sendMessage(Clap.clap(args) + "\n\n\t||Clap by " + event.getUser().getAsMention() + "||").queue();
			return;
		}

		if(event.getName().equals("color")) {
			event.replyEmbeds(Colour.color()).setEphemeral(true).queue();
			return;
		}

		if(event.getName().equals("colorhex")) {
			String hex = event.getOption("hex").getAsString();
			event.replyEmbeds(Colour.colorhex(hex)).setEphemeral(true).queue();
			return;
		}

		if(event.getName().equals("colorrgb")) {
			String r = event.getOption("red").getAsString(), 
					g = event.getOption("green").getAsString(), 
					b = event.getOption("blue").getAsString();
			event.replyEmbeds(Colour.colorrgb(r, g, b)).setEphemeral(true).queue();
			return;
		}

		if(event.getName().equals("echo")) {
			String msg = event.getOption("message").getAsString();
			String []args = msg.split("\\s+");
			event.reply("Echoed!").setEphemeral(true).queue();
			event.getChannel().sendMessage(Echo.echo(args) + "\n\n\t||Echo by " + event.getUser().getAsMention() + "||").queue();
			return;
		}

		if(event.getName().equals("eightball")) {
			String qst = event.getOption("question").getAsString();
			event.reply(EightBall.eightball(qst)).setEphemeral(true).queue();
			return;
		}

		if(event.getName().equals("pollnew")) {
			String qst = event.getOption("question").getAsString();
			event.reply("Poll Launched!").setEphemeral(true).queue();
			event.getChannel().sendMessageEmbeds(Polls.newpoll(qst)).queue(message -> {
				message.addReaction("✅").queue();
				message.addReaction("❌").queue();
				message.addReaction("➖").queue();
			});
			return;
		}

		if(event.getName().equals("rps")) {
			String move = event.getOption("move").getAsString();
			event.reply(RPS.rps(move)).setEphemeral(true).queue();
			return;
		}
	}
}
