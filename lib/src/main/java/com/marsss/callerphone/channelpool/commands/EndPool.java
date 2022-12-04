package com.marsss.callerphone.channelpool.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.channelpool.ChannelPool;
import com.marsss.callerphone.listeners.CommandListener;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class EndPool implements ICommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {

        if (!e.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
            e.getMessage().reply(Callerphone.Callerphone + "You need `Manage Channel` permission to run this command.").queue();
            return;
        }

        try {
            e.getMessage().reply(endPool(e.getChannel().getId())).queue();
        } catch (Exception ex) {
            CommandListener.sendError(e.getMessage(), ex);
        }
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        e.reply(endPool(e.getChannel().getId())).queue();
    }

    @Override
    public String getHelp() {
        return "`" + Callerphone.Prefix + "endpool` - End a channel pool.";
    }

    @Override
    public String[] getTriggers() {
        return "end,endpool,stoppool".split(",");
    }

    private String endPool(String id) {
        int stat = ChannelPool.endPool(id);
        if (stat == ChannelPool.SUCCESS) {
            return Callerphone.Callerphone + "Successfully ended channel pool!";
        } else if (stat == ChannelPool.IS_CHILD) {
            return Callerphone.Callerphone + "This channel is not hosting a pool.";
        }
        return Callerphone.Callerphone + "An error occurred.";
    }
}
