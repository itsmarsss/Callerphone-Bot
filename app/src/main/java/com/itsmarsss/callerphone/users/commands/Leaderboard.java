package com.itsmarsss.callerphone.users.commands;

import com.itsmarsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class Leaderboard implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {

    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public String getName() {
        return "leaderboard";
    }

    @Override
    public SlashCommandData getCommandData() {
        return null;
    }
}
