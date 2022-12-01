package com.marsss.callerphone.channelpool.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.channelpool.ChannelPool;
import com.marsss.callerphone.listeners.CommandListener;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class PoolCap implements ICommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        String[] args = e.getMessage().getContentRaw().split("\\s+");

        if (args.length == 1) {
            e.getMessage().reply(Callerphone.Callerphone + "Missing parameters, do `" + Callerphone.Prefix + "help poolcap` for more information.").queue();
            return;
        }

        final int cap = Integer.parseInt(args[1]);

        try {
            e.getMessage().reply(poolCap(e.getChannel().getId(), cap)).queue();
        } catch (Exception ex) {
            CommandListener.sendError(e.getMessage(), ex);
        }
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        e.reply(poolCap(e.getChannel().getId(), (int) e.getOption("capacity").getAsLong())).queue();
    }


    public static String getHelp() {
        return "`" + Callerphone.Prefix + "poolcap <number [1-10]>` - Set channel pool capacity.";
    }

    @Override
    public String getHelpF() {
        return "`" + Callerphone.Prefix + "poolcap <number [1-10]>` - Set channel pool capacity.";
    }

    @Override
    public String[] getTriggers() {
        return "cap,capcity,poolcap,poolcapacity".split(",");
    }

    private String poolCap(String id, int cap) {
        int stat = ChannelPool.setCap(id, cap);
        if (stat == ChannelPool.SUCCESS) {
            return Callerphone.Callerphone + "This pool now has capacity **" + ChannelPool.config.get(id).getCap() + "**.";
        } else if (stat == ChannelPool.ERROR) {
            return Callerphone.Callerphone + "This channel is not hosing a pool.";
        }
        return Callerphone.Callerphone + "An error occurred.";
    }
}
