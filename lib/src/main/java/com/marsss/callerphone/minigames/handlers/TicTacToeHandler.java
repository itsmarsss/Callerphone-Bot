package com.marsss.callerphone.minigames.handlers;

import com.marsss.callerphone.Callerphone;
import com.marsss.database.Storage;
import com.marsss.callerphone.minigames.MiniGameStatus;
import com.marsss.callerphone.minigames.games.TicTacToe;
import com.marsss.commandType.IButtonInteraction;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.util.concurrent.TimeUnit;

public class TicTacToeHandler implements IButtonInteraction {
    @Override
    public void runClick(ButtonInteraction e) {
        String[] param = e.getButton().getId().replaceFirst("ttt-", "").split("-");


        TicTacToe game = (TicTacToe) Storage.getUser(e.getUser().getId()).getGame(param[1]);

        if(game == null) {
            e.reply("Game not found.").setEphemeral(true).queue();
            e.getMessage().delete().queue();
            return;
        }

        if (param[0].equals("to")) {
            MiniGameStatus stat = game.toMove(Integer.parseInt(param[2]), Integer.parseInt(param[3]));

            if(stat == MiniGameStatus.INVALID_MOVE) {
                e.reply("Invalid move.").setEphemeral(true).queue();
                return;
            }

            MessageChannel channel = Callerphone.jda.getPrivateChannelById(game.getFromChannelId());

            channel.retrieveMessageById(game.getFromMessageId()).queue((message) -> {
                message.editMessage(MessageEditData.fromCreateData(game.getMessageForFrom())).queue();
                channel.sendMessage("You've got a game!").queue((msg) -> {
                    msg.delete().queueAfter(1, TimeUnit.SECONDS);
                });
            }, new ErrorHandler().handle(ErrorResponse.UNKNOWN_MESSAGE, (err) -> {
                channel.sendMessage(game.getMessageForFrom()).queue();
                channel.sendMessage("You've got a game!").queue((msg) -> {
                    msg.delete().queueAfter(1, TimeUnit.SECONDS);
                });
            }));
        } else if (param[0].equals("from")) {
            MiniGameStatus stat = game.fromMove(Integer.parseInt(param[2]), Integer.parseInt(param[3]));

            if(stat == MiniGameStatus.INVALID_MOVE) {
                e.reply("Invalid move.").setEphemeral(true).queue();
                return;
            }

            MessageChannel channel = Callerphone.jda.getPrivateChannelById(game.getToChannelId());

            channel.retrieveMessageById(game.getToMessageId()).queue((message) -> {
                message.editMessage(MessageEditData.fromCreateData(game.getMessageForTo())).queue();
                channel.sendMessage("You've got a game!").queue((msg) -> {
                    msg.delete().queueAfter(1, TimeUnit.SECONDS);
                });
            }, new ErrorHandler().handle(ErrorResponse.UNKNOWN_MESSAGE, (err) -> {
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
                    message.editMessage(MessageEditData.fromCreateData(game.getMessageForFrom())).queue();
                }, new ErrorHandler().handle(ErrorResponse.UNKNOWN_MESSAGE, (err) -> {
                    channel.sendMessage(game.getMessageForFrom()).queue();
                }));
            } else if (win == 1) {
                MessageChannel channel = Callerphone.jda.getPrivateChannelById(game.getToChannelId());

                channel.retrieveMessageById(game.getToMessageId()).queue((message) -> {
                    message.editMessage(MessageEditData.fromCreateData(game.getMessageForTo())).queue();
                }, new ErrorHandler().handle(ErrorResponse.UNKNOWN_MESSAGE, (err) -> {
                    channel.sendMessage(game.getMessageForTo()).queue();
                }));
            }
            Storage.getUser(game.getFromUserId()).removeGame(game.getID());
            Storage.getUser(game.getToUserId()).removeGame(game.getID());
        }

        game.incrementStage();

        if(game.getStage() == 9) {
            e.getMessage().editMessage(MessageEditData.fromCreateData(game.getBoardWithMessage("Tie Game!"))).queue();
            Storage.getUser(game.getFromUserId()).removeGame(game.getID());
            Storage.getUser(game.getToUserId()).removeGame(game.getID());
        }else{
            e.getMessage().editMessage(MessageEditData.fromCreateData(game.getBoardWithMessage("Game Sent!"))).queue();
        }
    }

    @Override
    public String getID() {
        return "ttt";
    }
}
