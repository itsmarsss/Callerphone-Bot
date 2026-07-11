package com.itsmarsss.callerphone.minigames;

import java.util.UUID;

/**
 * Shared identity/channel state for two-player minigames.
 */
public abstract class AbstractMiniGame implements IMiniGame {
    protected String fromChannelId;
    protected String toChannelId;
    protected String fromMessageId;
    protected String toMessageId;
    protected String fromUserId;
    protected String toUserId;
    protected String id;

    @Override
    public void initGame(String fromChannelID, String toChannelID, String fromUserID, String toUserID) {
        this.fromChannelId = fromChannelID;
        this.toChannelId = toChannelID;
        this.fromUserId = fromUserID;
        this.toUserId = toUserID;
        this.id = UUID.randomUUID().toString().replace("-", "");
    }

    @Override
    public String getFromUserId() {
        return fromUserId;
    }

    @Override
    public String getToUserId() {
        return toUserId;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public String getFromMessageId() {
        return fromMessageId;
    }

    @Override
    public String getToMessageId() {
        return toMessageId;
    }

    @Override
    public void setFromMessageId(String fromID) {
        this.fromMessageId = fromID;
    }

    @Override
    public void setToMessageId(String toID) {
        this.toMessageId = toID;
    }

    @Override
    public String getFromChannelId() {
        return fromChannelId;
    }

    @Override
    public String getToChannelId() {
        return toChannelId;
    }

    @Override
    public void setFromChannelId(String fromID) {
        this.fromChannelId = fromID;
    }

    @Override
    public void setToChannelId(String toID) {
        this.toChannelId = toID;
    }
}
