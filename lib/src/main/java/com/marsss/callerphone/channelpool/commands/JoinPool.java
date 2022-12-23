package com.marsss.callerphone.channelpool.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.channelpool.ChannelPool;
import com.marsss.callerphone.channelpool.PoolStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.List;

public class JoinPool implements ICommand {

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
            e.getMessage().reply(CP_EMJ + "Missing parameters, do `" + Callerphone.Prefix + "help join` for more information.").queue();
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
        final Member MEMBER = e.getMember();
        if (MEMBER == null) {
            return;
        }

        if (ChannelPool.permissionCheck(MEMBER, e)) {
            return;
        }
        List<OptionMapping> param = e.getOptions();
        if (param.size() == 1) {
            e.reply(joinPool(e.getChannel(), e.getOption("hostid").getAsString(), "")).queue();
            return;
        }
        e.reply(joinPool(e.getChannel(), e.getOption("hostid").getAsString(), e.getOption("password").getAsString())).queue();
    }

    private String joinPool(MessageChannel channel, String host, String pwd) {
        PoolStatus stat = ChannelPool.joinPool(host, channel.getId(), pwd);
        final TextChannel HOST_CHANNEL = Callerphone.getTextChannel(host);
        if (HOST_CHANNEL == null) {
            ChannelPool.clearChildren(host);
            return CP_EMJ + "An error occured.";
        }
        if (stat == PoolStatus.IS_HOST) {
            return CP_EMJ + "This channel is already hosting a pool." +
                    "\nThis channel's pool ID is: `" + channel.getId() + "`" +
                    (ChannelPool.hasPassword(channel.getId())
                            ? "\nThis channel's password is: ||`" + ChannelPool.getPassword(channel.getId()) + "`||"
                            : "\nSet a password with: `" + Callerphone.Prefix + "pwdpool <password>`") +
                    "\nEnd pool with: `" + Callerphone.Prefix + "endpool`";
        } else if (stat == PoolStatus.IS_CHILD) {
            return CP_EMJ + "This channel is already in a pool." +
                    "\nLeave pool with: `" + Callerphone.Prefix + "leavepool`";
        } else if (stat == PoolStatus.NOT_FOUND) {
            return CP_EMJ + "Requested pool `ID: " + host + "` does not exist.";
        } else if (stat == PoolStatus.INCORRECT_PASS) {
            HOST_CHANNEL.sendMessage(CP_EMJ + "Channel `ID: " + channel.getId() + "` attempted to join with incorrect password.").queue();
            return CP_EMJ + "Requested pool `ID: " + host + "` does not exist.";
        } else if (stat == PoolStatus.FULL) {
            HOST_CHANNEL.sendMessage(CP_EMJ + "Channel `ID: " + channel.getId() + "` attempted to join this full pool.").queue();
            return CP_EMJ + "This pool is already full " + ChannelPool.config.get(host).getCap() + "/" + ChannelPool.config.get(host).getCap() + ".";
        } else if (stat == PoolStatus.SUCCESS) {
            return CP_EMJ + "Successfully joined channel pool hosted by `ID: " + host + "(#" + HOST_CHANNEL.getName() + ")!";
        }
        return CP_EMJ + "An error occurred.";
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
