package com.marsss.callerphone.listeners;

import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.ToolSet;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.session.SessionDisconnectEvent;
import net.dv8tion.jda.api.events.session.SessionResumeEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.OffsetDateTime;

public class OnOtherEvent extends ListenerAdapter {
    public static final Logger logger = LoggerFactory.getLogger(OnOtherEvent.class);
    private static OffsetDateTime timeDisconnected = OffsetDateTime.now();
    private static int disconnectCount = 0;

    public void onSessionDisconnect(SessionDisconnectEvent event) {
        timeDisconnected = event.getTimeDisconnected();
    }

    public void onSessionResume(SessionResumeEvent event) {
        final TextChannel LOG_CHANNEL = ToolSet.getTextChannel(Callerphone.config.getLogStatusChannel());
        EmbedBuilder Emd = new EmbedBuilder()
                .setColor(Color.RED)
                .setTitle("Disconnected");

        disconnectCount++;
        logger.warn("Bot disconnected for: {} hour(s) {} minute(s) {} second(s) and {} | {} time(s)!", OffsetDateTime.now().getHour() - timeDisconnected.getHour(), OffsetDateTime.now().getMinute() - timeDisconnected.getMinute(), OffsetDateTime.now().getSecond() - timeDisconnected.getSecond(), timeDisconnected.getNano() / 1000000, disconnectCount);

        Emd.setDescription("The bot disconnected for " +
                        (OffsetDateTime.now().getHour() - timeDisconnected.getHour()) + " hour(s) " +
                        (OffsetDateTime.now().getMinute() - timeDisconnected.getMinute()) + " minute(s) " +
                        (OffsetDateTime.now().getSecond() - timeDisconnected.getSecond()) + " second(s) and " +
                        (timeDisconnected.getNano() / 1000000) + " milliseconds due to connectivity issues.\n" +
                        "Response number: " + event.getResponseNumber())
                .setTimestamp(OffsetDateTime.now())
                .setFooter("The bot disconnected " + disconnectCount + " times already since the last startup.");

        if (LOG_CHANNEL == null) {
            logger.warn("Invalid LOG channel.");
        } else {
            LOG_CHANNEL.sendMessageEmbeds(Emd.build()).queue();
        }
    }
}
