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

import java.awt.Color;
import java.time.OffsetDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		final TextChannel CHANNEL = event.getJDA().getTextChannelById("852338750519640116");
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
