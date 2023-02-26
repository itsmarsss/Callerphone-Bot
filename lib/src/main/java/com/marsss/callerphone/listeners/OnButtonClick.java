package com.marsss.callerphone.listeners;

import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.Storage;
import com.marsss.callerphone.minigames.games.TicTacToe;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class OnButtonClick extends ListenerAdapter {
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        String[] param = event.getButton().getId().split("-");


        TicTacToe game = (TicTacToe) Storage.getUser(event.getUser().getId()).getGame(param[1]);

        if(game == null) {
            event.reply("Game not found.").setEphemeral(true).queue();
            event.getMessage().delete().queue();
            return;
        }

        game.incrementStage();

        if (param[0].equals("to")) {
            game.toMove(Integer.parseInt(param[2]), Integer.parseInt(param[3]));

            MessageChannel channel = Callerphone.jda.getPrivateChannelById(game.getFromChannelId());

            channel.retrieveMessageById(game.getFromMessageId()).queue((message) -> {
                message.editMessage(game.getMessageForFrom()).queue();
                channel.sendMessage("You've got a game!").queue((msg) -> {
                    msg.delete().queueAfter(1, TimeUnit.SECONDS);
                });
            }, new ErrorHandler().handle(ErrorResponse.UNKNOWN_MESSAGE, (e) -> {
                channel.sendMessage(game.getMessageForFrom()).queue();
                channel.sendMessage("You've got a game!").queue((msg) -> {
                    msg.delete().queueAfter(1, TimeUnit.SECONDS);
                });
            }));
        } else if (param[0].equals("from")) {
            game.fromMove(Integer.parseInt(param[2]), Integer.parseInt(param[3]));

            MessageChannel channel = Callerphone.jda.getPrivateChannelById(game.getToChannelId());

            channel.retrieveMessageById(game.getToMessageId()).queue((message) -> {
                message.editMessage(game.getMessageForTo()).queue();
                channel.sendMessage("You've got a game!").queue((msg) -> {
                    msg.delete().queueAfter(1, TimeUnit.SECONDS);
                });
            }, new ErrorHandler().handle(ErrorResponse.UNKNOWN_MESSAGE, (e) -> {
                channel.sendMessage(game.getMessageForTo()).queue();
                channel.sendMessage("You've got a game!").queue((msg) -> {
                    msg.delete().queueAfter(1, TimeUnit.SECONDS);
                });
            }));
        }

        int win = game.checkForWin();

        if (win != -1) {
            if (win == 0) {
                MessageChannel channel = Callerphone.jda.getPrivateChannelById(game.getFromChannelId());

                channel.retrieveMessageById(game.getFromMessageId()).queue((message) -> {
                    message.editMessage(game.getMessageForFrom()).queue();
                }, new ErrorHandler().handle(ErrorResponse.UNKNOWN_MESSAGE, (e) -> {
                    channel.sendMessage(game.getMessageForFrom()).queue();
                }));
            } else if (win == 1) {
                MessageChannel channel = Callerphone.jda.getPrivateChannelById(game.getToChannelId());

                channel.retrieveMessageById(game.getToMessageId()).queue((message) -> {
                    message.editMessage(game.getMessageForTo()).queue();
                }, new ErrorHandler().handle(ErrorResponse.UNKNOWN_MESSAGE, (e) -> {
                    channel.sendMessage(game.getMessageForTo()).queue();
                }));
            }
            Storage.getUser(game.getFromUserId()).removeGame(game.getID());
            Storage.getUser(game.getToUserId()).removeGame(game.getID());
        }

        event.deferEdit().queue();

        if(game.getStage() == 9) {
            event.getMessage().editMessage(game.boardWithMessage("Tie Game!")).queue();
            Storage.getUser(game.getFromUserId()).removeGame(game.getID());
            Storage.getUser(game.getToUserId()).removeGame(game.getID());
        }else{
            event.getMessage().editMessage(game.boardWithMessage("Game Sent!")).queue();
        }


    }
}
