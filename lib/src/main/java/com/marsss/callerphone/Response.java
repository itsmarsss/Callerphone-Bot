package com.marsss.callerphone;

public enum Response {
    /*
    Naming convention: event/item_status
    eventitem_status("str"),
     */

    ERROR(ToolSet.CP_EMJ + "An error occurred."),
    MISSING_PARAM(ToolSet.CP_EMJ + "Missing parameters, do `" + Callerphone.Prefix + "help <command name>` for more information."),

    PING_TEMPLATE("**Reset ping:** %sms \n**WS ping:** %sms"),

    // Profile
    PROFILE_GENERAL("Level: `%d`\nExperience: `%d/100`\nPrefix: %s\n\n[`" + Callerphone.Prefix + "help exp`]"),
    PROFILE_CREDITS("Credits: `\u00A9 %d`\nRedeemed: `\u00A9 %d`\nNet: `\u00A9 %d`\n\n[`" + Callerphone.Prefix + "help creds`]"),
    PROFILE_MESSAGE("Executed: `%d`\n Transmitted: `%s`\nTotal: `%s`"),

    CONNECTION_ERROR(ToolSet.CP_EMJ + "Connection error, call ended."),
    DEFAULT_MESSAGE_TEMPLATE("**%s**#%s " + Callerphone.CallerphoneCall + "%s"),
    MODERATOR_MESSAGE_TEMPLATE("***[Moderator]* %s**#%s " + Callerphone.CallerphoneCall + "%s"),
    PREFIX_MESSAGE_TEMPLATE("***[%s]* %s**#%s " + Callerphone.CallerphoneCall + "%s"),
    MESSAGE_TOO_LONG(ToolSet.CP_ERR + " I sent a message too long for Callerphone to handle " + ToolSet.CP_ERR),
    ATTEMPTED_PING(ToolSet.CP_ERR + " I tried to ping everyone " + ToolSet.CP_ERR),
    ATTEMPTED_LINK(ToolSet.CP_ERR + " I tried to send a link " + ToolSet.CP_ERR),
    TEMP("There is no set return message");
    public final String label;

    Response(String label) {
        this.label = label;
    }

    public String toString() {
        return label;
    }
}
