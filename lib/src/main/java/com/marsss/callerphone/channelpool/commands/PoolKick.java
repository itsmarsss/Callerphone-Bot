package com.marsss.callerphone.channelpool.commands;

import com.marsss.Command;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class PoolKick implements Command {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        // TODO:
        // Password
        // Kick channels
        // Pool capacity (max 10)
        // Private/Public
    }

    @Override
    public void runSlash(SlashCommandEvent e) {

    }

    public static String getHelp() {
        return "Help Here";
    }

    @Override
    public String getHelpF() {
        return null;
    }

    @Override
    public String[] getTriggers() {
        return "settingspool,settingpool,setpool".split(",");
    }
}