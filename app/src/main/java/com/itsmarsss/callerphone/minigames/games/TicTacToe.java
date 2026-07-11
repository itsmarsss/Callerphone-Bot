package com.itsmarsss.callerphone.minigames.games;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.Constants;
import com.itsmarsss.callerphone.minigames.IMiniGame;
import com.itsmarsss.callerphone.minigames.MiniGameStatus;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class TicTacToe implements IMiniGame {
    private static final String X = "\u274E";
    private static final String O = "\uD83C\uDD7E";
    private static final String EMPTY = "\u2B1B";

    private String fromChannelID;
    private String toChannelID;
    private String fromMessageID;
    private String toMessageID;
    private String fromUserID;
    private String toUserID;
    private String id;
    private int stage;

    /** -1 empty, 0 = from player (X), 1 = to player (O) */
    private final int[][] board = {
            {-1, -1, -1},
            {-1, -1, -1},
            {-1, -1, -1}
    };

    public TicTacToe(String fromChannelID, String toChannelID, String fromUserID, String toUserID) {
        initGame(fromChannelID, toChannelID, fromUserID, toUserID);
    }

    public TicTacToe() {
    }

    @Override
    public void initGame(String fromChannelID, String toChannelID, String fromUserID, String toUserID) {
        this.fromChannelID = fromChannelID;
        this.toChannelID = toChannelID;
        this.fromUserID = fromUserID;
        this.toUserID = toUserID;
        this.id = UUID.randomUUID().toString().replace("-", "");
        this.stage = 0;
    }

    @Override
    public int checkForWin() {
        int win = checkRows();
        if (win != -1) {
            return win;
        }
        win = checkColumns();
        if (win != -1) {
            return win;
        }
        return checkDiagonals();
    }

    @Override
    public String getFromUserId() {
        return fromUserID;
    }

    @Override
    public String getToUserId() {
        return toUserID;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public String getToMessageId() {
        return toMessageID;
    }

    @Override
    public String getFromMessageId() {
        return fromMessageID;
    }

    @Override
    public void setToMessageId(String toID) {
        this.toMessageID = toID;
    }

    @Override
    public void setFromMessageId(String fromID) {
        this.fromMessageID = fromID;
    }

    @Override
    public String getToChannelId() {
        return toChannelID;
    }

    @Override
    public String getFromChannelId() {
        return fromChannelID;
    }

    @Override
    public void setToChannelId(String toID) {
        this.toChannelID = toID;
    }

    @Override
    public void setFromChannelId(String fromID) {
        this.fromChannelID = fromID;
    }

    public void incrementStage() {
        stage++;
    }

    public int getStage() {
        return stage;
    }

    public MiniGameStatus fromMove(int r, int c) {
        return place(r, c, 0);
    }

    public MiniGameStatus toMove(int r, int c) {
        return place(r, c, 1);
    }

    private MiniGameStatus place(int r, int c, int player) {
        if (r < 0 || r > 2 || c < 0 || c > 2 || board[r][c] != -1) {
            return MiniGameStatus.INVALID_MOVE;
        }
        board[r][c] = player;
        return MiniGameStatus.SUCCESS_MOVE;
    }

    private int checkRows() {
        for (int i = 0; i < 3; i++) {
            if (board[i][0] != -1 && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                return board[i][0];
            }
        }
        return -1;
    }

    private int checkColumns() {
        for (int i = 0; i < 3; i++) {
            if (board[0][i] != -1 && board[0][i] == board[1][i] && board[1][i] == board[2][i]) {
                return board[0][i];
            }
        }
        return -1;
    }

    private int checkDiagonals() {
        if (board[0][0] != -1 && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            return board[0][0];
        }
        if (board[0][2] != -1 && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            return board[0][2];
        }
        return -1;
    }

    public MessageCreateData getMessageForFrom() {
        return buildTurnMessage("from", toUserID, "has challenged you to a game of TicTacToe");
    }

    public MessageCreateData getMessageForTo() {
        return buildTurnMessage("to", fromUserID, "has challenged you to a game of TicTacToe");
    }

    private MessageCreateData buildTurnMessage(String side, String challengerId, String challengeText) {
        int win = checkForWin();
        if (win != -1) {
            return getBoardWithMessage("@" + resolveName(win == 0 ? fromUserID : toUserID) + " has won this game!");
        }
        if (stage >= Constants.TICTACTOE_MAX_STAGE) {
            return getBoardWithMessage("Tie game!");
        }

        return new MessageCreateBuilder()
                .setContent("@" + resolveName(challengerId) + " " + challengeText)
                .setComponents(getBoard(side))
                .build();
    }

    private String resolveName(String userId) {
        if (Callerphone.sdMgr == null || userId == null) {
            return "Unknown";
        }
        User user = Callerphone.sdMgr.getUserById(userId);
        return user != null ? user.getName() : "Unknown";
    }

    public Collection<ActionRow> getBoard(String side) {
        return buildBoard(side, false);
    }

    public MessageCreateData getBoardWithMessage(String msg) {
        return new MessageCreateBuilder()
                .setContent(msg)
                .setComponents(buildBoard(null, true))
                .build();
    }

    private Collection<ActionRow> buildBoard(String side, boolean allDisabled) {
        List<ActionRow> rows = new ArrayList<>(3);
        for (int r = 0; r < 3; r++) {
            List<Button> buttons = new ArrayList<>(3);
            for (int c = 0; c < 3; c++) {
                buttons.add(cellButton(r, c, side, allDisabled));
            }
            rows.add(ActionRow.of(buttons));
        }
        return rows;
    }

    private Button cellButton(int r, int c, String side, boolean forceDisabled) {
        int cell = board[r][c];
        if (cell == 0) {
            return Button.secondary("ttt-x-" + UUID.randomUUID(), X).asDisabled();
        }
        if (cell == 1) {
            return Button.secondary("ttt-o-" + UUID.randomUUID(), O).asDisabled();
        }
        if (forceDisabled || side == null) {
            return Button.secondary("ttt-e-" + UUID.randomUUID(), EMPTY).asDisabled();
        }
        // ttt-{side}-{gameId}-{row}-{col}
        return Button.secondary("ttt-" + side + "-" + id + "-" + r + "-" + c, EMPTY);
    }
}
