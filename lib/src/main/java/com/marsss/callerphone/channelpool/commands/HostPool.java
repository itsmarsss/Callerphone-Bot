package com.marsss.callerphone.channelpool.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.channelpool.ChannelPool;
import com.marsss.callerphone.channelpool.PoolStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class HostPool implements ICommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        final Member MEMBER = e.getMember();
        if (MEMBER == null) {
            return;
        }
        if (ChannelPool.permissionCheck(MEMBER, e.getMessage())) {
            return;
        }

        e.getMessage().reply(hostPool(e.getChannel())).queue();
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        final Member MEMBER = e.getMember();
        if (MEMBER == null) {
            return;
        }

        if (ChannelPool.permissionCheck(MEMBER, e)) {
            return;
        }

        e.reply(hostPool(e.getChannel())).queue();
    }

    private final String CP_EMJ = Callerphone.Callerphone;

    private String hostPool(MessageChannel channel) {
        PoolStatus stat = ChannelPool.hostPool(channel.getId());
        if (stat == PoolStatus.IS_HOST) {
            return CP_EMJ + "This channel is already hosting a pool." +
                    "\nThis channel's pool ID is: `" + channel.getId() + "`" +
                    (ChannelPool.hasPassword(channel.getId())
                            ? "\nThis channel's password is: ||`" + ChannelPool.getPassword(channel.getId()) + "`||"
                            : "\nSet a password with: `" + Callerphone.Prefix + "pwdpool <password>`") +
                    "\nEnd pool with: `" + Callerphone.Prefix + "endpool`";
        } else if (stat == PoolStatus.IS_CHILD) {
            return CP_EMJ + "This channel is already in a pool." +
                    "\nLeave pool with: `" + Callerphone.Prefix + "leavepool`";
        } else if (stat == PoolStatus.SUCCESS) {
            return CP_EMJ + "Successfully hosted channel pool for `#" + channel.getName() + "`!" +
                    "\nThis channel's pool ID is: `" + channel.getId() + "`" +
                    "\nSet a password with: `" + Callerphone.Prefix + "poolpass <password>`" +
                    "\nEnd pool with: `" + Callerphone.Prefix + "endpool`";
        }
        return CP_EMJ + "An error occurred.";
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
