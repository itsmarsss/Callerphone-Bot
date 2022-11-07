package com.marsss.callerphone.channelpool.commands;

import com.marsss.Command;
import com.marsss.callerphone.Callerphone;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class PoolTransOwner implements Command {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        // TODO:
        // Transfer ownership
    }

    @Override
    public void runSlash(SlashCommandEvent e) {

    }

    public static String getHelp() {
        return "`" + Callerphone.Prefix + "transferhost <channel ID>` - Transfer host permission to another pool.";
    }

    @Override
    public String getHelpF() {
        return "`" + Callerphone.Prefix + "transferhost <channel ID>` - Transfer host permission to another pool.";
    }

    @Override
    public String[] getTriggers() {
        return "trans,transhost,transferhost".split(",");
    }
}
