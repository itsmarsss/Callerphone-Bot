package com.itsmarsss.callerphone.bot;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.lang.management.ManagementFactory;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.concurrent.TimeUnit;

public class About implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        e.deferReply().queue();
        JDA jda = e.getJDA();

        jda.retrieveUserById(Callerphone.config.getOwnerID()).queue(
                owner -> e.getHook().editOriginalEmbeds(buildAbout(jda, owner.getName(), owner.getEffectiveAvatarUrl())).queue(),
                err -> e.getHook().editOriginalEmbeds(buildAbout(jda, "Unknown", null)).queue()
        );
    }

    private EmbedBuilder linksDescription() {
        return new EmbedBuilder().setDescription(
                "[Invite link](" + Callerphone.config.getBotInviteLink() + ")"
                        + "\n[Support server](" + Callerphone.config.getSupportServer() + ")"
                        + "\n[Bot listing (top.gg)](" + Callerphone.config.getBotListingTopGG() + ")"
                        + "\n[Upvote bot (top.gg)](" + Callerphone.config.getUpvoteBotTopGG() + ")"
                        + "\n[Bot listing (dbl)](" + Callerphone.config.getBotListingDBL() + ")"
                        + "\n[Upvote bot (dbl)](" + Callerphone.config.getUpvoteBotDBL() + ")"
                        + "\n[Upvote support server (top.gg)](" + Callerphone.config.getUpvoteSupportServerTopGG() + ")"
                        + "\n[Upvote support server (dbl)](" + Callerphone.config.getUpvoteSupportServerDBL() + ")"
                        + "\n\n[Privacy Policy](" + Callerphone.config.getPrivacyPolicy() + ")"
                        + "\n[Terms of Service](" + Callerphone.config.getTermsOfService() + ")"
        );
    }

    private net.dv8tion.jda.api.entities.MessageEmbed buildAbout(JDA jda, String ownerName, String ownerAvatar) {
        long totalServers = 0;
        long cachedUsers = 0;
        long totalUsers = 0;

        ShardManager sdMgr = Callerphone.sdMgr;
        if (sdMgr != null) {
            for (JDA shard : sdMgr.getShards()) {
                totalServers += shard.getGuilds().size();
                cachedUsers += shard.getUsers().size();
                for (Guild g : shard.getGuilds()) {
                    totalUsers += g.getMemberCount();
                }
            }
        }

        double load = ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();
        String cpu = load < 0 ? "Unavailable" : String.format("%.2f", load);

        return linksDescription()
                .setAuthor("Made by " + ownerName, null, ownerAvatar)
                .setColor(ToolSet.COLOR)
                .setTitle("**About:**")
                .addField("Servers",
                        totalServers + " server(s)\n" + jda.getShardInfo().getShardTotal() + " shard(s)", true)
                .addField("Channels",
                        (jda.getTextChannels().size() + jda.getVoiceChannels().size()) + " total\n"
                                + jda.getTextChannels().size() + " text\n"
                                + jda.getVoiceChannels().size() + " voice", true)
                .addField("Users",
                        totalUsers + " total\n" + cachedUsers + " cached", true)
                .addField("CPU Usage",
                        cpu + "\n" + ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors() + " processor(s)", true)
                .addField("Memory Usage",
                        convert(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + "\n"
                                + convert(Runtime.getRuntime().maxMemory()) + " max", true)
                .addField("Uptime", formatUptime(ManagementFactory.getRuntimeMXBean().getUptime()), true)
                .addField("Info",
                        (Callerphone.isQuickStart ? "QuickStarted Bot\n" : "")
                                + "Made in Java <:Java:899050421572739072> with JDA <:JDA:899083802989695037>\n"
                                + "Version " + Callerphone.VERSION, false)
                .build();
    }

    private static String convert(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }

    public static String formatUptime(long durationMs) {
        long days = TimeUnit.MILLISECONDS.toDays(durationMs);
        long hours = TimeUnit.MILLISECONDS.toHours(durationMs) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        sb.append(seconds).append("s");
        return sb.toString().trim();
    }

    /** @deprecated use {@link #formatUptime(long)} */
    @Deprecated
    public static String upTimeAbt() {
        return formatUptime(ManagementFactory.getRuntimeMXBean().getUptime());
    }

    @Override
    public String getHelp() {
        return "</about:1075168876930797618> - Introduces you to this bot";
    }

    @Override
    public String getName() {
        return "about";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Introduces you to this bot")
                .setContexts(InteractionContextType.GUILD);
    }
}
