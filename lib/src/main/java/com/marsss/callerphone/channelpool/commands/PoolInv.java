package com.marsss.callerphone.channelpool.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class PoolInv implements ICommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {

    }

    @Override
    public void runSlash(SlashCommandEvent e) {

    }

    public static String getHelp() {
        return "`" + Callerphone.Prefix + "invchan <channel ID>` - Invite channel to pool.";
    }

    @Override
    public String getHelpF() {
        return null;
    }

    @Override
    public String[] getTriggers() {
        return "invitechannel,invchannel,invitechan,invchan".split(",");
    }
}
