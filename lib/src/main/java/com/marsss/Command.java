package com.marsss;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public interface Command {
    void runCommand(GuildMessageReceivedEvent e);
    void runSlash(SlashCommandEvent event);

    static String getHelp() {
        return "Command Help Message Here";
    }
    String getHelpF();

    String[] getTriggers();
}
