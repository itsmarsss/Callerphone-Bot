package com.marsss.callerphone.minigames.games;

import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.minigames.IMiniGame;
import com.marsss.callerphone.minigames.MiniGameStatus;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class TicTacToe implements IMiniGame {
    private String fromChannelID;
    private String toChannelID;

    private String fromMessageID;
    private String toMessageID;

    private String fromUserID;
    private String toUserID;

    private String id;
    private int stage;

    private final int[][] tttMap = new int[][]{
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
        return this.fromUserID;
    }

    @Override
    public String getToUserId() {
        return this.toUserID;
    }

    @Override
    public String getID() {
        return this.id;
    }

    @Override
    public String getToMessageId() {
        return this.toMessageID;
    }

    @Override
    public String getFromMessageId() {
        return this.fromMessageID;
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
        return this.toChannelID;
    }

    @Override
    public String getFromChannelId() {
        return this.fromChannelID;
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
        this.stage++;
    }

    public int getStage() {
        return this.stage;
    }

    public MiniGameStatus fromMove(int r, int c) {
        if (tttMap[r][c] == -1) {
            tttMap[r][c] = 0;
            return MiniGameStatus.SUCCESS_MOVE;
        }
        return MiniGameStatus.INVALID_MOVE;
    }

    public MiniGameStatus toMove(int r, int c) {
        if (tttMap[r][c] == -1) {
            tttMap[r][c] = 1;
            return MiniGameStatus.SUCCESS_MOVE;
        }
        return MiniGameStatus.INVALID_MOVE;
    }

    private int checkRows() {
        for (int i = 0; i < 3; i++) {
            if (tttMap[i][0] != -1 &&
                    tttMap[i][0] == tttMap[i][1] &&
                    tttMap[i][1] == tttMap[i][2]) {

                return tttMap[i][0];
            }
        }
        return -1;
    }

    private int checkColumns() {
        for (int i = 0; i < 3; i++) {
            if (tttMap[0][i] != -1 &&
                    tttMap[0][i] == tttMap[1][i] &&
                    tttMap[1][i] == tttMap[2][i]) {

                return tttMap[0][i];
            }
        }
        return -1;
    }

    private int checkDiagonals() {
        if (tttMap[0][0] != -1 &&
                tttMap[0][0] == tttMap[1][1] &&
                tttMap[1][1] == tttMap[2][2]) {

            return tttMap[0][0];
        }

        if (tttMap[0][2] != -1 &&
                tttMap[0][2] == tttMap[1][1] &&
                tttMap[1][1] == tttMap[2][0]) {

            return tttMap[0][0];
        }

        return -1;
    }

    public MessageCreateData getMessageForFrom() {
        MessageCreateBuilder message = new MessageCreateBuilder();

        int win = checkForWin();

        if (win != -1) {
            if (win == 0) {
                return getBoardWithMessage("@" + Callerphone.sdMgr.getUserById(fromUserID).getAsTag() + " has won this game!");
            } else if (win == 1) {
                return getBoardWithMessage("@" + Callerphone.sdMgr.getUserById(toUserID).getAsTag() + " has won this game!");
            }
        } else {
            message.setContent("@" + Callerphone.sdMgr.getUserById(toUserID).getAsTag() + " has challenged you to a game of TicTacToe");
        }

        if (this.stage == 9) {
            return getBoardWithMessage("Tie game!");
        }

        message.setComponents(getBoard("from"));

        return message.build();
    }

    public MessageCreateData getMessageForTo() {
        MessageCreateBuilder message = new MessageCreateBuilder();

        int win = checkForWin();

        if (win != -1) {
            if (win == 0) {
                return getBoardWithMessage("@" + Callerphone.sdMgr.getUserById(fromUserID).getAsTag() + " has won this game!");

            } else if (win == 1) {
                return getBoardWithMessage("@" + Callerphone.sdMgr.getUserById(toUserID).getAsTag() + " has won this game!");
            }
        } else {
            message.setContent("@" + Callerphone.sdMgr.getUserById(fromUserID).getAsTag() + " has challenged you to a game of TicTacToe");
        }

        if (this.stage == 9) {
            return getBoardWithMessage("Tie game!");
        }

        message.setComponents(getBoard("to"));

        return message.build();
    }

    public Collection<ActionRow> getBoard(String prefix) {
        Collection<ActionRow> collection = new ArrayList<>();
        Collection<Button> collection1 = new ArrayList<>();
        Collection<Button> collection2 = new ArrayList<>();
        Collection<Button> collection3 = new ArrayList<>();
        for (int c = 0; c < 3; c++) {

            if (tttMap[0][c] == 0) {
                collection1.add(Button.secondary("invalid1" + UUID.randomUUID(), "\u274E").asDisabled());
            } else if (tttMap[0][c] == 1) {
                collection1.add(Button.secondary("invalid1" + UUID.randomUUID(), "\uD83C\uDD7E").asDisabled());
            } else {
                collection1.add(Button.secondary(prefix + "-" + this.id + "-" + "0" + "-" + c + "-" + this.stage, "\u2B1B"));
            }

            if (tttMap[1][c] == 0) {
                collection2.add(Button.secondary("invalid2" + UUID.randomUUID(), "\u274E").asDisabled());
            } else if (tttMap[1][c] == 1) {
                collection2.add(Button.secondary("invalid2" + UUID.randomUUID(), "\uD83C\uDD7E").asDisabled());
            } else {
                collection2.add(Button.secondary(prefix + "-" + this.id + "-" + "1" + "-" + c + "-" + this.stage, "\u2B1B"));
            }

            if (tttMap[2][c] == 0) {
                collection3.add(Button.secondary("invalid3" + UUID.randomUUID(), "\u274E").asDisabled());
            } else if (tttMap[2][c] == 1) {
                collection3.add(Button.secondary("invalid3" + UUID.randomUUID(), "\uD83C\uDD7E").asDisabled());
            } else {
                collection3.add(Button.secondary(prefix + "-" + this.id + "-" + "2" + "-" + c + "-" + this.stage, "\u2B1B"));
            }

        }

        ActionRow row1 = ActionRow.of(collection1);
        collection.add(row1);
        ActionRow row2 = ActionRow.of(collection2);
        collection.add(row2);
        ActionRow row3 = ActionRow.of(collection3);
        collection.add(row3);

        return collection;
    }

    public MessageCreateData getBoardWithMessage(String msg) {
        MessageCreateBuilder message = new MessageCreateBuilder();

        message.setContent(msg);

        Collection<ActionRow> collection = new ArrayList<>();
        Collection<Button> collection1 = new ArrayList<>();
        Collection<Button> collection2 = new ArrayList<>();
        Collection<Button> collection3 = new ArrayList<>();
        for (int c = 0; c < 3; c++) {

            if (tttMap[0][c] == 0) {
                collection1.add(Button.secondary(UUID.randomUUID().toString(), "\u274E").asDisabled());
            } else if (tttMap[0][c] == 1) {
                collection1.add(Button.secondary(UUID.randomUUID().toString(), "\uD83C\uDD7E").asDisabled());
            } else {
                collection1.add(Button.secondary(UUID.randomUUID().toString(), "\u2B1B").asDisabled());
            }

            if (tttMap[1][c] == 0) {
                collection2.add(Button.secondary(UUID.randomUUID().toString(), "\u274E").asDisabled());
            } else if (tttMap[1][c] == 1) {
                collection2.add(Button.secondary(UUID.randomUUID().toString(), "\uD83C\uDD7E").asDisabled());
            } else {
                collection2.add(Button.secondary(UUID.randomUUID().toString(), "\u2B1B").asDisabled());
            }

            if (tttMap[2][c] == 0) {
                collection3.add(Button.secondary(UUID.randomUUID().toString(), "\u274E").asDisabled());
            } else if (tttMap[2][c] == 1) {
                collection3.add(Button.secondary(UUID.randomUUID().toString(), "\uD83C\uDD7E").asDisabled());
            } else {
                collection3.add(Button.secondary(UUID.randomUUID().toString(), "\u2B1B").asDisabled());
            }

        }

        ActionRow row1 = ActionRow.of(collection1);
        collection.add(row1);
        ActionRow row2 = ActionRow.of(collection2);
        collection.add(row2);
        ActionRow row3 = ActionRow.of(collection3);
        collection.add(row3);

        message.setComponents(collection);

        return message.build();
    }
}
