package com.marsss.callerphone.minigames.commands;

import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.Response;
import com.marsss.callerphone.ToolSet;
import com.marsss.callerphone.minigames.MiniGame;
import com.marsss.callerphone.minigames.games.TicTacToe;
import com.marsss.commandType.ISlashCommand;
import com.marsss.database.categories.Users;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class PlayMiniGame implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        User opponent = e.getOption("opponent").getAsUser();

        MiniGame game;

        switch (e.getSubcommandName()) {
            case "tictactoe":
                game = MiniGame.TICTACTOE;
                break;
            default:
                e.reply(Response.MISSING_PARAM.toString()).queue();
                return;
        }

        if (!Users.hasUser(opponent.getId())) {
            e.reply("Your opponent has not yet agreed to our Privacy Policy or Terms of Service. Get them to run `" + Callerphone.config.getPrefix() + "agree`").queue();
            return;
        }

        if (e.getUser().getId().equals(opponent.getId())) {
            e.reply("You cannot challenge yourself to a MiniGame.").queue();
            return;
        }

        if (opponent.isBot()) {
            e.reply("You cannot challenge this player.").queue();
            return;
        }

        e.reply(playMiniGame(e.getUser(), opponent, game)).queue();
    }

    private MessageCreateData playMiniGame(User from, User to, MiniGame game) {
        switch (game) {
            case TICTACTOE:
                return playTictactoe(from, to);
        }
        return new MessageCreateBuilder().setContent(Response.ERROR.toString()).build();
    }

    private MessageCreateData playTictactoe(User from, User to) {
        MessageCreateBuilder message = new MessageCreateBuilder();

        TicTacToe ttt = new TicTacToe(null, null, from.getId(), to.getId());

        if (!Users.getUser(from.getId()).addGame(ttt) ||
                !Users.getUser(to.getId()).addGame(ttt)) {
            Users.getUser(from.getId()).removeGame(ttt.getID());
            Users.getUser(to.getId()).removeGame(ttt.getID());

            message.setContent("One or both players have reached the game limit...");
        } else {
            message.setContent("Successfully challenged " + to.getAsMention() + " to a game of TicTacToe.");

            ToolSet.sendPrivateGameMessageFrom(from, new MessageCreateBuilder().setContent("You game of TicTacToe with @" + to.getName() + " will show up here.").build(), ttt);


            ToolSet.sendPrivateGameMessageTo(to, ttt.getMessageForTo(), ttt);
        }
        return message.build();
    }

    @Override
    public String getHelp() {
        return "</game tictactoe:1089656103391985665> - Play TicTacToe minigame.";
    }

    @Override
    public String[] getTriggers() {
        return "game,play,minigame,playmini,playgame".split(",");
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getTriggers()[0], getHelp().split(" - ")[1])
                .addSubcommands(
                        new SubcommandData("tictactoe", "Play TicTacToe with someone")
                                .addOptions(
                                        new OptionData(OptionType.USER, "opponent", "Who to challenge")
                                                .setRequired(true)
                                )
                )
                .setGuildOnly(true);
    }
}
