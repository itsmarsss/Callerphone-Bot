package com.marsss.listeners;

import java.util.List;

import com.marsss.Bot;
import com.marsss.bot.*;
import com.marsss.entertainments.*;
import com.marsss.utils.*;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandListener extends ListenerAdapter {
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		Member MEMBER = event.getMember();
		Message MESSAGE = event.getMessage();

		String prefix = ";";

		String content = MESSAGE.getContentRaw();
		
		String args[] = content.split("\\s+");
		
		if(MEMBER.getUser().isBot() || !args[0].equals(prefix))
			return;

		// Utils
		switch(args[0]) {



		case ";help":
			MESSAGE.replyEmbeds(Help.help(args[1])).queue();
			break;



		case ";botinfo":
			MESSAGE.replyEmbeds(BotInfo.botinfo()).queue();
			break;



		case ";channelinfo":
			List<TextChannel> CHANNELS = MESSAGE.getMentionedChannels();
			if(CHANNELS.size() > 0) {
				if(CHANNELS.get(0).getType() == ChannelType.TEXT) {
					MESSAGE.replyEmbeds(ChannelInfo.textchannelinfo(CHANNELS.get(0))).queue();
				}
			}else if(MEMBER.getVoiceState().inVoiceChannel()) {
				MESSAGE.replyEmbeds(ChannelInfo.voicechannelinfo(MEMBER.getVoiceState().getChannel())).queue();
				break;
			}else if(MESSAGE.getMentionedChannels().size() == 0) {
				MESSAGE.replyEmbeds(ChannelInfo.textchannelinfo(event.getChannel())).queue();
			}
			break;



		case ";roleinfo":
			MESSAGE.replyEmbeds(RoleInfo.roleinfo(MESSAGE.getMentionedRoles().get(0))).queue();
			break;



		case ";serverinfo":
			MESSAGE.replyEmbeds(ServerInfo.serverinfo(event.getGuild())).queue();
			break;



		case ";userinfo":
			if(MESSAGE.getMentionedMembers().size() == 0) {
				MESSAGE.replyEmbeds(UserInfo.userinfo(MEMBER)).queue();
			}else {
				MESSAGE.replyEmbeds(UserInfo.userinfo(MESSAGE.getMentionedMembers().get(0))).queue();
				break;
			}



		case ";poll":
			content = content.replace(";poll ", "");
			event.getChannel().sendMessage(event.getAuthor().getName() + " launched a poll:").complete();
			event.getChannel().sendMessageEmbeds(Polls.newpoll(content)).queue(message -> {
				message.addReaction("✅").queue();
				message.addReaction("�?�").queue();
			});
			return;
		}

		
		// Bot
		switch (args[0]) {
		
		
		
		case ";donate":
			MESSAGE.reply(Donate.donate()).queue();
			break;
			
			
			
		case ";invite":
			MESSAGE.replyEmbeds(Invite.invite()).queue();
			break;
			
			
			
		case ";ping":
			Bot.jda.getRestPing().queue(
					(ping) -> MESSAGE.replyFormat("**Reset ping:** %sms \n**WS ping:** %sms", ping, Bot.jda.getGatewayPing()).queue());
			break;
			
			
			
		case ";uptime":
			MESSAGE.reply(Uptime.uptime()).queue();
			break;
			
			
			
		}


		// Entertainments
		switch (args[0]) {
		
		
		
		case ";clap":
			args[0] = "";
			MESSAGE.reply(Clap.clap(args)).queue();
			break;
		
		
		
		case ";color":
			MESSAGE.replyEmbeds(Colour.color()).queue();
			break;
			
			
			
		case ";colorhex":
			MESSAGE.replyEmbeds(Colour.colorhex(args[1])).queue();
			break;
			
			
			
		case ";colorrgb":
			MESSAGE.replyEmbeds(Colour.colorrgb(args[1], args[2], args[3])).queue();
			break;
			
			
			
			
		case ";echo":
			args[0] = "";
			MESSAGE.reply(Echo.echo(args)).queue();
			break;
			
			
			
		case ";eightball":
			content = content.replace(";eightball ", "");
			MESSAGE.reply(EightBall.eightball(content)).queue();
			break;
			
			
			
		}
	}
}
