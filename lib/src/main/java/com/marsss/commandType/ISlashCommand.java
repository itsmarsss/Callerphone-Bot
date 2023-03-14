package com.marsss.commandType;

import com.marsss.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface ISlashCommand extends ICommand {
    void runSlash(SlashCommandInteractionEvent e);
}
