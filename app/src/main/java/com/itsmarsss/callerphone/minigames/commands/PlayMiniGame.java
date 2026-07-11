package com.itsmarsss.callerphone.minigames.commands;

import com.itsmarsss.callerphone.Response;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.minigames.MiniGame;
import com.itsmarsss.callerphone.minigames.games.TicTacToe;
import com.itsmarsss.callerphone.users.BotUser;
import com.itsmarsss.commandType.ISlashCommand;
import com.itsmarsss.database.categories.Users;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class PlayMiniGame implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        OptionMapping opponentOpt = e.getOption("opponent");
        if (opponentOpt == null || e.getSubcommandName() == null) {
            e.reply(Response.MISSING_PARAM.toString()).setEphemeral(true).queue();
            return;
        }

        User opponent = opponentOpt.getAsUser();
        MiniGame game;
        switch (e.getSubcommandName()) {
            case "tictactoe":
                game = MiniGame.TICTACTOE;
                break;
            default:
                e.reply(Response.MISSING_PARAM.toString()).setEphemeral(true).queue();
                return;
        }

        if (!Users.hasUser(opponent.getId())) {
            e.reply("Your opponent has not yet agreed to our Privacy Policy or Terms of Service.")
                    .setEphemeral(true).queue();
            return;
        }

        if (e.getUser().getId().equals(opponent.getId())) {
            e.reply("You cannot challenge yourself to a MiniGame.").setEphemeral(true).queue();
            return;
        }

        if (opponent.isBot() || opponent.isSystem()) {
            e.reply("You cannot challenge this player.").setEphemeral(true).queue();
            return;
        }

        e.reply(playMiniGame(e.getUser(), opponent, game)).queue();
    }

    private MessageCreateData playMiniGame(User from, User to, MiniGame game) {
        if (game == MiniGame.TICTACTOE) {
            return playTictactoe(from, to);
        }
        return new MessageCreateBuilder().setContent(Response.ERROR.toString()).build();
    }

    private MessageCreateData playTictactoe(User from, User to) {
        MessageCreateBuilder message = new MessageCreateBuilder();
        TicTacToe ttt = new TicTacToe(null, null, from.getId(), to.getId());

        BotUser fromUser = Users.getUser(from.getId());
        BotUser toUser = Users.getUser(to.getId());

        if (fromUser == null || toUser == null
                || !fromUser.addGame(ttt)
                || !toUser.addGame(ttt)) {
            if (fromUser != null) {
                fromUser.removeGame(ttt.getID());
            }
            if (toUser != null) {
                toUser.removeGame(ttt.getID());
            }
            message.setContent("One or both players have reached the game limit...");
        } else {
            message.setContent("Successfully challenged " + to.getAsMention() + " to a game of TicTacToe.");
            ToolSet.sendPrivateGameMessageFrom(from,
                    new MessageCreateBuilder()
                            .setContent("Your game of TicTacToe with @" + to.getName() + " will show up here.")
                            .build(),
                    ttt);
            ToolSet.sendPrivateGameMessageTo(to, ttt.getMessageForTo(), ttt);
        }
        return message.build();
    }

    @Override
    public String getHelp() {
        return "</game tictactoe:1089656103391985665> - Play TicTacToe minigame.";
    }

    @Override
    public String getName() {
        return "game";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Play a minigame with someone")
                .addSubcommands(
                        new SubcommandData("tictactoe", "Play TicTacToe with someone")
                                .addOptions(new OptionData(OptionType.USER, "opponent", "Who to challenge")
                                        .setRequired(true))
                )
                .setContexts(InteractionContextType.GUILD);
    }
}
