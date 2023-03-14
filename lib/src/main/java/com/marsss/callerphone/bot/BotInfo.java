package com.marsss.callerphone.bot;

import java.awt.Color;
import java.time.format.DateTimeFormatter;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.MessageReceivedEvent;

public class BotInfo implements ICommand {

    @Override
    public void runCommand(MessageReceivedEvent e) {
        e.getMessage().replyEmbeds(botInfo()).queue();
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        e.replyEmbeds(botInfo()).setEphemeral(true).queue();
    }

    private final JDA jda = Callerphone.jda;
    private final StringBuilder DESCRIPTION = new StringBuilder()
            .append("**Tag of the bot:** ").append(jda.getSelfUser().getAsTag())
            .append("\n**Avatar url:** [link](").append(jda.getSelfUser().getAvatarUrl()).append(")")
            .append("\n**Time created:** ").append(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(jda.getSelfUser().getTimeCreated()))
            .append("\n**Id:** ").append(jda.getSelfUser().getId())
            .append("\n**Shard info:** [").append(jda.getShardInfo().getShardId() + 1).append("/").append(jda.getShardInfo().getShardTotal()).append("]")
            .append("\n**Servers:** ").append(jda.getGuilds().size());

    private MessageEmbed botInfo() {
        EmbedBuilder BotInfo = new EmbedBuilder()
                .setColor(new Color(114, 137, 218))
                .setTitle("**Bot Info**")
                .setDescription(DESCRIPTION);

        return BotInfo.build();
    }

    @Override
    public String getHelp() {
        return "`" + Callerphone.config.getPrefix() + "botinfo` - Get information about the bot.";
    }

    @Override
    public String[] getTriggers() {
        return "botinfo,info".split(",");
    }

}
