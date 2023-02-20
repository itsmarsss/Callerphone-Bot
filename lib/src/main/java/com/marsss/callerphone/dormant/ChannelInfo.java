package com.marsss.callerphone.dormant;

import java.awt.Color;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;

import com.marsss.callerphone.ToolSet;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

@SuppressWarnings("ConstantConditions")
public class ChannelInfo implements ICommand {

    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        final Message MESSAGE = e.getMessage();
        final String CONTENT = MESSAGE.getContentRaw();
        final String[] ARGS = CONTENT.split("\\s+");

        final List<TextChannel> CHANNELS = MESSAGE.getMentionedChannels();
        GuildChannel CHANNEL;

        try {
            CHANNEL = Callerphone.jda.getGuildChannelById(Long.parseLong(ARGS[1]));
        } catch (Exception ex) {
            ex.printStackTrace();
            CHANNEL = null;
        }

        if (CHANNELS.size() > 0) {
            if (CHANNEL == null)
                CHANNEL = CHANNELS.get(0);
        } else if (CHANNEL == null) {
            MESSAGE.replyEmbeds(textchannelinfo(e.getChannel())).queue();
        }

        ChannelType type;
        try {
            type = CHANNEL.getType();
        } catch (Exception ex) {
            ex.printStackTrace();
            MESSAGE.reply("Channel not recognized").queue();
            return;
        }

        switch (type) {

            case TEXT:
                MESSAGE.replyEmbeds(textchannelinfo(ToolSet.getTextChannel(CHANNEL.getId()))).queue();
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

    @Override
    public void runSlash(SlashCommandEvent e) {
        final GuildChannel CHANNEL = e.getOption("channel").getAsGuildChannel();
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
            PARENT = chnl.getParent().getAsMention();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            PARENT = "Server";
        }

        final String POSITION = String.valueOf(chnl.getPosition());
        final String ISNEWS = String.valueOf(chnl.isNews());
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
                .addField("News", ISNEWS, true)
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
            PARENT = chnl.getParent().getAsMention();
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
        return "`" + Callerphone.Prefix + "channelinfo <#channel/id/empty>` - Get information about the channel.";
    }

    @Override
    public String[] getTriggers() {
        return "channelinfo,chaninfo,channelinf,chaninf".split(",");
    }
}
