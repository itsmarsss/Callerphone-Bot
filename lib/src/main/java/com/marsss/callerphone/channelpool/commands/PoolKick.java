package com.marsss.callerphone.channelpool.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.channelpool.ChannelPool;
import com.marsss.callerphone.channelpool.PoolStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class PoolKick implements ICommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        if (!e.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
            e.getMessage().reply(Callerphone.Callerphone + "You need `Manage Channel` permission to run this command.").queue();
            return;
        }

        String[] args = e.getMessage().getContentRaw().split("\\s+");

        if (args.length == 1) {
            e.getMessage().reply(Callerphone.Callerphone + "Missing parameters, do `" + Callerphone.Prefix + "help kickchan` for more information.").queue();
            return;
        }

        final String id = args[1];

        e.getMessage().reply(poolKick(e.getChannel().getId(), id)).queue();
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        if (!e.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
            e.reply(Callerphone.Callerphone + "You need `Manage Channel` permission to run this command.").setEphemeral(true).queue();
            return;
        }

        e.reply(poolKick(e.getChannel().getId(), e.getOption("target").getAsString())).queue();
    }

    private String poolKick(String IDh, String IDc) {
        PoolStatus stat = ChannelPool.removeChildren(IDh, IDc);
        if (stat == PoolStatus.SUCCESS) {
            Callerphone.jda.getTextChannelById(IDc).sendMessage(Callerphone.Callerphone + "You have been kicked from the pool.").queue();
            return Callerphone.Callerphone + "Successfully kicked `ID: " + IDc + "` (#" + Callerphone.jda.getTextChannelById(IDc).getName() + ") from this pool.";
        } else if (stat == PoolStatus.ERROR) {
            return Callerphone.Callerphone + "Requested pool not found.";
        }
        return Callerphone.Callerphone + "An error occurred.";
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