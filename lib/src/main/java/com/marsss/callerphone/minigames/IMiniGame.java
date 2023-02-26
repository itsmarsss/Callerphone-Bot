package com.marsss.callerphone.minigames;

public interface IMiniGame {

    void initGame(String fromChannelID, String toChannelID, String fromUserID, String toUserID);
    int checkForWin();

    String getFromUserId();
    String getToUserId();

    String getID();

    String getToMessageId();
    String getFromMessageId();
    void setToMessageId(String toID);
    void setFromMessageId(String fromID);

    String getToChannelId();
    String getFromChannelId();
    void setToChannelId(String toID);
    void setFromChannelId(String fromID);
}
