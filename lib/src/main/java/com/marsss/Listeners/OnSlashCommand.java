package com.marsss.Listeners;

import com.marsss.Bot;
import com.marsss.Entertainments.*;
import com.marsss.Utils.Commands;
import com.marsss.Utils.Help;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class OnSlashCommand extends ListenerAdapter {
	public void onSlashCommand(SlashCommandEvent event) {
		if(event.getName().equals("clap")) {
			String msg = event.getOption("message").getAsString();
			String []args = msg.split("\\s+");
			event.reply(Clap.clap(args)).queue();
			return;
		}

		if(event.getName().equals("color")) {
			event.replyEmbeds(Colour.color()).queue();
			return;
		}

		if(event.getName().equals("colorhex")) {
			String hex = event.getOption("hex").getAsString();
			event.replyEmbeds(Colour.colorhex(hex)).queue();
			return;
		}

		if(event.getName().equals("colorrgb")) {
			String r = event.getOption("red").getAsString(), 
					g = event.getOption("green").getAsString(), 
					b = event.getOption("blue").getAsString();
			event.replyEmbeds(Colour.colorrgb(r, g, b)).queue();
			return;
		}

		if(event.getName().equals("echo")) {
			String msg = event.getOption("message").getAsString();
			String []args = msg.split("\\s+");
			event.reply(Echo.echo(args)).queue();
			return;
		}

		if(event.getName().equals("eightball")) {
			String qst = event.getOption("question").getAsString();
			event.reply(EightBall.eightball(qst)).queue();
			return;
		}

		if(event.getName().equals("pollnew")) {
			String qst = event.getOption("question").getAsString();
			event.reply(event.getUser().getName() + " launched a poll:").complete();
			event.getChannel().sendMessageEmbeds(Polls.newpoll(qst)).queue(message -> {
				message.addReaction("✅").queue();
				message.addReaction("❌").queue();
			});
			return;
		}

		if(event.getName().equals("rps")) {
			String move = event.getOption("move").getAsString();
			event.reply(RPS.rps(move)).queue();
			return;
		}

		if(event.getName().equals("ping")) {
			Bot.jda.getRestPing().queue(
					(ping) -> event.replyFormat("**Reset ping:** %sms \n**WS ping:** %sms", ping, Bot.jda.getGatewayPing()).queue());
			return;
		}

		if(event.getName().equals("commands")) {
			event.replyEmbeds(Commands.commands()).queue();
			return;
		}

		if(event.getName().equals("help")) {
			String cmd = event.getOption("command").getAsString();
			event.replyEmbeds(Help.help(cmd)).queue();
			return;
		}
	}
}
