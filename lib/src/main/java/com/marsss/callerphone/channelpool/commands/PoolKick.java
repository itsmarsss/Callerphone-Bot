package com.marsss.callerphone.channelpool.commands;

import com.marsss.Command;
import com.marsss.callerphone.Callerphone;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class PoolKick implements Command {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        // TODO:
        // Kick channels
    }

    @Override
    public void runSlash(SlashCommandEvent e) {

    }

    public static String getHelp() {
        return "`" + Callerphone.Prefix + "kickchan <channel ID>` - Kick channel from pool.";
    }

    @Override
    public String getHelpF() {
        return "`" + Callerphone.Prefix + "kickchan <channel ID>` - Kick channel from pool.";
    }

    @Override
    public String[] getTriggers() {
        return "kick,kickchannel,kickchan".split(",");
    }
}