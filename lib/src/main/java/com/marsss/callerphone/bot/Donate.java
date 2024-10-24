package com.marsss.callerphone.bot;

import com.marsss.callerphone.ToolSet;
import com.marsss.commandType.IFullCommand;
import com.marsss.callerphone.Callerphone;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class Donate implements IFullCommand {

    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        e.replyEmbeds(donate()).setEphemeral(true).queue();
    }

    @Override
    public void runCommand(MessageReceivedEvent e) {
        e.getMessage().replyEmbeds(donate()).queue();
    }

    private MessageEmbed donate() {
        return new EmbedBuilder()
                .setColor(ToolSet.COLOR)
                .setDescription("Donate at <" + Callerphone.config.getDonateLink() + ">")
                .build();
    }

    @Override
    public String getHelp() {
        return "</donate:1075168879443185745> - Help us out by donating.";
    }

    @Override
    public String[] getTriggers() {
        return "donate,don".split(",");
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getTriggers()[0], getHelp().split(" - ")[1])
                .setGuildOnly(true);
    }
}
