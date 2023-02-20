package com.marsss.callerphone;

public enum Response {
    /*
    Naming convention: event/item_status
    eventitem_status("str"),
     */

    ERROR("An error occurred."),
    MISSING_PARAM("Missing parameters, do `%shelp <command name>` for more information."),

    PING_TEMPLATE("**Reset ping:** %sms \n**WS ping:** %sms"),

    // Profile
    PROFILE_GENERAL("Level: `%d`\nExperience: `%d/100`\nPrefix: %s\n\n[`%shelp exp`]"),
    PROFILE_CREDITS("Credits: `\u00A9 %d`\nRedeemed: `\u00A9 %d`\nNet: `\u00A9 %d`\n\n[`%shelp creds`]"),
    PROFILE_MESSAGE("Executed: `%d`\n Transmitted: `%s`\nTotal: `%s`"),

    CONNECTION_ERROR("Connection error, call ended."),
    DEFAULT_MESSAGE_TEMPLATE("**%s**#%s CP_CALL %s"),
    MODERATOR_MESSAGE_TEMPLATE("***[Moderator]* %s**#%s CP_CALL %s"),
    PREFIX_MESSAGE_TEMPLATE("***[%s]* %s**#%s CP_CALL %s"),
    MESSAGE_TOO_LONG(" I sent a message too long for Callerphone to handle "),
    ATTEMPTED_PING(" I tried to ping everyone "),
    ATTEMPTED_LINK(" I tried to send a link "),
    USER_TEMPLATE("\t\t{\n\t\t\t\"id\": \"%s\",\n\t\t\t\"status\": \"%s\",\n\t\t\t\"reason\": \"%s\",\n\t\t\t\"prefix\": \"%s\",\n\t\t\t\"credits\": %s,\n\t\t\t\"executed\": %s,\n\t\t\t\"transmitted\": %s\n\t\t}"),
    POOL_TEMPLATE("\t\t{\n\t\t\t\"hostID\": \"%s\",\n\t\t\t\"pwd\": \"%s\",\n\t\t\t\"cap\": %s,\n\t\t\t\"pub\": %s,\n\t\t\t\"children\": [\n\t\t\t\t%s\n\t\t\t]\n\t\t}"),

    NO_PERMISSION("You need `Manage Channel` permission to run this command."),
    TEMP("There is no set return message");
    public final String label;

    Response(String label) {
        this.label = label;
    }

    public String toString() {
        return label;
    }
}
