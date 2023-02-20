package com.marsss.callerphone.channelpool.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.Response;
import com.marsss.callerphone.ToolSet;
import com.marsss.callerphone.channelpool.ChannelPool;
import com.marsss.callerphone.channelpool.PoolResponse;
import com.marsss.callerphone.channelpool.PoolStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class HostPool implements ICommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        final Member MEMBER = e.getMember();

        if (ChannelPool.permissionCheck(MEMBER, e.getMessage())) {
            return;
        }

        e.getMessage().reply(hostPool(e.getChannel())).queue();
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        final Member MEMBER = e.getMember();

        if (ChannelPool.permissionCheck(MEMBER, e)) {
            return;
        }

        e.reply(hostPool(e.getChannel())).queue();
    }

    private String hostPool(MessageChannel channel) {
        PoolStatus stat = ChannelPool.hostPool(channel.getId());

        if (stat == PoolStatus.IS_HOST) {

            return PoolResponse.ALREADY_HOSTING + "\n" +
                    String.format(PoolResponse.POOL_ID.toString(), channel.getId()) + "\n" +
                    (ChannelPool.hasPassword(channel.getId())
                            ? String.format(PoolResponse.POOL_PWD.toString(), ChannelPool.getPassword(channel.getId()))
                            : PoolResponse.POOL_SET_PWD) + "\n" +
                    PoolResponse.POOL_END_WITH;

        } else if (stat == PoolStatus.IS_CHILD) {

            return PoolResponse.ALREADY_IN_POOL + "\n" +
                    PoolResponse.POOL_LEAVE_WITH;

        } else if (stat == PoolStatus.SUCCESS) {

            return String.format(PoolResponse.HOST_POOL_SUCCESS.toString(), channel.getName()) + "\n" +
                    String.format(PoolResponse.POOL_ID.toString(), channel.getId()) + "\n" +
                    PoolResponse.POOL_SET_PWD + "\n" +
                    PoolResponse.POOL_END_WITH;

        }

        return ToolSet.CP_ERR + Response.ERROR.toString();
    }

    @Override
    public String getHelp() {
        return "`" + Callerphone.config.getPrefix() + "hostpool` - Host a channel pool.";
    }

    @Override
    public String[] getTriggers() {
        return "host,hostpool,startpool".split(",");
    }
}
