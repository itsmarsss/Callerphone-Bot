package com.itsmarsss.callerphone.tccallerphone;

import com.itsmarsss.callerphone.tccallerphone.entities.Conversation;

public final class ChatResult {
    private final ChatStatus status;
    private final Conversation conversation;
    private final int queuePosition;
    private final int queueSize;

    private ChatResult(ChatStatus status, Conversation conversation, int queuePosition, int queueSize) {
        this.status = status;
        this.conversation = conversation;
        this.queuePosition = queuePosition;
        this.queueSize = queueSize;
    }

    public static ChatResult queued(int position, int size) {
        return new ChatResult(ChatStatus.SUCCESS_CALLER, null, position, size);
    }

    public static ChatResult alreadyQueued(int position, int size) {
        return new ChatResult(ChatStatus.ALREADY_QUEUED, null, position, size);
    }

    public static ChatResult matched(Conversation conversation) {
        return new ChatResult(ChatStatus.SUCCESS_RECEIVER, conversation, -1, -1);
    }

    public static ChatResult conflict() {
        return new ChatResult(ChatStatus.CONFLICT, null, -1, -1);
    }

    public static ChatResult nonExistent() {
        return new ChatResult(ChatStatus.NON_EXISTENT, null, -1, -1);
    }

    public static ChatResult noCall() {
        return new ChatResult(ChatStatus.NO_CALL, null, -1, -1);
    }

    public ChatStatus getStatus() {
        return status;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public int getQueuePosition() {
        return queuePosition;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public boolean hasQueueInfo() {
        return queuePosition > 0 && queueSize > 0;
    }
}
