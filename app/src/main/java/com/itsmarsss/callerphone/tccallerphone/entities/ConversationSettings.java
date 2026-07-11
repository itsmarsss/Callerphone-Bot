package com.itsmarsss.callerphone.tccallerphone.entities;

public final class ConversationSettings {
    private final boolean callerAnonymous;
    private final boolean receiverAnonymous;
    private final boolean filterProfanity;

    private ConversationSettings(boolean callerAnonymous, boolean receiverAnonymous, boolean filterProfanity) {
        this.callerAnonymous = callerAnonymous;
        this.receiverAnonymous = receiverAnonymous;
        this.filterProfanity = filterProfanity;
    }

    public static ConversationSettings forModes(ChatMode callerMode, ChatMode receiverMode) {
        boolean callerAnon = callerMode != null && callerMode.isAnonymous();
        boolean receiverAnon = receiverMode != null && receiverMode.isAnonymous();
        boolean filter = (callerMode != null && callerMode.filtersProfanity())
                || (receiverMode != null && receiverMode.filtersProfanity());
        return new ConversationSettings(callerAnon, receiverAnon, filter);
    }

    public boolean isCallerAnonymous() {
        return callerAnonymous;
    }

    public boolean isReceiverAnonymous() {
        return receiverAnonymous;
    }

    public boolean isFilterProfanity() {
        return filterProfanity;
    }

    public boolean isAnonymous(boolean fromCaller) {
        return fromCaller ? callerAnonymous : receiverAnonymous;
    }
}
