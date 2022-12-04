package com.marsss.callerphone.bot;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;

public class Donate implements ICommand {

    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        e.getMessage().replyEmbeds(donate()).queue();
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        e.replyEmbeds(donate()).setEphemeral(true).queue();
    }

    @Override
    public String getHelpF() {
        return "`" + Callerphone.Prefix + "donate` - Help us out by donating.";
    }

    @Override
    public String[] getTriggers() {
        return "donate,don".split(",");
    }

    private MessageEmbed donate() {
        return new EmbedBuilder().
                setColor(new Color(114, 137, 218))
                .setDescription("Donate at <" + Callerphone.donate + ">")
                .build();
    }
}
