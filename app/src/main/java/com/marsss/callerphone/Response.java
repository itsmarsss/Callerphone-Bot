package com.marsss.callerphone;

public enum Response {
    /*
    Naming convention: event/item_status
    eventitem_status("str"),
     */

    ERROR(ToolSet.CP_EMJ + "An error occurred."),
    MISSING_PARAM(ToolSet.CP_EMJ + "Missing parameters, do `/help <command name>` for more information."),

    PING_TEMPLATE("**Reset ping:** %sms \n**WS ping:** %sms"),

    // Profile
    PROFILE_GENERAL("Level: `%d`\nExperience: `%d/100`\nPrefix: %s\n\n[`/help exp`]"),
    PROFILE_CREDITS("Credits: `\u23E3 %d`\nRedeemed: `\u23E3 %d`\nNet: `\u23E3 %d`\n\n[`/help creds`]"),
    PROFILE_MESSAGE("Executed: `%d`\n Transmitted: `%s`\nTotal: `%s`"),

    CONNECTION_ERROR(ToolSet.CP_EMJ + "Connection error, call ended."),
    DEFAULT_MESSAGE_TEMPLATE("**%s** " + ToolSet.CP_CALL + "%s"),
    MODERATOR_MESSAGE_TEMPLATE("***[Moderator]* %s** " + ToolSet.CP_CALL + "%s"),
    PREFIX_MESSAGE_TEMPLATE("***[%s]* %s** " + ToolSet.CP_CALL + "%s"),
    MESSAGE_TOO_LONG(ToolSet.CP_ERR + "I sent a message too long for Callerphone to handle " + ToolSet.CP_ERR),
    ATTEMPTED_PING(ToolSet.CP_ERR + "I tried to ping everyone " + ToolSet.CP_ERR),
    ATTEMPTED_LINK(ToolSet.CP_ERR + "I tried to send a link " + ToolSet.CP_ERR),
    USER_TEMPLATE("\t\t{\n\t\t\t\"id\": \"%s\",\n\t\t\t\"status\": \"%s\",\n\t\t\t\"reason\": \"%s\",\n\t\t\t\"prefix\": \"%s\",\n\t\t\t\"credits\": %s,\n\t\t\t\"executed\": %s,\n\t\t\t\"transmitted\": %s\n\t\t}"),
    POOL_TEMPLATE("\t\t{\n\t\t\t\"hostID\": \"%s\",\n\t\t\t\"pwd\": \"%s\",\n\t\t\t\"cap\": %s,\n\t\t\t\"pub\": %s,\n\t\t\t\"children\": [\n\t\t\t\t%s\n\t\t\t]\n\t\t}"),
    NO_PERMISSION(ToolSet.CP_EMJ + "You need `Manage Channel` permission to run this command."),

    BLACKLISTED(ToolSet.CP_ERR + "**Blacklisted; Reason:** %s\nSubmit an appeal in our support server " + Callerphone.config.getSupportServer()),


    ERROR_MSG("An error occurred with error: `%s`." +
            "\nIf this is a recurring problem, please join our support server and report this issue. " + Callerphone.config.getSupportServer()),
    TEMP("There is no set return message");
    public final String label;

    Response(String label) {
        this.label = label;
    }

    public String toString() {
        return label;
    }
}