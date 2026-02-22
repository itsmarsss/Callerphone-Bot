package com.marsss.callerphone.minigames;

public interface IMiniGame {

    void initGame(String fromChannelID, String toChannelID, String fromUserID, String toUserID);
    int checkForWin();

    String getFromUserId();
    String getToUserId();

    String getID();

    String getFromMessageId();
    String getToMessageId();

    void setFromMessageId(String fromID);
    void setToMessageId(String toID);

    String getFromChannelId();
    String getToChannelId();

    void setFromChannelId(String fromID);
    void setToChannelId(String toID);
}
