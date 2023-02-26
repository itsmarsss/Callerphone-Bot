package com.marsss.callerphone.minigames.commands;

import com.marsss.ICommand;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class ShowMiniGames implements ICommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {

    }

    @Override
    public void runSlash(SlashCommandEvent e) {

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
