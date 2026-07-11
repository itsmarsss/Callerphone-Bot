package com.itsmarsss.callerphone.minigames.commands;

import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ShowMiniGames implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        e.replyEmbeds(new EmbedBuilder()
                .setColor(ToolSet.COLOR)
                .setTitle("MiniGames")
                .setDescription(
                        "**Available**\n" +
                                "• TicTacToe — `/game tictactoe @opponent`\n\n" +
                                "**Coming soon**\n" +
                                "• Connect 4\n" +
                                "• Battleship\n" +
                                "• Word Search"
                )
                .setFooter("Challenge a friend in DMs after starting a game")
                .build()).queue();
    }

    @Override
    public String getHelp() {
        return "</games> - List available minigames.";
    }

    @Override
    public String getName() {
        return "games";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "List available minigames")
                .setContexts(InteractionContextType.GUILD);
    }
}
