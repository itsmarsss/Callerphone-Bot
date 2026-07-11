package com.itsmarsss.callerphone.tccallerphone;

import com.itsmarsss.callerphone.tccallerphone.entities.Conversation;

public final class ChatResult {
    private final ChatStatus status;
    private final Conversation conversation;

    private ChatResult(ChatStatus status, Conversation conversation) {
        this.status = status;
        this.conversation = conversation;
    }

    public static ChatResult queued() {
        return new ChatResult(ChatStatus.SUCCESS_CALLER, null);
    }

    public static ChatResult matched(Conversation conversation) {
        return new ChatResult(ChatStatus.SUCCESS_RECEIVER, conversation);
    }

    public static ChatResult conflict() {
        return new ChatResult(ChatStatus.CONFLICT, null);
    }

    public static ChatResult nonExistent() {
        return new ChatResult(ChatStatus.NON_EXISTENT, null);
    }

    public static ChatResult noCall() {
        return new ChatResult(ChatStatus.NO_CALL, null);
    }

    public ChatStatus getStatus() {
        return status;
    }

    public Conversation getConversation() {
        return conversation;
    }
}
