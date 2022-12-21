package com.marsss.callerphone.channelpool.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.channelpool.ChannelPool;
import com.marsss.callerphone.channelpool.PoolStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.List;

public class JoinPool implements ICommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        if (ChannelPool.permissionCheck(e.getMember(), e.getMessage())) {
            return;
        }

        String[] args = e.getMessage().getContentRaw().split("\\s+");

        if (args.length == 1) {
            e.getMessage().reply(Callerphone.Callerphone + "Missing parameters, do `" + Callerphone.Prefix + "help join` for more information.").queue();
            return;
        }

        final String host = e.getMessage().getContentRaw().split("\\s+")[1];

        String pwd = "";

        if (args.length >= 3)
            pwd = args[2];

        e.getMessage().reply(joinPool(e.getChannel(), host, pwd)).queue();
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        if (ChannelPool.permissionCheck(e.getMember(), e)) {
            return;
        }
        List<OptionMapping> param = e.getOptions();
        if(param.size() == 1) {
            e.reply(joinPool(e.getChannel(), e.getOption("hostid").getAsString(), "")).queue();
            return;
        }
        e.reply(joinPool(e.getChannel(), e.getOption("hostid").getAsString(), e.getOption("password").getAsString())).queue();
    }

    private String joinPool(MessageChannel channel, String host, String pwd) {
        PoolStatus stat = ChannelPool.joinPool(host, channel.getId(), pwd);
        if (stat == PoolStatus.IS_HOST) {
            return Callerphone.Callerphone + "This channel is already hosting a pool." +
                    "\nThis channel's pool ID is: `" + channel.getId() + "`" +
                    (ChannelPool.hasPassword(channel.getId())
                            ? "\nThis channel's password is: ||`" + ChannelPool.getPassword(channel.getId()) + "`||"
                            : "\nSet a password with: `" + Callerphone.Prefix + "pwdpool <password>`") +
                    "\nEnd pool with: `" + Callerphone.Prefix + "endpool`";
        } else if (stat == PoolStatus.IS_CHILD) {
            return Callerphone.Callerphone + "This channel is already in a pool." +
                    "\nLeave pool with: `" + Callerphone.Prefix + "leavepool`";
        } else if (stat == PoolStatus.NOT_FOUND) {
            return Callerphone.Callerphone + "Requested pool `ID: " + host + "` does not exist.";
        } else if (stat == PoolStatus.INCORRECT_PASS) {
            Callerphone.jda.getTextChannelById(host).sendMessage(Callerphone.Callerphone + "Channel `ID: " + channel.getId() + "` attempted to join with incorrect password.").queue();
            return Callerphone.Callerphone + "Requested pool `ID: " + host + "` does not exist.";
        } else if (stat == PoolStatus.FULL) {
            Callerphone.jda.getTextChannelById(host).sendMessage(Callerphone.Callerphone + "Channel `ID: " + channel.getId() + "` attempted to join this full pool.").queue();
            return Callerphone.Callerphone + "This pool is already full " + ChannelPool.config.get(host).getCap() + "/" + ChannelPool.config.get(host).getCap() + ".";
        } else if (stat == PoolStatus.SUCCESS) {
            return Callerphone.Callerphone + "Successfully joined channel pool hosted by `#" + Callerphone.jda.getTextChannelById(host).getName() + "`*(ID: " + host + ")*!";
        }
        return Callerphone.Callerphone + "An error occurred.";
    }

    @Override
    public String getHelp() {
        return "`" + Callerphone.Prefix + "joinpool <ID> <password>` - Join a channel pool.";
    }

    @Override
    public String[] getTriggers() {
        return "join,joinpool,addpool".split(",");
    }
}
