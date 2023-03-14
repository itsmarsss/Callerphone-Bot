package com.marsss.callerphone.channelpool.commands;

import com.marsss.ICommand;
import net.dv8tion.jda.api.events.interaction.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.guild.MessageReceivedEvent;

public class PoolProperties implements ICommand {
    @Override
    public void runCommand(MessageReceivedEvent e) {

    }

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
