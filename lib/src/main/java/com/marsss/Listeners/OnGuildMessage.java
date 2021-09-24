package com.marsss.Listeners;

import com.marsss.Bot;
import com.marsss.Entertainments.*;
import com.marsss.Utils.*;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class OnGuildMessage extends ListenerAdapter {
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		User author = event.getAuthor();
		String msg = event.getMessage().getContentRaw();

		String prefix = ";";
		
		if(author.isBot() || !msg.startsWith(prefix))
			return;

		msg = msg.toLowerCase();
		
		
		if(msg.startsWith(";clap ")) {
			msg = msg.replaceFirst(";clap ", "");
			String []args = msg.split("\\s+");
			event.getMessage().reply(Clap.clap(args)).queue();
			return;
		}

		if(msg.startsWith(";color ")) {
			event.getMessage().replyEmbeds(Colour.color()).queue();
			return;
		}

		if(msg.startsWith(";colorhex ")) {
			msg = msg.replaceFirst(";colorhex ", "");
			event.getMessage().replyEmbeds(Colour.colorhex(msg)).queue();
			return;
		}

		if(msg.startsWith(";colorrgb ")) {
			msg = msg.replaceFirst(";colorrgb ", "");
			String []args = msg.split("\\s+");
			String r = args[0], 
					g = args[1], 
					b = args[2];
			event.getMessage().replyEmbeds(Colour.colorrgb(r, g, b)).queue();
			return;
		}

		if(msg.startsWith(";echo ")) {
			msg = msg.replaceFirst(";echo ", "");
			String []args = msg.split("\\s+");
			event.getMessage().reply(Echo.echo(args)).queue();
			return;
		}

		if(msg.startsWith(";eightball ")) {
			msg = msg.replaceFirst(";eightball ", "");
			event.getMessage().reply(EightBall.eightball(msg)).queue();
			return;
		}

		if(msg.startsWith(";poll ")) {
			msg = msg.replaceFirst(";poll ", "");
			event.getChannel().sendMessage(event.getAuthor().getName() + " launched a poll:").complete();
			event.getChannel().sendMessageEmbeds(Polls.newpoll(msg)).queue(message -> {
				message.addReaction("✅").queue();
				message.addReaction("❌").queue();
				message.addReaction("➖").queue();
			});
			return;
		}

		if(msg.startsWith(";rps ")) {
			msg = msg.replaceFirst(";rps ", "");
			event.getMessage().reply(RPS.rps(msg)).queue();
			return;
		}

		if(msg.startsWith(";ping")) {
			Bot.jda.getRestPing().queue(
					(ping) -> event.getMessage().replyFormat("**Reset ping:** %sms \n**WS ping:** %sms", ping, Bot.jda.getGatewayPing()).queue());
		}
		
		if(msg.startsWith(";commands")) {
			event.getMessage().replyEmbeds(Commands.commands()).queue();
			return;
		}
		
		if(msg.startsWith(";botinfo")) {
			event.getMessage().replyEmbeds(BotInfo.botinfo()).queue();
			return;
		}
		
		if(msg.startsWith(";uptime")) {
			event.getMessage().reply(Uptime.uptime()).queue();
			return;
		}
		
		if(msg.startsWith(";userinfo")) {
			if(event.getMessage().getMentionedMembers().size() == 0) {
				event.getMessage().replyEmbeds(UserInfo.userinfo(event.getMember())).queue();
				return;
			}
			
			event.getMessage().replyEmbeds(UserInfo.userinfo(event.getMessage().getMentionedMembers().get(0))).queue();
			return;
		}

		if(msg.startsWith(";help ")) {
			msg = msg.replaceFirst(";help ", "");
			event.getMessage().replyEmbeds(Help.help(msg)).queue();
			return;
		}
		
	}
}
