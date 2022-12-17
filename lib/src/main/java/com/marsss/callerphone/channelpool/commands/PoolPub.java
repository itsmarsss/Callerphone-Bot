package com.marsss.callerphone.channelpool.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.channelpool.ChannelPool;
import com.marsss.callerphone.channelpool.PoolStatus;
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

        e.getMessage().reply(poolPub(e.getChannel().getId(), pub)).queue();
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        if (!e.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
            e.reply(Callerphone.Callerphone + "You need `Manage Channel` permission to run this command.").setEphemeral(true).queue();
            return;
        }

        e.reply(poolPub(e.getChannel().getId(), e.getOption("public").getAsBoolean())).queue();
    }

    private String poolPub(String id, boolean pub) {
        PoolStatus stat = ChannelPool.setPublicity(id, pub);
        if (stat == PoolStatus.SUCCESS) {
            return Callerphone.Callerphone + "This pool is now **" + (pub ? "public" : "private") + "**.";
        } else if (stat == PoolStatus.ERROR) {
            return Callerphone.Callerphone + "This pool is not hosting a pool.";
        }
        return Callerphone.Callerphone + "An error occurred.";
    }

    @Override
    public String getHelp() {
        return "`" + Callerphone.Prefix + "poolpub <true|false>` - Set channel pool publicity.";
    }

    @Override
    public String[] getTriggers() {
        return "public,publicity,poolpub,poolpublic,poolpublicity".split(",");
    }
}
