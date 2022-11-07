package com.marsss.callerphone.channelpool;

import com.marsss.Command;
import com.marsss.callerphone.Callerphone;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class HostPool implements Command {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        e.getMessage().reply(hostPool(e.getMember(), e.getChannel())).queue();
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        e.reply(hostPool(e.getMember(), e.getChannel())).queue();
    }

    public static String getHelp() {
        return "`" + Callerphone.Prefix + "hostpool` - Host a channel pool.";
    }

    @Override
    public String getHelpF() {
        return "`" + Callerphone.Prefix + "hostpool` - Host a channel pool.";
    }

    @Override
    public String[] getTriggers() {
        return "host,hostpool,startpool".split(",");
    }

    private String hostPool(Member member, MessageChannel channel) {
        if (!member.hasPermission(Permission.MANAGE_CHANNEL)) {
            return Callerphone.Callerphone + "You need `Manage Channel` permission to run this command.";
        }

        int stat = ChannelPool.hostPool(channel.getId());
        if (stat == 413) {
            return Callerphone.Callerphone + "This channel is already hosting a pool." +
                    "\nThis channel's pool ID is: `" + channel.getId() + "`" +
                    "\nSet a password with: `" + Callerphone.Prefix + "pwdpool <password>`";
        } else if (stat == 409) {
            return Callerphone.Callerphone + "This channel is already in a pool.";
        } else if (stat == 201) {
            return Callerphone.Callerphone + "Successfully hosted channel pool for " + channel.getName() + "!" +
                    "\nThis channel's pool ID is: `" + channel.getId() + "`" +
                    "\nSet a password with: `" + Callerphone.Prefix + "poolpass <password>`";
        }
        return Callerphone.Callerphone + "An error occurred.";
    }
}
