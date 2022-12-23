package com.marsss.callerphone.channelpool.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.channelpool.ChannelPool;
import com.marsss.callerphone.channelpool.PoolStatus;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class PoolKick implements ICommand {

    private final String CP_EMJ = Callerphone.Callerphone;

    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        if (ChannelPool.permissionCheck(e.getMember(), e.getMessage())) {
            return;
        }

        String[] args = e.getMessage().getContentRaw().split("\\s+");

        if (args.length == 1) {
            e.getMessage().reply(CP_EMJ + "Missing parameters, do `" + Callerphone.Prefix + "help kickchan` for more information.").queue();
            return;
        }

        final String CHANNELID = args[1];

        e.getMessage().reply(poolKick(e.getChannel().getId(), CHANNELID)).queue();
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        if (ChannelPool.permissionCheck(e.getMember(), e)) {
            return;
        }

        e.reply(poolKick(e.getChannel().getId(), e.getOption("target").getAsString())).queue();
    }

    private String poolKick(String IDh, String IDc) {
        PoolStatus stat = ChannelPool.removeChildren(IDh, IDc);
        if (stat == PoolStatus.SUCCESS) {
            final TextChannel CHILD_CHANNEL = Callerphone.getTextChannel(IDc);
            if(CHILD_CHANNEL != null){
                CHILD_CHANNEL.sendMessage(CP_EMJ + "You have been kicked from the pool.").queue();
            }
            return CP_EMJ + "Successfully kicked `ID: " + IDc + " from this pool.";
        } else if (stat == PoolStatus.ERROR) {
            return CP_EMJ + "Requested pool not found.";
        }
        return CP_EMJ + "An error occurred.";
    }

    @Override
    public String getHelp() {
        return "`" + Callerphone.Prefix + "kickchan <channel ID>` - Kick channel from pool.";
    }

    @Override
    public String[] getTriggers() {
        return "poolkick,kick,kickchannel,kickchan".split(",");
    }
}