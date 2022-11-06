package com.marsss.callerphone.utils;

import java.awt.Color;
import java.time.format.DateTimeFormatter;

import com.marsss.Command;
import com.marsss.callerphone.Callerphone;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class BotInfo implements Command {

    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        final JDA jda = Callerphone.jda;
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        final String DESC = "**Tag of the bot:** " + jda.getSelfUser().getAsTag() +
                "\n**Avatar url:** [link](" + jda.getSelfUser().getAvatarUrl() + ")" +
                "\n**Time created:** " + dtf.format(jda.getSelfUser().getTimeCreated()) +
                "\n**Id:** " + jda.getSelfUser().getId() +
                "\n**Shard info:** [" + (jda.getShardInfo().getShardId() + 1) + "/" + jda.getShardInfo().getShardTotal() + "]" +
                "\n**Servers:** " + jda.getGuilds().size();

        EmbedBuilder BotInfo = new EmbedBuilder()
                .setColor(Color.cyan)
                .setTitle("**Bot Info**")
                .setDescription(DESC);

        e.getMessage().replyEmbeds(BotInfo.build());
    }

    @Override
    public String getHelp() {
        return "`" + Callerphone.Prefix + "botinfo` - Get information about the bot.";
    }

    @Override
    public String[] getTriggers() {
        return "botinfo,info".split(",");
    }

}
