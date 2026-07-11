package com.itsmarsss.callerphone.tccallerphone;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.ToolSet;

public enum ChatResponse {

    NO_CALL(ToolSet.CP_EMJ + "There is no call."),
    OTHER_PARTY_HUNG_UP(ToolSet.CP_EMJ + "The other party hung up the phone."),
    HUNG_UP(ToolSet.CP_EMJ + "You hung up the phone."),
    LEFT_QUEUE(ToolSet.CP_EMJ + "You left the call queue."),
    ALREADY_CALL(ToolSet.CP_EMJ + "There is already a call going on!"),
    NO_PORT(ToolSet.CP_EMJ + "Hmmm, I was unable to find a conversation."),
    CALLING(ToolSet.CP_EMJ + "Calling..."),
    /** Args: position, queue size */
    QUEUED(ToolSet.CP_EMJ + "Calling... You are **#%d** in queue (%d waiting)."),
    /** Args: position, queue size */
    ALREADY_QUEUED(ToolSet.CP_EMJ + "You are already in queue at **#%d** (%d waiting). Use `/endchat` to leave."),
    PICKED_UP(ToolSet.CP_EMJ + "Someone picked up the phone!"),
    LEVEL_LOW(ToolSet.CP_EMJ + "You need to have at least 50 levels to set your own prefix."),
    SET_PREFIX_SUCCESS(ToolSet.CP_EMJ + "Successfully set your prefix to `%s`!"),
    CHAT_REPORTED_SUCCESS(ToolSet.CP_EMJ + "Chat reported!"),
    CHAT_REPORTED_NOT_SUCCESS(ToolSet.CP_EMJ + "Something went wrong, couldn't report call."),
    CHAT_REPORT_VIA_SERVER(ToolSet.CP_ERR + "Unable to report call, join our support server and provide images of the incident " + Callerphone.config.getSupportServer()),
    PREFIX_TOO_LONG(ToolSet.CP_EMJ + "Prefix too long (Maximum length is 15 characters)");

    public final String label;

    ChatResponse(String label) {
        this.label = label;
    }

    public String toString() {
        return label;
    }

    public String format(Object... args) {
        return String.format(label, args);
    }
}