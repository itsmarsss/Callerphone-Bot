package com.marsss.callerphone;

public enum Response {
    /*
    Naming convention: classname_event/item_status
    classname_eventitem_status("str"),
     */

    // Universal
    ERROR(ToolSet.CP_EMJ + "An error occurred."),
    MISSING_PARAM(ToolSet.CP_EMJ + "Missing parameters, do `" + Callerphone.Prefix + "help <command name>` for more information."),

    // Ping
    PING_TEMPLATE("**Reset ping:** %sms \n**WS ping:** %sms"),

    // Profile
    PROFILE_GENERAL("Level: `%d`\nExperience: `%d/100`\nPrefix: %s\n\n[`" + Callerphone.Prefix + "help exp`]"),
    PROFILE_CREDITS("Credits: `\u00A9 %d`\nRedeemed: `\u00A9 %d`\nNet: `\u00A9 %d`\n\n[`" + Callerphone.Prefix + "help creds`]"),
    PROFILE_MESSAGE("Executed: `%d`\n Transmitted: `%s`\nTotal: `%s`"),

    CONNECTION_ERROR(ToolSet.CP_EMJ + "Connection error, call ended."),
    DEFAULT_MESSAGE_TEMPLATE("**%s**#%s " + Callerphone.CallerphoneCall + "%s"),
    MODERATOR_MESSAGE_TEMPLATE("***[Moderator]* %s**#%s " + Callerphone.CallerphoneCall + "%s"),
    PREFIX_MESSAGE_TEMPLATE("***[%s]* %s**#%s " + Callerphone.CallerphoneCall + "%s"),
    MESSAGE_TOO_LONG(":x: I sent a message too long for Callerphone to handle! :x:"),
    ATTEMPTED_PING(":x: I tried to ping everyone :( :x:"),
    ATTEMPTED_LINK(":x: I tried to send a link :( :x:"),
    // Pools
    // EndPool
    TEMP("asd");
    public final String label;

    Response(String label) {
        this.label = label;
    }

    public String toString() {
        return label;
    }
}
