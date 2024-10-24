package com.marsss.callerphone.bot;

import java.lang.management.ManagementFactory;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.concurrent.CompletableFuture;

import com.marsss.callerphone.ToolSet;
import com.marsss.callerphone.Callerphone;

import com.marsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class About implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        e.replyEmbeds(about(e.getJDA())).queue();
    }

    private StringBuilder description = new StringBuilder()
            .append("[Invite link](").append(Callerphone.config.getBotInviteLink()).append(")")
            .append("\n[Support server](").append(Callerphone.config.getSupportServer()).append(")")
            .append("\n[Bot listing (top.gg)](").append(Callerphone.config.getBotListingTopGG()).append(")")
            .append("\n[Upvote bot (top.gg)](").append(Callerphone.config.getUpvoteBotTopGG()).append(")")
            .append("\n[Bot listing (dbl)](").append(Callerphone.config.getBotListingDBL()).append(")")
            .append("\n[Upvote bot (dbl)](").append(Callerphone.config.getUpvoteBotDBL()).append(")")
            .append("\n[Upvote support server (top.gg)](").append(Callerphone.config.getUpvoteSupportServerTopGG()).append(")")
            .append("\n[Upvote support server (dbl)](").append(Callerphone.config.getUpvoteSupportServerDBL()).append(")")
            .append("\n")
            .append("\n[Privacy Policy](").append(Callerphone.config.getPrivacyPolicy()).append(")")
            .append("\n[Terms of Service](").append(Callerphone.config.getTermsOfService()).append(")");

    private MessageEmbed about(JDA jda) {
        EmbedBuilder aboutEmbed = new EmbedBuilder();

        CompletableFuture<Void> future = new CompletableFuture<>();

        jda.retrieveUserById(Callerphone.config.getOwnerID()).queue(u -> {
                    long totalServers = 0;
                    long totalUsers = 0;
                    long users = 0;

                    for (JDA shard : Callerphone.sdMgr.getShards()) {
                        totalServers += shard.getGuilds().size();
                        totalUsers += shard.getUsers().size();

                        for (Guild g : jda.getGuilds()) {
                            users += g.getMemberCount();
                        }
                    }

                    String UNIQUE_USERS = Callerphone.isQuickStart ? "N/A (QuickStart)" : totalUsers + " unique user(s)";

                    aboutEmbed.setAuthor("Made by " + u.getName(), null, u.getAvatarUrl())
                            .setColor(ToolSet.COLOR)
                            .setTitle("**About:**")
                            .setDescription(description)
                            .addField("Servers",
                                    totalServers + " server(s)\n" +
                                            jda.getShardInfo().getShardTotal() + " shard(s)\n", true)

                            .addField("Channels",
                                    jda.getTextChannels().size() + jda.getVoiceChannels().size() + " total\n" +
                                            jda.getTextChannels().size() + " text channel(s)\n" +
                                            jda.getVoiceChannels().size() + " voice channel(s)", true)

                            .addField("Users",
                                    users + " user(s)\n" +
                                            UNIQUE_USERS, true)

                            .addField("CPU Usage",
                                    (String.valueOf(ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage()).startsWith("-")) ? ("Unavailable") : (ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage() + "%") + "\n" +
                                            ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors() + " processor(s)", true)

                            .addField("Memory Usage",
                                    convert(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + "\n" +
                                            convert(Runtime.getRuntime().maxMemory()) + " max\n", true)

                            .addField("Uptime",
                                    upTimeAbt(), true);

                    aboutEmbed.addField("Info",
                            (Callerphone.isQuickStart ? "QuickStarted Bot\n" : "") +
                                    "Made in Java <:Java:899050421572739072> with Java Discord Api <:JDA:899083802989695037>", false);

                    future.complete(null);
                },
                future::completeExceptionally
        );

        try {
            future.get();
        } catch (Exception e) {
        }

        return aboutEmbed.build();
    }


    @Override
    public String getHelp() {
        return "</about:1075168876930797618> - Introduces you to this bot";
    }

    @Override
    public String[] getTriggers() {
        return "about,abt".split(",");
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getTriggers()[0], getHelp().split(" - ")[1])
                .setGuildOnly(true);
    }

    // https://programming.guide/java/formatting-byte-size-to-human-readable-format.html {

    private String convert(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        final CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }

    // }


    // https://github.com/DV8FromTheWorld/Yui/blob/master/src/main/java/net/dv8tion/discord/commands/UptimeCommand.java {

    public static String upTimeAbt() {

        final long DURATION = ManagementFactory.getRuntimeMXBean().getUptime();

        final long YEARS = DURATION / 31104000000L;
        final long MONTHS = DURATION / 2592000000L % 12;
        final long DAYS = DURATION / 86400000L % 30;
        final long HOURS = DURATION / 3600000L % 24;
        final long MINUTES = DURATION / 60000L % 60;
        final long SECONDS = DURATION / 1000L % 60;

        String UPTIME = (YEARS == 0 ? "" : YEARS + "y ") +
                (MONTHS == 0 ? "" : MONTHS + "M ") +
                (DAYS == 0 ? "" : DAYS + "d ") +
                (HOURS == 0 ? "" : HOURS + "h ") +
                (MINUTES == 0 ? "" : MINUTES + "m ") +
                (SECONDS == 0 ? "" : SECONDS + "s ");

        UPTIME = replaceLast(UPTIME, ", ", "");
        UPTIME = replaceLast(UPTIME, ",", " and");

        return UPTIME;
    }

    private static String replaceLast(final String text, final String regex, final String replacement) {
        return text.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
    }

    // }
}
