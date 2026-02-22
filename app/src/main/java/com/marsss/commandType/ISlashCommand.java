package com.marsss.commandType;

import com.marsss.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public interface ISlashCommand extends ICommand {
    void runSlash(SlashCommandInteractionEvent e);

    SlashCommandData getCommandData();
}
