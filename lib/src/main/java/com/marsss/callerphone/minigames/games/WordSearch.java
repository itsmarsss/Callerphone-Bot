package com.marsss.callerphone.minigames.games;

import com.marsss.callerphone.minigames.IMiniGame;
import net.dv8tion.jda.api.entities.Message;

public class WordSearch implements IMiniGame {
    @Override
    public void initGame(String fromChannelID, String toChannelID, String fromUserID, String toUserID) {

    }

    @Override
    public int checkForWin() {
        return -1;
    }

    @Override
    public String getFromUserId() {
        return null;
    }

    @Override
    public String getToUserId() {
        return null;
    }

    @Override
    public String getID() {
        return null;
    }

    @Override
    public String getToMessageId() {
        return null;
    }

    @Override
    public String getFromMessageId() {
        return null;
    }

    @Override
    public void setToMessageId(String toID) {

    }

    @Override
    public void setFromMessageId(String fromID) {

    }

    @Override
    public String getToChannelId() {
        return null;
    }

    @Override
    public String getFromChannelId() {
        return null;
    }

    @Override
    public void setToChannelId(String toID) {

    }

    @Override
    public void setFromChannelId(String fromID) {

    }
    // Fields
    /* Private
    From channel ID (String)
    To channel ID (String)
    From user ID (String)
    To user ID (String)

    2D array int[][]

    All valid word list arr String[]
    From user word list arr String[]
    To user word list arr String[]
     */

    /* Methods
    check for win
    generate valid words
    check valid word
    getters and setters
     */
}
