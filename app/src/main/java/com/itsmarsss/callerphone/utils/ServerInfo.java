package com.itsmarsss.callerphone.utils;

import com.itsmarsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ServerInfo implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        if (e.getGuild() == null) {
            e.replyEmbeds(EmbedHelpers.error("This command can only be used in a server.")).setEphemeral(true).queue();
            return;
        }
        e.replyEmbeds(serverInfo(e.getGuild())).queue();
    }

    private MessageEmbed serverInfo(Guild guild) {
        Member owner = guild.getOwner();
        TextChannel system = guild.getSystemChannel();
        TextChannel rules = guild.getRulesChannel();
        TextChannel community = guild.getCommunityUpdatesChannel();
        VoiceChannel afk = guild.getAfkChannel();

        return EmbedHelpers.base()
                .setDescription(":minidisc: **Server information for " + guild.getName() + ":**")
                .addField("General Information",
                        "Name: " + guild.getName() +
                                "\nDescription: " + EmbedHelpers.orDefault(guild.getDescription(), "N/A") +
                                "\nIcon URL: " + linkOrNa(guild.getIconUrl(), "Icon") +
                                "\nBanner URL: " + linkOrNa(guild.getBannerUrl(), "Banner"),
                        false)
                .addField("Categories",
                        "Category Count: " + guild.getCategories().size() +
                                "\nChannel Count: " + guild.getChannels().size() +
                                "\nTextChannel Count: " + guild.getTextChannels().size() +
                                "\nVoiceChannel Count: " + guild.getVoiceChannels().size() +
                                "\nStageChannel Count: " + guild.getStageChannels().size() +
                                "\nSystem Channel: " + mentionOrNa(system) +
                                "\nRules Channel: " + mentionOrNa(rules) +
                                "\nCommunity Update Channel: " + mentionOrNa(community),
                        false)
                .addField("Members",
                        "Member Count: " + guild.getMemberCount() +
                                "\nOwner: " + (owner != null ? owner.getAsMention() : "N/A"),
                        true)
                .addField("Boosts",
                        "Boost Count: " + guild.getBoostCount() +
                                "\nBoost Tier: " + guild.getBoostTier(),
                        true)
                .addField("Roles", "Role Count: " + guild.getRoles().size(), true)
                .addField("Created", guild.getTimeCreated().format(EmbedHelpers.DATE_FMT), true)
                .addField("AFK",
                        "AFK Channel: " + mentionOrNa(afk) +
                                "\nAFK Timeout: " + guild.getAfkTimeout().getSeconds() + "s",
                        true)
                .setFooter("ID: " + guild.getId())
                .setThumbnail(guild.getIconUrl())
                .build();
    }

    private static String mentionOrNa(net.dv8tion.jda.api.entities.channel.middleman.GuildChannel channel) {
        return channel != null ? channel.getAsMention() : "N/A";
    }

    private static String linkOrNa(String url, String label) {
        return url != null ? "[" + label + "](" + url + ")" : "N/A";
    }

    @Override
    public String getHelp() {
        return "</serverinfo:1075169253948399688> - Get information about the server.";
    }

    @Override
    public String getName() {
        return "serverinfo";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Get information about the server")
                .setContexts(InteractionContextType.GUILD);
    }
}
