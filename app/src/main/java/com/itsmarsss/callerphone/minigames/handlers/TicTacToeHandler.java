package com.itsmarsss.callerphone.minigames.handlers;

import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.discord.match.MatchPresenter;
import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.callerphone.experience.ExperienceView;
import com.itsmarsss.callerphone.experience.ExperienceIntent;
import com.itsmarsss.callerphone.minigames.MiniGameStatus;
import com.itsmarsss.callerphone.minigames.games.TicTacToe;
import com.itsmarsss.callerphone.users.BotUser;
import com.itsmarsss.commandType.IButtonInteraction;
import com.itsmarsss.database.categories.Users;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
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
            replyGameExpired(e);
            return;
        }

        String[] param = customId.substring("ttt-".length()).split("-");
        if (param.length < 4) {
            replyGameExpired(e);
            return;
        }

        BotUser botUser = Users.getUser(e.getUser().getId());
        if (botUser == null) {
            e.reply(ExperienceRenderer.toMessage(ExperienceView.builder(ExperienceIntent.WARNING)
                    .title("Accept terms first")
                    .description("Run `/match join` once so games can track your session.")
                    .build())).setEphemeral(true).queue();
            return;
        }

        TicTacToe game = (TicTacToe) botUser.getGame(param[1]);
        if (game == null) {
            e.reply(ExperienceRenderer.toMessage(MatchPresenter.generalGameShelf())).setEphemeral(true).queue();
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
            replyGameExpired(e);
            return;
        }

        String side = param[0];
        MiniGameStatus stat;
        if ("to".equals(side)) {
            stat = game.toMove(row, col);
        } else if ("from".equals(side)) {
            stat = game.fromMove(row, col);
        } else {
            replyGameExpired(e);
            return;
        }
        if (stat == MiniGameStatus.INVALID_MOVE) {
            e.reply(ExperienceRenderer.toMessage(ExperienceView.builder(ExperienceIntent.WARNING)
                    .title("That square is taken")
                    .description("Pick an empty cell on your board.")
                    .build())).setEphemeral(true).queue();
            return;
        }

        int win = game.checkForWin();
        if (win != -1) {
            MessageCreateData winnerBoard = game.getBoardWithMessage(
                    "**You won.** Good game — rematch from chat or call controls."
            );
            MessageCreateData loserBoard = game.getBoardWithMessage(
                    "**They won.** The call or chat stays open."
            );
            if (win == 0) {
                updateBoard(game.getFromChannelId(), game.getFromMessageId(), winnerBoard);
                updateBoard(game.getToChannelId(), game.getToMessageId(), loserBoard);
            } else {
                updateBoard(game.getToChannelId(), game.getToMessageId(), winnerBoard);
                updateBoard(game.getFromChannelId(), game.getFromMessageId(), loserBoard);
            }
            e.getMessage().editMessage(MessageEditData.fromCreateData(
                    e.getUser().getId().equals(win == 0 ? game.getFromUserId() : game.getToUserId())
                            ? winnerBoard
                            : loserBoard
            )).queue();
            endGame(game);
            return;
        }

        game.incrementStage();

        if (game.getStage() >= 9) {
            MessageCreateData tie = game.getBoardWithMessage("**Tie.** Good game — challenge again anytime.");
            e.getMessage().editMessage(MessageEditData.fromCreateData(tie)).queue();
            updateBoard(game.getFromChannelId(), game.getFromMessageId(), tie);
            updateBoard(game.getToChannelId(), game.getToMessageId(), tie);
            endGame(game);
            return;
        }

        if ("to".equals(side)) {
            notifyOpponent(game.getFromChannelId(), game.getFromMessageId(), game.getMessageForFrom());
        } else {
            notifyOpponent(game.getToChannelId(), game.getToMessageId(), game.getMessageForTo());
        }
        e.getMessage().editMessage(MessageEditData.fromCreateData(
                game.getBoardWithMessage("Move sent — waiting on them.")
        )).queue();
    }

    private void notifyOpponent(String channelId, String messageId, MessageCreateData board) {
        MessageChannel channel = resolveChannel(channelId);
        if (channel == null) {
            return;
        }

        updateBoard(channel, messageId, board);
        // Quiet turn ping only in DMs — call channels already show the edited board.
        if (channel instanceof PrivateChannel) {
            channel.sendMessage("Your turn.").queue(msg ->
                    msg.delete().queueAfter(2, TimeUnit.SECONDS, null, err -> {
                    }));
        }
    }

    private void updateBoard(String channelId, String messageId, MessageCreateData board) {
        MessageChannel channel = resolveChannel(channelId);
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

    /** Guild call channels or private DMs. */
    private MessageChannel resolveChannel(String channelId) {
        if (channelId == null) {
            return null;
        }
        try {
            return ToolSet.getMessageChannel(channelId);
        } catch (Exception e) {
            logger.debug("Could not resolve game channel {}", channelId, e);
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

    private static void replyGameExpired(ButtonInteraction e) {
        e.reply(ExperienceRenderer.toMessage(ExperienceView.builder(ExperienceIntent.WARNING)
                .title("That game expired")
                .description("Start a fresh board from a call or Match chat.")
                .build())).setEphemeral(true).queue();
    }

    @Override
    public String getID() {
        return "ttt";
    }
}
