package com.marsss.listeners;

import java.awt.Color;
import java.time.OffsetDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.marsss.Bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.DisconnectEvent;
import net.dv8tion.jda.api.events.ResumedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class OnOtherEvent extends ListenerAdapter{
	public static Logger logger = LoggerFactory.getLogger(OnOtherEvent.class);
	private static OffsetDateTime timeDisconnected = OffsetDateTime.now();
	private static int disconnectCount = 0;
	public void onDisconnect(DisconnectEvent event) {
		timeDisconnected = event.getTimeDisconnected();
	}

	public void onResumed(ResumedEvent event)  {
		final TextChannel CHANNEL = event.getJDA().getTextChannelById(Bot.logstatus);
		EmbedBuilder Emd = new EmbedBuilder().setColor(Color.RED).setTitle("Disconnected");
		
		disconnectCount++;
		logger.warn("Bot disconnected for: " + 
				(OffsetDateTime.now().getHour() - timeDisconnected.getHour())  + " hour(s) " +
				(OffsetDateTime.now().getMinute() - timeDisconnected.getMinute()) + " minute(s) " +
				(OffsetDateTime.now().getSecond() - timeDisconnected.getSecond()) + " second(s) and " +
				(timeDisconnected.getNano() /1000000) + " | " + disconnectCount + " time(s)!");
		
		Emd.setDescription("The bot disconnected for " +
				(OffsetDateTime.now().getHour() - timeDisconnected.getHour())  + " hour(s) " +
				(OffsetDateTime.now().getMinute() - timeDisconnected.getMinute()) + " minute(s) " +
				(OffsetDateTime.now().getSecond() - timeDisconnected.getSecond()) + " second(s) and " +
				(timeDisconnected.getNano() /1000000) + " milliseconds due to connectivity issues.\n" +
				"Response number: " + event.getResponseNumber()).setTimestamp(OffsetDateTime.now()).setFooter("The bot disconnected " + disconnectCount + " times already since the last startup.");
		CHANNEL.sendMessageEmbeds(Emd.build()).queue();
	}

}
