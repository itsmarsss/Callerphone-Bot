package com.marsss;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public interface ICommand {
    void runCommand(MessageReceivedEvent e);
    void runSlash(SlashCommandEvent e);
    String getHelp();

    String[] getTriggers();
}
