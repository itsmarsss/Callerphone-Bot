package com.marsss.callerphone.channelpool.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.channelpool.ChannelPool;
import com.marsss.callerphone.channelpool.PoolStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class HostPool implements ICommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        if (ChannelPool.permissionCheck(e.getMember(), e.getMessage())) {
            return;
        }

        e.getMessage().reply(hostPool(e.getChannel())).queue();
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        if (ChannelPool.permissionCheck(e.getMember(), e)) {
            return;
        }

        e.reply(hostPool(e.getChannel())).queue();
    }

    private String hostPool(MessageChannel channel) {
        PoolStatus stat = ChannelPool.hostPool(channel.getId());
        if (stat == PoolStatus.IS_HOST) {
            return Callerphone.Callerphone + "This channel is already hosting a pool." +
                    "\nThis channel's pool ID is: `" + channel.getId() + "`" +
                    (ChannelPool.hasPassword(channel.getId())
                            ? "\nThis channel's password is: ||`" + ChannelPool.getPassword(channel.getId()) + "`||"
                            : "\nSet a password with: `" + Callerphone.Prefix + "pwdpool <password>`") +
                    "\nEnd pool with: `" + Callerphone.Prefix + "endpool`";
        } else if (stat == PoolStatus.IS_CHILD) {
            return Callerphone.Callerphone + "This channel is already in a pool." +
                    "\nLeave pool with: `" + Callerphone.Prefix + "leavepool`";
        } else if (stat == PoolStatus.SUCCESS) {
            return Callerphone.Callerphone + "Successfully hosted channel pool for `#" + channel.getName() + "`!" +
                    "\nThis channel's pool ID is: `" + channel.getId() + "`" +
                    "\nSet a password with: `" + Callerphone.Prefix + "poolpass <password>`" +
                    "\nEnd pool with: `" + Callerphone.Prefix + "endpool`";
        }
        return Callerphone.Callerphone + "An error occurred.";
    }

    @Override
    public String getHelp() {
        return "`" + Callerphone.Prefix + "hostpool` - Host a channel pool.";
    }

    @Override
    public String[] getTriggers() {
        return "host,hostpool,startpool".split(",");
    }
}
