package com.itsmarsss.callerphone;

public enum Response {
    ERROR("We couldn't finish that. Nothing was lost — try again."),
    MISSING_PARAM("Something's missing. Try `/help` for that command."),

    PING_TEMPLATE("**Rest ping:** %sms \n**WS ping:** %sms"),

    PROFILE_GENERAL("Level `%d`\nExp `%d/100`\nPrefix %s"),
    PROFILE_CREDITS("Balance `◉ %d`"),
    PROFILE_MESSAGE("Commands `%d`\nCall messages `%d`\nTotal `%d`"),

    CONNECTION_ERROR("Connection lost. Call ended."),
    DEFAULT_MESSAGE_TEMPLATE("**%s** · %s"),
    MODERATOR_MESSAGE_TEMPLATE("**[Mod] %s** · %s"),
    PREFIX_MESSAGE_TEMPLATE("**[%s] %s** · %s"),
    MESSAGE_TOO_LONG("That message was too long to send. Try a shorter one."),
    ATTEMPTED_PING("Pings aren't allowed in calls. Remove the mention and resend."),
    ATTEMPTED_LINK("Links aren't allowed in calls. Remove the URL and resend."),

    USER_TEMPLATE("\t\t{\n\t\t\t\"id\": \"%s\",\n\t\t\t\"status\": \"%s\",\n\t\t\t\"reason\": \"%s\",\n\t\t\t\"prefix\": \"%s\",\n\t\t\t\"credits\": %s,\n\t\t\t\"executed\": %s,\n\t\t\t\"transmitted\": %s\n\t\t}"),

    NO_PERMISSION("You need Manage Channel permission for that."),
    /** Args: reason, support server */
    BLACKLISTED("**Blacklisted.** Reason: %s\nAppeal in the support server: %s"),
    /** Args: error, support server */
    ERROR_MSG("We hit a problem (`%s`). If it keeps happening, join support: %s"),
    TEMP("There is no set return message"),
    FEATURE_COMING_SOON("This game isn't ready yet. Try Tic-Tac-Toe or keep chatting.");

    public final String label;

    Response(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

    public String format(Object... args) {
        return String.format(label, args);
    }
}
