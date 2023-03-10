package com.marsss.callerphone.minigames.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.Response;
import com.marsss.callerphone.Storage;
import com.marsss.callerphone.ToolSet;
import com.marsss.callerphone.minigames.MiniGame;
import com.marsss.callerphone.minigames.games.TicTacToe;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.List;

public class PlayMiniGame implements ICommand {

    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        Message message = e.getMessage();
        String raw = message.getContentRaw();

        String[] args = raw.split("\\s+");

        List<Member> pings = message.getMentionedMembers();

        if (pings.size() == 0) {
            e.getMessage().reply(Response.MISSING_PARAM.toString()).queue();
            return;
        }

        User opponent = pings.get(0).getUser();

        MiniGame game;

        switch (args[1].toLowerCase()) {
            case "tictactoe":
                game = MiniGame.TICTACTOE;
                break;
            default:
                e.getMessage().reply(Response.MISSING_PARAM.toString()).queue();
                return;
        }

        if (!Storage.hasUser(opponent.getId())) {
            e.getMessage().reply("Your opponent has not yet agreed to our Privacy Policy or Terms of Service. Get them to run `" + Callerphone.config.getPrefix() + "agree`").queue();
            return;
        }

        if(e.getAuthor().getId().equals(opponent.getId())) {
            e.getMessage().reply("You cannot challenge yourself to a MiniGame.").queue();
            return;
        }

        if(opponent.isBot()) {
            e.getMessage().reply("I cannot send a message to this bot.").queue();
            return;
        }

        e.getMessage().reply(playMiniGame(e.getAuthor(), opponent, game)).queue();
    }

    @Override
    public void runSlash(SlashCommandEvent e) {

    }

    private Message playMiniGame(User from, User to, MiniGame game) {
        switch (game) {
            case TICTACTOE:
                return playTictactoe(from, to);
        }
        return new MessageBuilder().setContent(Response.ERROR.toString()).build();
    }

    private Message playTictactoe(User from, User to) {
        MessageBuilder message = new MessageBuilder();

        TicTacToe ttt = new TicTacToe(null, null, from.getId(), to.getId());

        if (!Storage.getUser(from.getId()).addGame(ttt) ||
                !Storage.getUser(to.getId()).addGame(ttt)) {
            Storage.getUser(from.getId()).removeGame(ttt.getID());
            Storage.getUser(to.getId()).removeGame(ttt.getID());

            message.setContent("One or both players have reached the game limit...");
        } else {
            message.setContent("Successfully challenged " + to.getAsMention() + " to a game of TicTacToe.");

            ToolSet.sendPrivateGameMessageFrom(from, new MessageBuilder().setContent("You game of TicTacToe with @" + to.getAsTag() + " will show up here.").build(), ttt);


            ToolSet.sendPrivateGameMessageTo(to, ttt.getMessageForTo(), ttt);
        }
        return message.build();
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public String[] getTriggers() {
        return "game,play,minigame,playmini,playgame".split(",");
    }
}
