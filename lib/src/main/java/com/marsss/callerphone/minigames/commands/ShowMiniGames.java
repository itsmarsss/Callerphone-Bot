package com.marsss.callerphone.minigames.commands;

import com.marsss.ICommand;
import net.dv8tion.jda.api.events.interaction.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.guild.MessageReceivedEvent;

public class ShowMiniGames implements ICommand {

    @Override
    public void runSlash(SlashCommandInteractionEvent e) {

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
