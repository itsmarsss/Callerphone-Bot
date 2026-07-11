package com.itsmarsss.callerphone.listeners;

/**
 * @deprecated Replaced by {@link com.itsmarsss.callerphone.discord.mod.ModCommandRouter}.
 * Kept only so old references compile; not registered as a listener.
 */
@Deprecated
public class OnMessageEvent {
    public static String adminHelp() {
        return com.itsmarsss.callerphone.Callerphone.config.getPrefix() + "mod / rmod — see `help mod`";
    }

    public static String blacklistHelp() {
        return "blacklist / rblacklist — see `help mod`";
    }

    public static String supportHelp() {
        return "prefix / rprefix — see `help mod`";
    }

    public static String showItemsHelp() {
        return "Use staff list commands via existing tooling; Match uses mreview/mreports.";
    }
}
