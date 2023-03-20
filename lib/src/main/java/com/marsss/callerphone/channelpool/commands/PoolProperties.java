package com.marsss.callerphone.channelpool.commands;

import com.marsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class PoolProperties implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {

    }

    private String poolProperties() {
        return "N/A";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public String[] getTriggers() {
        return new String[0];
    }
}
