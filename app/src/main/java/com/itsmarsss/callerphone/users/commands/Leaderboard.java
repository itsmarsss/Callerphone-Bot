package com.itsmarsss.callerphone.users.commands;

import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/**
 * Placeholder until credit ranking is backed by an efficient Mongo aggregation.
 */
public class Leaderboard implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        e.reply(ToolSet.CP_EMJ + "Leaderboard is coming soon!")
                .setEphemeral(true)
                .queue();
    }

    @Override
    public String getHelp() {
        return "</leaderboard> - View the credits leaderboard.";
    }

    @Override
    public String getName() {
        return "leaderboard";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "View the credits leaderboard")
                .setContexts(InteractionContextType.GUILD);
    }
}
