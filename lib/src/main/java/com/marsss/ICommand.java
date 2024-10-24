package com.marsss;

import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public interface ICommand {
    String getHelp();

    String[] getTriggers();

    SlashCommandData getCommandData();
}
