package com.marsss.callerphone.utils;

import com.marsss.callerphone.ToolSet;
import com.marsss.commandType.IFullCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.time.format.DateTimeFormatter;

public class ServerInfo implements IFullCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        e.replyEmbeds(serverInfo(e.getGuild())).queue();
    }

    @Override
    public void runCommand(MessageReceivedEvent e) {
        e.getMessage().replyEmbeds(serverInfo(e.getGuild())).queue();
    }

    private MessageEmbed serverInfo(Guild gld) {
        final String NAME = gld.getName();
        String DESCRIPTION = gld.getDescription();

        final String ICONURL = gld.getIconUrl();
        final String BANNERURL = gld.getBannerUrl();

        final String MEMBERS = String.valueOf(gld.getMembers().size());
        final String OWNER = gld.getOwner().getAsMention();


        final String BOOSTCOUNT = String.valueOf(gld.getBoostCount());
        final String BOOSTTIER = gld.getBoostTier().toString();

        String CATEGORIES = "N/A";
        String CHANNELS = "N/A";
        String TEXTCHANNELS = "N/A";
        String VOICECHANNELS = "N/A";
        String STAGECHANNELS = "N/A";
        String SYSTEMCHANNEL = "N/A";
        String RULESCHANNEL = "N/A";
        String COMMUNITYUPDATESCHANNEL = "N/A";

        String ROLES = "N/A";

        String AFKCHANNEL = "N/A";
        String AFKTIMEOUT = "N/A";

        if (DESCRIPTION == null)
            DESCRIPTION = "N/A";

        try {
            CATEGORIES = String.valueOf(gld.getCategories().size());
            CHANNELS = String.valueOf(gld.getChannels().size());
            TEXTCHANNELS = String.valueOf(gld.getTextChannels().size());
            VOICECHANNELS = String.valueOf(gld.getVoiceChannels().size());
            STAGECHANNELS = String.valueOf(gld.getStageChannels().size());
            SYSTEMCHANNEL = gld.getSystemChannel().getAsMention();
            RULESCHANNEL = gld.getRulesChannel().getAsMention();
            COMMUNITYUPDATESCHANNEL = gld.getCommunityUpdatesChannel().getAsMention();

            ROLES = String.valueOf(gld.getRoles().size());

            AFKCHANNEL = gld.getAfkChannel().getAsMention();
            AFKTIMEOUT = gld.getAfkTimeout().getSeconds() + "s";
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }

        final String REGION = gld.retrieveRegions().toString();
        final String DATECREATED = gld.getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME);

        EmbedBuilder serverInfoEmbed = new EmbedBuilder()
                .setColor(ToolSet.COLOR)
                .setDescription(":minidisc: **Server information for " + NAME + ":**")
                .addField("General Information",
                        "Name: " + NAME +
                                "\nDescription: " + DESCRIPTION +
                                "\nIcon URL: [Icon](" + ICONURL + ")" +
                                "\nBanner URL: [Banner](" + BANNERURL + ")",
                        false)

                .addField("Categories",
                        "Category Count: " + CATEGORIES +
                                "\nChannel Count: " + CHANNELS +
                                "\nTextChannel Count: " + TEXTCHANNELS +
                                "\nVoiceChannel Count: " + VOICECHANNELS +
                                "\nStageChannel Count: " + STAGECHANNELS +
                                "\nSystem Channel: " + SYSTEMCHANNEL +
                                "\nRules Channel: " + RULESCHANNEL +
                                "\nCommunity Update Channel: " + COMMUNITYUPDATESCHANNEL,
                        false)

                .addField("Members",
                        "Member Count: " + MEMBERS +
                                "\nOwner: " + OWNER,
                        true)

                .addField("Boosts",
                        "Boost Count: " + BOOSTCOUNT +
                                "\nBoost Tier: " + BOOSTTIER,
                        true)

                .addField("Roles", "Role Count: " + ROLES,
                        true)

                .addField("Region",
                        "Region: " + REGION +
                                "\nCreation Date: " + DATECREATED,
                        true)

                .addField("AFK",
                        "AFK Channel: " + AFKCHANNEL +
                                "\nAFK Timeout: " + AFKTIMEOUT,
                        true)

                .setFooter("ID: " + gld.getId())
                .setThumbnail(ICONURL);

        return serverInfoEmbed.build();
    }

    @Override
    public String getHelp() {
        return "</serverinfo:1075169253948399688> - Get information about the server.";
    }

    @Override
    public String[] getTriggers() {
        return "serverinfo,servinfo,serverinf,servinf".split(",");
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getTriggers()[0], getHelp().split(" - ")[1])
                .setGuildOnly(true);
    }
}
