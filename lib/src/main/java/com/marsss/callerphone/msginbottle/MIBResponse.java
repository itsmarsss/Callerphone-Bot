package com.marsss.callerphone.msginbottle;

import com.marsss.callerphone.ToolSet;

public enum MIBResponse {
    SEND_SUCCESS(ToolSet.CP_EMJ + "Your bottle has been sent!"),
    FIND_MAX(ToolSet.CP_ERR + "You have reached your max find limit."),
    SEND_MAX(ToolSet.CP_ERR + "You have reached your max send limit."),
    MESSAGE_FLAGGED(ToolSet.CP_ERR + "Your message has been flagged. Please remove any links, pings, or inappropriate words.");
    public final String label;

    MIBResponse(String label) {
        this.label = label;
    }

    public String toString() {
        return label;
    }
}
