package com.marsss.callerphone.utils;

import java.awt.Color;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.marsss.Command;
import com.marsss.callerphone.Callerphone;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class ChannelInfo implements Command {

    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        final Message MESSAGE = e.getMessage();
        final String CONTENT = MESSAGE.getContentRaw();
        final String args[] = CONTENT.split("\\s+");

        List<TextChannel> CHANNELS = MESSAGE.getMentionedChannels();
        GuildChannel CHANNEL;

        try {
            CHANNEL = Callerphone.jda.getGuildChannelById(Long.parseLong(args[1]));
        } catch (Exception ex) {
            CHANNEL = null;
        }

        if (CHANNELS.size() > 0) {
            if (CHANNEL == null)
                CHANNEL = CHANNELS.get(0);
        } else if (CHANNEL == null) {
            //				MESSAGE.reply("Getting info").queue(message -> {
            //					message.editMessage("").queue();
            //					message.editMessageEmbeds(ChannelInfo.textchannelinfo(event.getChannel())).queue();
            //				});
            MESSAGE.replyEmbeds(textchannelinfo(e.getChannel())).queue();
        }

        ChannelType type;
        try {
            type = CHANNEL.getType();
        } catch (Exception ex) {
            MESSAGE.reply("Channel not recognized").queue();
            return;
        }

        switch (type) {

            case TEXT:
                MESSAGE.replyEmbeds(textchannelinfo(Callerphone.jda.getTextChannelById(CHANNEL.getId()))).queue();
                break;
            case VOICE:
                MESSAGE.replyEmbeds(voicechannelinfo(Callerphone.jda.getVoiceChannelById(CHANNEL.getId()))).queue();
                break;
            case CATEGORY:
                MESSAGE.replyEmbeds(categorychannelinfo(Callerphone.jda.getCategoryById(CHANNEL.getId()))).queue();
                break;
            default:
                MESSAGE.reply("Channel not recognized").queue();
                break;

        }
    }

    public static String getHelp() {
        return "`" + Callerphone.Prefix + "channelinfo <#channel/id/empty>` - Get information about the channel.";
    }

    @Override
    public String[] getTriggers() {
        return "channelinfo,chaninfo,channelinf,chaninf".split(",");
    }

    public MessageEmbed textchannelinfo(TextChannel chnl) {
        String NAME = chnl.getName();
        String TOPIC = chnl.getTopic();
        String TYPE = chnl.getType().name();
        String SLOWMODE = String.valueOf(chnl.getSlowmode());
        String ID = chnl.getId();
        String DATE_CREATED = chnl.getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME);
        String PARENT;

        try {
            PARENT = chnl.getParent().getAsMention();
        } catch (NullPointerException ex) {
            PARENT = "Server";
        }

        String POSITION = String.valueOf(chnl.getPosition());
        String ISNEWS = String.valueOf(chnl.isNews());
        String ISNSFW = String.valueOf(chnl.isNSFW());
        String ISSYNCED = String.valueOf(chnl.isSynced());

        if (TOPIC == null) {
            TOPIC = "No Topic";
        }

        EmbedBuilder ChnlInfEmd = new EmbedBuilder()
                .setColor(Color.cyan)
                .setDescription("🗨️ **Channel information for " + chnl.getAsMention() + ":**")
                .addField("Name", NAME, false)
                .addField("Topic", TOPIC, true)
                .addField("Type", TYPE, true)
                .addField("Slowmode", SLOWMODE + "s", true)
                .addField("Creation Date", DATE_CREATED, true)
                .addField("Parent", PARENT, true)
                .addField("Position", POSITION, true)
                .addField("News", ISNEWS, true)
                .addField("NSFW", ISNSFW, true)
                .addField("Synced", ISSYNCED, true)
                .setFooter("ID: " + ID);

        return ChnlInfEmd.build();
    }

    public MessageEmbed voicechannelinfo(VoiceChannel chnl) {
        String NAME = chnl.getName();
        String TYPE = String.valueOf(chnl.getType());
        String BITRATE = String.valueOf(chnl.getBitrate());
        String REGION = String.valueOf(chnl.getRegion());
        String USERLIMIT = String.valueOf(chnl.getUserLimit());
        String ID = chnl.getId();
        String DATE_CREATED = chnl.getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME);
        String PARENT;

        try {
            PARENT = chnl.getParent().getAsMention();
        } catch (NullPointerException ex) {
            PARENT = "Server";
        }

        String POSITION = String.valueOf(chnl.getPosition());
        String ISSYNCED = String.valueOf(chnl.isSynced());


        if (USERLIMIT.equals("0")) {
            USERLIMIT = "Unlimited";
        }

        EmbedBuilder ChnlInfEmd = new EmbedBuilder()
                .setColor(Color.cyan)
                .setDescription("📻 **Channel information for " + chnl.getAsMention() + ":**")
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
        String NAME = chnl.getName();
        String TYPE = String.valueOf(chnl.getType());
        String TEXTCHANNELS = String.valueOf(chnl.getTextChannels().size());
        String VOICECHANNELS = String.valueOf(chnl.getVoiceChannels().size());
        String ID = chnl.getId();
        String DATE_CREATED = chnl.getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME);
        String POSITION = String.valueOf(chnl.getPosition());

        EmbedBuilder ChnlInfEmd = new EmbedBuilder()
                .setColor(Color.cyan)
                .setDescription("📁 **Category information for " + chnl.getAsMention() + ":**")
                .addField("Name", NAME, false)
                .addField("Type", TYPE, true)
                .addField("TextChannels", TEXTCHANNELS, true)
                .addField("VoiceChannels", VOICECHANNELS, true)
                .addField("Creation Date", DATE_CREATED, false)
                .addField("Position", POSITION, false)
                .setFooter("ID: " + ID);

        return ChnlInfEmd.build();
    }

}
