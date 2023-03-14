package com.marsss;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface ICommand {
    void runCommand(MessageReceivedEvent e);
    void runSlash(SlashCommandInteractionEvent e);
    String getHelp();

    String[] getTriggers();
}
