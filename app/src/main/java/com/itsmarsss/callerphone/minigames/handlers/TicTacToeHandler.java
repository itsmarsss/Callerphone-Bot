package com.itsmarsss.callerphone.minigames.handlers;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.minigames.MiniGameStatus;
import com.itsmarsss.callerphone.minigames.games.TicTacToe;
import com.itsmarsss.callerphone.users.BotUser;
import com.itsmarsss.commandType.IButtonInteraction;
import com.itsmarsss.database.categories.Users;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class TicTacToeHandler implements IButtonInteraction {
    private static final Logger logger = LoggerFactory.getLogger(TicTacToeHandler.class);

    @Override
    public void runClick(ButtonInteraction e) {
        String customId = e.getButton().getCustomId();
        if (customId == null || !customId.startsWith("ttt-")) {
            e.reply("Invalid game button.").setEphemeral(true).queue();
            return;
        }

        String[] param = customId.substring("ttt-".length()).split("-");
        if (param.length < 4) {
            e.reply("Invalid game button.").setEphemeral(true).queue();
            return;
        }

        BotUser botUser = Users.getUser(e.getUser().getId());
        if (botUser == null) {
            e.reply("User session not found.").setEphemeral(true).queue();
            return;
        }

        TicTacToe game = (TicTacToe) botUser.getGame(param[1]);
        if (game == null) {
            e.reply("Game not found.").setEphemeral(true).queue();
            e.getMessage().delete().queue(null, err -> {
            });
            return;
        }

        int row;
        int col;
        try {
            row = Integer.parseInt(param[2]);
            col = Integer.parseInt(param[3]);
        } catch (NumberFormatException ex) {
            e.reply("Invalid move coordinates.").setEphemeral(true).queue();
            return;
        }

        String side = param[0];
        MiniGameStatus stat;
        if ("to".equals(side)) {
            stat = game.toMove(row, col);
            if (stat == MiniGameStatus.INVALID_MOVE) {
                e.reply("Invalid move.").setEphemeral(true).queue();
                return;
            }
            notifyOpponent(game.getFromChannelId(), game.getFromMessageId(), game.getMessageForFrom());
        } else if ("from".equals(side)) {
            stat = game.fromMove(row, col);
            if (stat == MiniGameStatus.INVALID_MOVE) {
                e.reply("Invalid move.").setEphemeral(true).queue();
                return;
            }
            notifyOpponent(game.getToChannelId(), game.getToMessageId(), game.getMessageForTo());
        } else {
            e.reply("Invalid game button.").setEphemeral(true).queue();
            return;
        }

        int win = game.checkForWin();
        if (win != -1) {
            if (win == 0) {
                updateBoard(game.getFromChannelId(), game.getFromMessageId(), game.getMessageForFrom());
            } else if (win == 1) {
                updateBoard(game.getToChannelId(), game.getToMessageId(), game.getMessageForTo());
            }
            endGame(game);
        }

        game.incrementStage();

        if (game.getStage() == 9) {
            e.getMessage().editMessage(MessageEditData.fromCreateData(game.getBoardWithMessage("Tie Game!"))).queue();
            endGame(game);
        } else {
            e.getMessage().editMessage(MessageEditData.fromCreateData(game.getBoardWithMessage("Game Sent!"))).queue();
        }
    }

    private void notifyOpponent(String channelId, String messageId, MessageCreateData board) {
        MessageChannel channel = getPrivateChannel(channelId);
        if (channel == null) {
            return;
        }

        updateBoard(channel, messageId, board);
        channel.sendMessage("You've got a game!").queue(msg ->
                msg.delete().queueAfter(1, TimeUnit.SECONDS, null, err -> {
                }));
    }

    private void updateBoard(String channelId, String messageId, MessageCreateData board) {
        MessageChannel channel = getPrivateChannel(channelId);
        if (channel != null) {
            updateBoard(channel, messageId, board);
        }
    }

    private void updateBoard(MessageChannel channel, String messageId, MessageCreateData board) {
        if (messageId == null) {
            channel.sendMessage(board).queue();
            return;
        }
        channel.retrieveMessageById(messageId).queue(
                message -> message.editMessage(MessageEditData.fromCreateData(board)).queue(),
                new ErrorHandler().handle(ErrorResponse.UNKNOWN_MESSAGE, err -> channel.sendMessage(board).queue())
        );
    }

    private MessageChannel getPrivateChannel(String channelId) {
        if (channelId == null || Callerphone.sdMgr == null) {
            return null;
        }
        try {
            return Callerphone.sdMgr.getPrivateChannelById(channelId);
        } catch (Exception e) {
            logger.debug("Could not resolve private channel {}", channelId, e);
            return null;
        }
    }

    private void endGame(TicTacToe game) {
        BotUser from = Users.getUser(game.getFromUserId());
        BotUser to = Users.getUser(game.getToUserId());
        if (from != null) {
            from.removeGame(game.getID());
        }
        if (to != null) {
            to.removeGame(game.getID());
        }
    }

    @Override
    public String getID() {
        return "ttt";
    }
}
