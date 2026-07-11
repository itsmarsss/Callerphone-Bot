package com.itsmarsss.callerphone.utils;

import com.itsmarsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ChannelInfo implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        OptionMapping option = e.getOption("channel");
        GuildChannel channel = option != null
                ? option.getAsChannel()
                : e.getChannel().asGuildMessageChannel();
        e.replyEmbeds(buildEmbed(channel)).queue();
    }

    public MessageEmbed buildEmbed(GuildChannel channel) {
        switch (channel.getType()) {
            case TEXT:
                return textChannelInfo((TextChannel) channel);
            case VOICE:
                return voiceChannelInfo((VoiceChannel) channel);
            case CATEGORY:
                return categoryChannelInfo((Category) channel);
            default:
                return EmbedHelpers.error("Channel type not yet implemented.");
        }
    }

    private MessageEmbed textChannelInfo(TextChannel channel) {
        return EmbedHelpers.base()
                .setDescription(":speech_left: **Channel information for " + channel.getAsMention() + ":**")
                .addField("Name", channel.getName(), false)
                .addField("Topic", EmbedHelpers.orDefault(channel.getTopic(), "No Topic"), true)
                .addField("Type", channel.getType().name(), true)
                .addField("Slowmode", channel.getSlowmode() + "s", true)
                .addField("Creation Date", channel.getTimeCreated().format(EmbedHelpers.DATE_FMT), true)
                .addField("Parent", EmbedHelpers.parentCategory(channel), true)
                .addField("Position", String.valueOf(channel.getPosition()), true)
                .addField("NSFW", String.valueOf(channel.isNSFW()), true)
                .addField("Synced", String.valueOf(channel.isSynced()), true)
                .setFooter("ID: " + channel.getId())
                .build();
    }

    private MessageEmbed voiceChannelInfo(VoiceChannel channel) {
        int limit = channel.getUserLimit();
        return EmbedHelpers.base()
                .setDescription(":radio: **Channel information for " + channel.getAsMention() + ":**")
                .addField("Name", channel.getName(), false)
                .addField("Type", channel.getType().name(), false)
                .addField("Bitrate", channel.getBitrate() + "kbps", true)
                .addField("Region", String.valueOf(channel.getRegion()), true)
                .addField("User Limit", limit == 0 ? "Unlimited" : String.valueOf(limit), true)
                .addField("Creation Date", channel.getTimeCreated().format(EmbedHelpers.DATE_FMT), false)
                .addField("Parent", EmbedHelpers.parentCategory(channel), true)
                .addField("Position", String.valueOf(channel.getPosition()), true)
                .addField("Synced", String.valueOf(channel.isSynced()), true)
                .setFooter("ID: " + channel.getId())
                .build();
    }

    private MessageEmbed categoryChannelInfo(Category channel) {
        return EmbedHelpers.base()
                .setDescription(":file_folder: **Category information for " + channel.getAsMention() + ":**")
                .addField("Name", channel.getName(), false)
                .addField("Type", channel.getType().name(), true)
                .addField("TextChannels", String.valueOf(channel.getTextChannels().size()), true)
                .addField("VoiceChannels", String.valueOf(channel.getVoiceChannels().size()), true)
                .addField("Creation Date", channel.getTimeCreated().format(EmbedHelpers.DATE_FMT), false)
                .addField("Position", String.valueOf(channel.getPosition()), false)
                .setFooter("ID: " + channel.getId())
                .build();
    }

    @Override
    public String getHelp() {
        return "</channelinfo:1075169169877770290> <#channel/empty> - Get information about the channel.";
    }

    @Override
    public String getName() {
        return "channelinfo";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Get information about a channel")
                .addOptions(new OptionData(OptionType.CHANNEL, "channel", "Target channel"))
                .setContexts(InteractionContextType.GUILD);
    }
}
