package com.marsss.callerphone.channelpool.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.channelpool.ChannelPool;
import com.marsss.callerphone.listeners.CommandListener;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class PoolPub implements ICommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {

        if (!e.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
            e.getMessage().reply(Callerphone.Callerphone + "You need `Manage Channel` permission to run this command.").queue();
            return;
        }

        String[] args = e.getMessage().getContentRaw().split("\\s+");

        if (args.length == 1) {
            e.getMessage().reply(Callerphone.Callerphone + "Missing parameters, do `" + Callerphone.Prefix + "help poolpub` for more information.").queue();
            return;
        }

        final boolean pub = Boolean.parseBoolean(args[1]);

        try {
            e.getMessage().reply(poolPub(e.getChannel().getId(), pub)).queue();
        } catch (Exception ex) {
            CommandListener.sendError(e.getMessage(), ex);
        }
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        e.reply(poolPub(e.getChannel().getId(), e.getOption("publicity").getAsBoolean())).queue();
    }

    @Override
    public String getHelpF() {
        return "`" + Callerphone.Prefix + "poolpub <true|false>` - Set channel pool publicity.";
    }

    @Override
    public String[] getTriggers() {
        return "public,publicity,poolpub,poolpublic,poolpublicity".split(",");
    }

    private String poolPub(String id, boolean pub) {
        int stat = ChannelPool.setPublicity(id, pub);
        if (stat == ChannelPool.SUCCESS) {
            return Callerphone.Callerphone + "This pool is now " + (pub ? "public" : "private") + ".";
        } else if (stat == ChannelPool.ERROR) {
            return Callerphone.Callerphone + "This pool is not hosting a pool.";
        }
        return Callerphone.Callerphone + "An error occurred.";
    }
}
