package com.marsss.callerphone.channelpool.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.channelpool.ChannelPool;
import com.marsss.callerphone.channelpool.PoolStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class PoolCap implements ICommand {

    private final String CP_EMJ = Callerphone.Callerphone;

    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        final Member MEMBER = e.getMember();
        if (MEMBER == null) {
            return;
        }

        if (ChannelPool.permissionCheck(MEMBER, e.getMessage())) {
            return;
        }

        String[] args = e.getMessage().getContentRaw().split("\\s+");

        if (args.length == 1) {
            e.getMessage().reply(CP_EMJ + "Missing parameters, do `" + Callerphone.Prefix + "help poolcap` for more information.").queue();
            return;
        }

        final int CAP = Integer.parseInt(args[1]);

        e.getMessage().reply(poolCap(e.getChannel().getId(), CAP)).queue();
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

        e.reply(poolCap(e.getChannel().getId(), (int) e.getOption("capacity").getAsLong())).queue();
    }

    private String poolCap(String id, int cap) {
        PoolStatus stat = ChannelPool.setCap(id, cap);
        if (stat == PoolStatus.SUCCESS) {
            return CP_EMJ + "This pool now has capacity **" + ChannelPool.config.get(id).getCap() + "**.";
        } else if (stat == PoolStatus.ERROR) {
            return CP_EMJ + "This channel is not hosing a pool.";
        }
        return CP_EMJ + "An error occurred.";
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
