package com.marsss.callerphone.users.commands;

import com.marsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Leaderboard implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {

    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public String[] getTriggers() {
        return "leaderboard,leader,board,lb,top".split(",");
    }
}
