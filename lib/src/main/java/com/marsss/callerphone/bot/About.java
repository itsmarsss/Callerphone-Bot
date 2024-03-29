package com.marsss.callerphone.bot;

import java.awt.Color;
import java.lang.management.ManagementFactory;

import com.marsss.commandType.ISlashCommand;
import com.marsss.callerphone.Callerphone;

import com.marsss.callerphone.ToolSet;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class About implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        e.replyEmbeds(about()).queue();
    }

    @Override
    public String getHelp() {
        return "`" + Callerphone.config.getPrefix() + "about` - Introduces you to this bot";
    }

    @Override
    public String[] getTriggers() {
        return "about,abt".split(",");
    }

    private final StringBuilder DESCRIPTION = new StringBuilder()
            .append("[Invite link](").append(Callerphone.config.getBotInviteLink()).append(")")
            .append("\n[Support server](").append(Callerphone.config.getSupportServer()).append(")")
            .append("\n[Bot listing (top.gg)](https://top.gg/bot/849713468348956692)")
            .append("\n[Upvote bot (top.gg)](https://top.gg/bot/849713468348956692/vote)")
            .append("\n[Bot listing (dbl)](https://discordbotlist.com/bots/callerphone)")
            .append("\n[Upvote bot (dbl)](https://discordbotlist.com/bots/callerphone/upvote)")
            .append("\n[Upvote support server (top.gg)](https://top.gg/servers/798428155907801089/vote)")
            .append("\n[Upvote support server (dbl)](https://discordbotlist.com/servers/legendary-bot-official-server/upvote)")
            .append("\n")
            .append("\n[Privacy Policy](").append(Callerphone.config.getPrivacyPolicy()).append(")")
            .append("\n[Terms of Service](").append(Callerphone.config.getTermsOfService()).append(")");
    private MessageEmbed about() {
        final JDA jda = Callerphone.jda;

        EmbedBuilder AbtEmd = new EmbedBuilder();
        jda.retrieveUserById(Callerphone.config.getOwnerID()).queue(u -> {

            jda.getShardInfo();
            long users = 0;
            for (Guild g : jda.getGuilds()) {
                users += g.getMemberCount();
            }

            final String UNIQUE_USERS = Callerphone.isQuickStart ? "N/A (QuickStart)" : jda.getUsers().size() + " unique user(s)";

            AbtEmd.setAuthor("Made by " + u.getAsTag(), null, u.getAvatarUrl())
                    .setColor(new Color(114, 137, 218))
                    .setTitle("**About:**")
                    .setDescription(DESCRIPTION)
                    .addField("Servers",
                            jda.getGuilds().size() + " server(s)\n" +
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
                            ToolSet.convert(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + "\n" +
                                    ToolSet.convert(Runtime.getRuntime().maxMemory()) + " max\n", true)

                    .addField("Uptime",
                            Uptime.upTimeAbt(), true)

                    .setFooter("One of the many bots in the sequel...");

            AbtEmd.addField("Info",
                    (Callerphone.isQuickStart ? "QuickStarted Bot\n" : "") +
                            "Made in Java <:Java:899050421572739072> with Java Discord Api <:JDA:899083802989695037>", false);
        });
        return AbtEmd.build();
    }

}
