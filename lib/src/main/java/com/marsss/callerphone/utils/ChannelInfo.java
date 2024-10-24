package com.marsss.callerphone.utils;

import com.marsss.callerphone.ToolSet;
import com.marsss.commandType.IFullCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ChannelInfo implements IFullCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        GuildChannel channel = e.getChannel().asGuildMessageChannel();

        if(!e.getOptions().isEmpty()) {
            channel = e.getOptions().get(0).getAsChannel();
        }

        e.replyEmbeds(sortChannelType(channel)).queue();
    }

    @Override
    public void runCommand(MessageReceivedEvent e) {
        List<GuildChannel> channels = e.getMessage().getMentions().getChannels();

        GuildChannel channel = e.getChannel().asGuildMessageChannel();

        if (!channels.isEmpty()) {
            channel = channels.get(0);
        }

        e.getMessage().replyEmbeds(sortChannelType(channel)).queue();
    }

    public MessageEmbed sortChannelType(GuildChannel channel) {
        ChannelType channelType = channel.getType();

        switch (channelType) {
            case TEXT:
                return textChannelInfo((TextChannel) channel);
            case VOICE:
                return voiceChannelInfo((VoiceChannel) channel);
            case CATEGORY:
                return categoryChannelInfo((Category) channel);
            default:
                return new EmbedBuilder().setDescription("Channel type not yet implemented.").build();
        }
    }

    public MessageEmbed textChannelInfo(TextChannel channel) {
        final String NAME = channel.getName();
        String TOPIC = channel.getTopic();
        final String TYPE = channel.getType().name();
        final String SLOWMODE = String.valueOf(channel.getSlowmode());
        final String ID = channel.getId();
        final String DATE_CREATED = channel.getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME);
        String PARENT;

        try {
            PARENT = channel.getParentCategory().getAsMention();
        } catch (NullPointerException ex) {
            PARENT = "Server";
        }

        final String POSITION = String.valueOf(channel.getPosition());
        final String ISNSFW = String.valueOf(channel.isNSFW());
        final String ISSYNCED = String.valueOf(channel.isSynced());

        if (TOPIC == null) {
            TOPIC = "No Topic";
        }

        EmbedBuilder channelInfoEmbed = new EmbedBuilder()
                .setColor(ToolSet.COLOR)
                .setDescription(":speech_left: **Channel information for " + channel.getAsMention() + ":**")
                .addField("Name", NAME, false)
                .addField("Topic", TOPIC, true)
                .addField("Type", TYPE, true)
                .addField("Slowmode", SLOWMODE + "s", true)
                .addField("Creation Date", DATE_CREATED, true)
                .addField("Parent", PARENT, true)
                .addField("Position", POSITION, true)
                .addField("NSFW", ISNSFW, true)
                .addField("Synced", ISSYNCED, true)
                .setFooter("ID: " + ID);

        return channelInfoEmbed.build();
    }

    public MessageEmbed voiceChannelInfo(VoiceChannel channel) {
        final String NAME = channel.getName();
        final String TYPE = String.valueOf(channel.getType());
        final String BITRATE = String.valueOf(channel.getBitrate());
        final String REGION = String.valueOf(channel.getRegion());
        String USERLIMIT = String.valueOf(channel.getUserLimit());
        final String ID = channel.getId();
        final String DATE_CREATED = channel.getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME);
        String PARENT;

        try {
            PARENT = channel.getParentCategory().getAsMention();
        } catch (NullPointerException ex) {
            PARENT = "Server";
        }

        final String POSITION = String.valueOf(channel.getPosition());
        final String ISSYNCED = String.valueOf(channel.isSynced());


        if (USERLIMIT.equals("0")) {
            USERLIMIT = "Unlimited";
        }

        EmbedBuilder channelInfoEmbed = new EmbedBuilder()
                .setColor(ToolSet.COLOR)
                .setDescription(":radio: **Channel information for " + channel.getAsMention() + ":**")
                .addField("Name", NAME, false)
                .addField("Type", TYPE, false)
                .addField("Bitrate", BITRATE + "kbps", true)
                .addField("Region", REGION, true)
                .addField("User Limit", USERLIMIT, true)
                .addField("Creation Date", DATE_CREATED, false)
                .addField("Parent", PARENT, true)
                .addField("Position", POSITION, true)
                .addField("Synced", ISSYNCED, true)
                .setFooter("ID: " + ID);

        return channelInfoEmbed.build();
    }

    public MessageEmbed categoryChannelInfo(Category channel) {
        final String NAME = channel.getName();
        final String TYPE = String.valueOf(channel.getType());
        final String TEXTCHANNELS = String.valueOf(channel.getTextChannels().size());
        final String VOICECHANNELS = String.valueOf(channel.getVoiceChannels().size());
        final String ID = channel.getId();
        final String DATE_CREATED = channel.getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME);
        final String POSITION = String.valueOf(channel.getPosition());

        EmbedBuilder channelInfoEmbed = new EmbedBuilder()
                .setColor(ToolSet.COLOR)
                .setDescription(":file_folder: **Category information for " + channel.getAsMention() + ":**")
                .addField("Name", NAME, false)
                .addField("Type", TYPE, true)
                .addField("TextChannels", TEXTCHANNELS, true)
                .addField("VoiceChannels", VOICECHANNELS, true)
                .addField("Creation Date", DATE_CREATED, false)
                .addField("Position", POSITION, false)
                .setFooter("ID: " + ID);

        return channelInfoEmbed.build();
    }

    @Override
    public String getHelp() {
        return "</channelinfo:1075169169877770290> <#channel/empty> - Get information about the channel.";
    }

    @Override
    public String[] getTriggers() {
        return "channelinfo,chaninfo,channelinf,chaninf".split(",");
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getTriggers()[0], getHelp().split(" - ")[1])
                .addOptions(
                        new OptionData(OptionType.CHANNEL, "channel", "Target channel")
                )
                .setGuildOnly(true);
    }
}
