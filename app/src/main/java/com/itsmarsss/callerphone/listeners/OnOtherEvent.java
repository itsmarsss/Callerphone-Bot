package com.itsmarsss.callerphone.listeners;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.ToolSet;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.session.SessionDisconnectEvent;
import net.dv8tion.jda.api.events.session.SessionResumeEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicInteger;

public class OnOtherEvent extends ListenerAdapter {
    public static final Logger logger = LoggerFactory.getLogger(OnOtherEvent.class);

    private static volatile OffsetDateTime timeDisconnected = OffsetDateTime.now();
    private static final AtomicInteger disconnectCount = new AtomicInteger(0);

    @Override
    public void onSessionDisconnect(SessionDisconnectEvent event) {
        timeDisconnected = event.getTimeDisconnected();
    }

    @Override
    public void onSessionResume(SessionResumeEvent event) {
        OffsetDateTime disconnectedAt = timeDisconnected;
        Duration downtime = Duration.between(disconnectedAt, OffsetDateTime.now());
        if (downtime.isNegative()) {
            downtime = Duration.ZERO;
        }

        int count = disconnectCount.incrementAndGet();
        long hours = downtime.toHours();
        long minutes = downtime.toMinutes() % 60;
        long seconds = downtime.getSeconds() % 60;
        long millis = downtime.toMillis() % 1000;

        logger.warn("Bot disconnected for {}h {}m {}s {}ms ({} times since startup)",
                hours, minutes, seconds, millis, count);

        TextChannel logChannel = ToolSet.getTextChannel(Callerphone.config.getLogStatusChannel());
        if (logChannel == null) {
            logger.warn("Invalid LOG channel for disconnect notice");
            return;
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Color.RED)
                .setTitle("Disconnected")
                .setDescription("The bot disconnected for "
                        + hours + " hour(s) "
                        + minutes + " minute(s) "
                        + seconds + " second(s) and "
                        + millis + " milliseconds due to connectivity issues.\n"
                        + "Response number: " + event.getResponseNumber())
                .setTimestamp(OffsetDateTime.now())
                .setFooter("The bot disconnected " + count + " times already since the last startup.");

        logChannel.sendMessageEmbeds(embed.build()).queue();
    }
}
