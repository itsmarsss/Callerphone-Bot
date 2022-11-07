package com.marsss.callerphone.bot;

import java.awt.Color;
import java.lang.management.ManagementFactory;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import com.marsss.Command;
import com.marsss.callerphone.Callerphone;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class About implements Command {

    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        final JDA jda = Callerphone.jda;

        final StringBuilder desc = new StringBuilder()
                .append("[Invite link](" + Callerphone.invite + ")")
                .append("\n[Support server](" + Callerphone.support + ")")
                .append("\n[Bot listing (top.gg)](https://top.gg/bot/849713468348956692)")
                .append("\n[Upvote bot (top.gg)](https://top.gg/bot/849713468348956692/vote)")
                .append("\n[Bot listing (dbl)](https://discordbotlist.com/bots/callerphone)")
                .append("\n[Upvote bot (dbl)](https://discordbotlist.com/bots/callerphone/upvote)")
                .append("\n[Upvote support server (top.gg)](https://top.gg/servers/798428155907801089/vote)")
                .append("\n[Upvote support server (dbl)](https://discordbotlist.com/servers/legendary-bot-official-server/upvote)")

                .append("\n");

        jda.getShardInfo();
        long users = 0;
        for (Guild g : jda.getGuilds()) {
            users += g.getMemberCount();
        }

        String UNIQUEUSERS;
        if (Callerphone.isQuickStart) {
            UNIQUEUSERS = "N/A (QuickStart)";
        } else {
            UNIQUEUSERS = jda.getUsers().size() + " unique user(s)";
        }

        EmbedBuilder AbtEmd = new EmbedBuilder()
                .setAuthor("Made by " + jda.getUserById(Callerphone.owner).getAsTag(), null, jda.getUserById(Callerphone.owner).getAvatarUrl())
                .setColor(Color.cyan)
                .setTitle("**About:**")
                .setDescription(desc)
                .addField("Servers",
                        jda.getGuilds().size() + " server(s)\n" +
                                jda.getShardInfo().getShardTotal() + " shard(s)\n", true)

                .addField("Channels",
                        jda.getTextChannels().size() + jda.getVoiceChannels().size() + " total\n" +
                                jda.getTextChannels().size() + " text channel(s)\n" +
                                jda.getVoiceChannels().size() + " voice channel(s)", true)

                .addField("Users",
                        users + " user(s)\n" +
                                UNIQUEUSERS, true)

                .addField("CPU Usage",
                        (String.valueOf(ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage()).startsWith("-")) ? ("Unavailable") : (ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage() + "%") + "\n" +
                                ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors() + " processor(s)", true)

                .addField("Memory Usage",
                        convert(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + "\n" +
                                convert(Runtime.getRuntime().maxMemory()) + " max\n", true)

                .addField("Uptime",
                        Uptime.uptimeabt(), true)

                .setFooter("One of the many bots in the sequel...");

        if (Callerphone.isQuickStart) {
            AbtEmd.addField("Info",
                    "QuickStarted Bot\n" +
                            "Made in Java <:Java:899050421572739072> with Java Discord Api <:JDA:899083802989695037>", false);
        } else {
            AbtEmd.addField("Info",
                    "Made in Java <:Java:899050421572739072> with Java Discord Api <:JDA:899083802989695037>", false);
        }

        e.getMessage().replyEmbeds(AbtEmd.build()).queue();
    }

    @Override
    public void runSlash(SlashCommandEvent event) {

    }

    public static String getHelp() {
        return "`" + Callerphone.Prefix + "about` - Introduces you to this bot";
    }

    @Override
    public String getHelpF() {
        return "`" + Callerphone.Prefix + "about` - Introduces you to this bot";
    }

    @Override
    public String[] getTriggers() {
        return "about,abt".split(",");
    }

    // https://programming.guide/java/formatting-byte-size-to-human-readable-format.html {

    public static String convert(long bytes) {
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

}
