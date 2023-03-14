package com.marsss.callerphone.utils;

import com.marsss.callerphone.Callerphone;
import com.marsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.time.format.DateTimeFormatter;

public class ChannelInfo implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        final GuildChannel CHANNEL = e.getOption("channel").getAsChannel();
        final ChannelType TYPE = CHANNEL.getType();

        switch (TYPE) {

            case TEXT:
                e.replyEmbeds(textchannelinfo((TextChannel) CHANNEL)).queue();
                break;
            case VOICE:
                e.replyEmbeds(voicechannelinfo((VoiceChannel) CHANNEL)).queue();
                break;
            case CATEGORY:
                e.replyEmbeds(categorychannelinfo((Category) CHANNEL)).queue();
                break;
            default:
                e.reply("Channel not recognized").queue();
                break;
        }
    }

    public MessageEmbed textchannelinfo(TextChannel chnl) {
        final String NAME = chnl.getName();
        String TOPIC = chnl.getTopic();
        final String TYPE = chnl.getType().name();
        final String SLOWMODE = String.valueOf(chnl.getSlowmode());
        final String ID = chnl.getId();
        final String DATE_CREATED = chnl.getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME);
        String PARENT;

        try {
            PARENT = chnl.getParentCategory().getAsMention();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            PARENT = "Server";
        }

        final String POSITION = String.valueOf(chnl.getPosition());
        final String ISNSFW = String.valueOf(chnl.isNSFW());
        final String ISSYNCED = String.valueOf(chnl.isSynced());

        if (TOPIC == null) {
            TOPIC = "No Topic";
        }

        EmbedBuilder ChnlInfEmd = new EmbedBuilder()
                .setColor(new Color(114, 137, 218))
                .setDescription(":speech_left: **Channel information for " + chnl.getAsMention() + ":**")
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

        return ChnlInfEmd.build();
    }

    public MessageEmbed voicechannelinfo(VoiceChannel chnl) {
        final String NAME = chnl.getName();
        final String TYPE = String.valueOf(chnl.getType());
        final String BITRATE = String.valueOf(chnl.getBitrate());
        final String REGION = String.valueOf(chnl.getRegion());
        String USERLIMIT = String.valueOf(chnl.getUserLimit());
        final String ID = chnl.getId();
        final String DATE_CREATED = chnl.getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME);
        String PARENT;

        try {
            PARENT = chnl.getParentCategory().getAsMention();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            PARENT = "Server";
        }

        final String POSITION = String.valueOf(chnl.getPosition());
        final String ISSYNCED = String.valueOf(chnl.isSynced());


        if (USERLIMIT.equals("0")) {
            USERLIMIT = "Unlimited";
        }

        EmbedBuilder ChnlInfEmd = new EmbedBuilder()
                .setColor(new Color(114, 137, 218))
                .setDescription(":radio: **Channel information for " + chnl.getAsMention() + ":**")
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

        return ChnlInfEmd.build();
    }

    public MessageEmbed categorychannelinfo(Category chnl) {
        final String NAME = chnl.getName();
        final String TYPE = String.valueOf(chnl.getType());
        final String TEXTCHANNELS = String.valueOf(chnl.getTextChannels().size());
        final String VOICECHANNELS = String.valueOf(chnl.getVoiceChannels().size());
        final String ID = chnl.getId();
        final String DATE_CREATED = chnl.getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME);
        final String POSITION = String.valueOf(chnl.getPosition());

        EmbedBuilder ChnlInfEmd = new EmbedBuilder()
                .setColor(new Color(114, 137, 218))
                .setDescription(":file_folder: **Category information for " + chnl.getAsMention() + ":**")
                .addField("Name", NAME, false)
                .addField("Type", TYPE, true)
                .addField("TextChannels", TEXTCHANNELS, true)
                .addField("VoiceChannels", VOICECHANNELS, true)
                .addField("Creation Date", DATE_CREATED, false)
                .addField("Position", POSITION, false)
                .setFooter("ID: " + ID);

        return ChnlInfEmd.build();
    }

    @Override
    public String getHelp() {
        return "`" + Callerphone.config.getPrefix() + "channelinfo <#channel/id/empty>` - Get information about the channel.";
    }

    @Override
    public String[] getTriggers() {
        return "channelinfo,chaninfo,channelinf,chaninf".split(",");
    }
}
