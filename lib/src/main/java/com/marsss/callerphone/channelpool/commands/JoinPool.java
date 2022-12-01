package com.marsss.callerphone.channelpool.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.channelpool.ChannelPool;
import com.marsss.callerphone.listeners.CommandListener;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class JoinPool implements ICommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        String[] args = e.getMessage().getContentRaw().split("\\s+");

        if (args.length == 1) {
            e.getMessage().reply(Callerphone.Callerphone + "Missing parameters, do `" + Callerphone.Prefix + "help join` for more information.").queue();
            return;
        }

        final String host = e.getMessage().getContentRaw().split("\\s+")[1];

        String pwd = "";

        if (args.length >= 3)
            pwd = args[2];

        try {
            e.getMessage().reply(joinPool(e.getMember(), e.getChannel(), host, pwd)).queue();
        } catch (Exception ex) {
            CommandListener.sendError(e.getMessage(), ex);
        }
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        e.reply(joinPool(e.getMember(), e.getChannel(), e.getOption("hostID").getAsString(), e.getOption("password").getAsString())).queue();
    }

    public static String getHelp() {
        return "`" + Callerphone.Prefix + "joinpool <ID> <password>` - Join a channel pool.";
    }

    @Override
    public String getHelpF() {
        return "`" + Callerphone.Prefix + "joinpool <ID> <password>` - Join a channel pool.";
    }

    @Override
    public String[] getTriggers() {
        return "join,joinpool,addpool".split(",");
    }

    private String joinPool(Member member, MessageChannel channel, String host, String pwd) {
        if (!member.hasPermission(Permission.MANAGE_CHANNEL)) {
            return Callerphone.Callerphone + "You need `Manage Channel` permission to run this command.";
        }

        int stat = ChannelPool.joinPool(host, channel.getId(), pwd);
        if (stat == ChannelPool.IS_HOST) {
            if (ChannelPool.hasPassword(channel.getId())) {
                return Callerphone.Callerphone + "This channel is already hosting a pool." +
                        "\nThis channel's pool ID is: `" + channel.getId() + "`" +
                        "\nThis channel's password is: ||`" + ChannelPool.getPassword(channel.getId()) + "`||" +
                        "\nEnd pool with: `" + Callerphone.Prefix + "endpool`";
            } else {
                return Callerphone.Callerphone + "This channel is already hosting a pool." +
                        "\nThis channel's pool ID is: `" + channel.getId() + "`" +
                        "\nSet a password with: `" + Callerphone.Prefix + "pwdpool <password>`" +
                        "\nEnd pool with: `" + Callerphone.Prefix + "endpool`";
            }
        } else if (stat == ChannelPool.IS_CHILD) {
            return Callerphone.Callerphone + "This channel is already in a pool." +
                    "\nLeave pool with: `" + Callerphone.Prefix + "leavepool`";
        } else if (stat == ChannelPool.NOT_FOUND) {
            return Callerphone.Callerphone + "Requested pool `ID: " + host + "` does not exist.";
        } else if (stat == ChannelPool.INCORRECT_PASS) {
            Callerphone.jda.getTextChannelById(host).sendMessage(Callerphone.Callerphone + "Channel ID: `" + channel.getId() + "` attempted to join with incorrect password.").queue();
            return Callerphone.Callerphone + "Requested pool `ID: " + host + "` does not exist.";
        } else if (stat == ChannelPool.FULL) {
            Callerphone.jda.getTextChannelById(host).sendMessage(Callerphone.Callerphone + "Channel ID: `" + channel.getId() + "` attempted to join this full pool.").queue();
            return Callerphone.Callerphone + "This pool is already full " + ChannelPool.config.get(host).getCap() + "/" + ChannelPool.config.get(host).getCap() + ".";
        } else if (stat == ChannelPool.SUCCESS) {
            return Callerphone.Callerphone + "Successfully joined channel pool hosted by `#" + Callerphone.jda.getTextChannelById(host).getName() + "`*(ID: " + host + ")*!";
        }
        return Callerphone.Callerphone + "An error occurred.";
    }
}
