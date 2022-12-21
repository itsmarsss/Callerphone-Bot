package com.marsss.callerphone.channelpool.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.channelpool.ChannelPool;
import com.marsss.callerphone.channelpool.PoolStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class PoolCap implements ICommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        if (ChannelPool.permissionCheck(e.getMember(), e.getMessage())) {
            return;
        }

        String[] args = e.getMessage().getContentRaw().split("\\s+");

        if (args.length == 1) {
            e.getMessage().reply(Callerphone.Callerphone + "Missing parameters, do `" + Callerphone.Prefix + "help poolcap` for more information.").queue();
            return;
        }

        final int cap = Integer.parseInt(args[1]);

        e.getMessage().reply(poolCap(e.getChannel().getId(), cap)).queue();
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        if (ChannelPool.permissionCheck(e.getMember(), e)) {
            return;
        }

        e.reply(poolCap(e.getChannel().getId(), (int) e.getOption("capacity").getAsLong())).queue();
    }

    private String poolCap(String id, int cap) {
        PoolStatus stat = ChannelPool.setCap(id, cap);
        if (stat == PoolStatus.SUCCESS) {
            return Callerphone.Callerphone + "This pool now has capacity **" + ChannelPool.config.get(id).getCap() + "**.";
        } else if (stat == PoolStatus.ERROR) {
            return Callerphone.Callerphone + "This channel is not hosing a pool.";
        }
        return Callerphone.Callerphone + "An error occurred.";
    }

    @Override
    public String getHelp() {
        return "`" + Callerphone.Prefix + "poolcap <number [1-10]>` - Set channel pool capacity.";
    }

    @Override
    public String[] getTriggers() {
        return "cap,capcity,poolcap,poolcapacity".split(",");
    }
}
