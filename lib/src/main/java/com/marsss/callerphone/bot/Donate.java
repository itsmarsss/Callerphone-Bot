package com.marsss.callerphone.bot;

import com.marsss.commandType.ISlashCommand;
import com.marsss.callerphone.Callerphone;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

public class Donate implements ISlashCommand {

    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        e.replyEmbeds(donate()).setEphemeral(true).queue();
    }

    @Override
    public String getHelp() {
        return "`" + Callerphone.config.getPrefix() + "donate` - Help us out by donating.";
    }

    @Override
    public String[] getTriggers() {
        return "donate,don".split(",");
    }

    private MessageEmbed donate() {
        return new EmbedBuilder().
                setColor(new Color(114, 137, 218))
                .setDescription("Donate at <" + Callerphone.config.getDonateLink() + ">")
                .build();
    }
}
