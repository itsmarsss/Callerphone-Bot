package com.marsss.callerphone.channelpool.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.Response;
import com.marsss.callerphone.ToolSet;
import com.marsss.callerphone.channelpool.ChannelPool;
import com.marsss.callerphone.channelpool.PoolResponse;
import com.marsss.callerphone.channelpool.PoolStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class PoolCap implements ICommand {

    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        final Member MEMBER = e.getMember();

        if (ChannelPool.permissionCheck(MEMBER, e.getMessage())) {
            return;
        }

        String[] args = e.getMessage().getContentRaw().split("\\s+");

        if (args.length == 1) {
            e.getMessage().reply(String.format(ToolSet.CP_ERR + Response.MISSING_PARAM.toString(), Callerphone.config.getPrefix())).queue();
            return;
        }

        final int CAP = Integer.parseInt(args[1]);

        e.getMessage().reply(poolCap(e.getChannel().getId(), CAP)).queue();
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        final Member MEMBER = e.getMember();

        if (ChannelPool.permissionCheck(MEMBER, e)) {
            return;
        }

        e.reply(poolCap(e.getChannel().getId(), (int) e.getOption("capacity").getAsLong())).queue();
    }

    private String poolCap(String id, int cap) {
        PoolStatus stat = ChannelPool.setCap(id, cap);

        if (stat == PoolStatus.SUCCESS) {

            return String.format(PoolResponse.POOL_HAS_CAPACITY.toString(), ChannelPool.config.get(id).getCap());

        } else if (stat == PoolStatus.NOT_FOUND) {

            return PoolResponse.NOT_HOSTING.toString();

        }

        return ToolSet.CP_ERR + Response.ERROR.toString();
    }

    @Override
    public String getHelp() {
        return "`" + Callerphone.config.getPrefix() + "poolcap <number [1-10]>` - Set channel pool capacity.";
    }

    @Override
    public String[] getTriggers() {
        return "cap,capcity,poolcap,poolcapacity".split(",");
    }
}
