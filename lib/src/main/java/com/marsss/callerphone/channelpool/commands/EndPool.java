package com.marsss.callerphone.channelpool.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.channelpool.ChannelPool;
import com.marsss.callerphone.channelpool.PoolStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.nio.channels.Channel;

public class EndPool implements ICommand {

    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        if (ChannelPool.permissionCheck(e.getMember(), e.getMessage())) {
            return;
        }

        e.getMessage().reply(endPool(e.getChannel().getId())).queue();
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        if (ChannelPool.permissionCheck(e.getMember(), e)) {
            return;
        }

        e.reply(endPool(e.getChannel().getId())).queue();
    }

    private String endPool(String id) {
        PoolStatus stat = ChannelPool.endPool(id);
        if (stat == PoolStatus.SUCCESS) {
            return Callerphone.Callerphone + "Successfully ended channel pool!";
        } else if (stat == PoolStatus.IS_CHILD) {
            return Callerphone.Callerphone + "This channel is not hosting a pool.";
        }
        return Callerphone.Callerphone + "An error occurred.";
    }

    @Override
    public String getHelp() {
        return "`" + Callerphone.Prefix + "endpool` - End a channel pool.";
    }

    @Override
    public String[] getTriggers() {
        return "end,endpool,stoppool".split(",");
    }
}
