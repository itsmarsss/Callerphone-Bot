package com.marsss.callerphone.channelpool.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.channelpool.ChannelPool;
import com.marsss.callerphone.channelpool.PoolStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class LeavePool implements ICommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        if (ChannelPool.permissionCheck(e.getMember(), e.getMessage())) {
            return;
        }

        e.getMessage().reply(leavePool(e.getChannel().getId())).queue();
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        if (ChannelPool.permissionCheck(e.getMember(), e)) {
            return;
        }

        e.reply(leavePool(e.getChannel().getId())).queue();
    }

    private String leavePool(String id) {
        PoolStatus stat = ChannelPool.leavePool(id);
        if (stat == PoolStatus.ERROR) {
            return Callerphone.Callerphone + "This channel is not in a pool.";
        } else if (stat == PoolStatus.IS_HOST) {
            return Callerphone.Callerphone + "This channel is already hosting a pool." +
                    "\nThis channel's pool ID is: `" + id + "`" +
                    (ChannelPool.hasPassword(id)
                            ? "\nThis channel's password is: ||`" + ChannelPool.getPassword(id) + "`||"
                            : "\nSet a password with: `" + Callerphone.Prefix + "pwdpool <password>`") +
                    "\nEnd pool with: `" + Callerphone.Prefix + "endpool`";
        } else if (stat == PoolStatus.SUCCESS) {
            return Callerphone.Callerphone + "Successfully left channel pool!";
        }
        return Callerphone.Callerphone + "An error occurred.";
    }

    @Override
    public String getHelp() {
        return "`" + Callerphone.Prefix + "leavepool` - Leave a channel pool.";
    }

    @Override
    public String[] getTriggers() {
        return "leave,leavepool,exitpool".split(",");
    }
}
