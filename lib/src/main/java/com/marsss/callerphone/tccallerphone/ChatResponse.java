package com.marsss.callerphone.tccallerphone;

import com.marsss.callerphone.ToolSet;

public enum ChatResponse {

    NO_CALL("There is no call."),
    OTHER_PARTY_HUNG_UP("The other party hung up the phone."),
    HUNG_UP("You hung up the phone."),
    ALREADY_CALL("There is already a call going on!"),
    NO_PORT("Hmmm, I was unable to find an conversation."),
    CALLING("Calling..."),
    PICKED_UP("Someone picked up the phone!"),
    LEVEL_LOW("You need to have at least 50 levels to set your own prefix."),
    SET_PREFIX_SUCCESS("Successfully set your prefix to `%s`!"),
    CHAT_REPORTED_SUCCESS("Chat reported!"),
    CHAT_REPORTED_NOT_SUCCESS("Something went wrong, couldn't report call."),
    PREFIX_TOO_LONG("Prefix too long (Maximum length is 15 characters)");

    public final String label;

    ChatResponse(String label) {
        this.label = label;
    }

    public String toString() {
        return label;
    }

}
