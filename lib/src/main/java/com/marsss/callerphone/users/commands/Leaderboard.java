package com.marsss.callerphone.users.commands;

import com.marsss.ICommand;
import net.dv8tion.jda.api.events.interaction.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.guild.MessageReceivedEvent;

public class Leaderboard implements ICommand {
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
