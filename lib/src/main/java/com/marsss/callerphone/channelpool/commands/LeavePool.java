package com.marsss.callerphone.channelpool.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.channelpool.ChannelPool;
import com.marsss.callerphone.listeners.CommandListener;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class LeavePool implements ICommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {

        if (!e.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
            e.getMessage().reply(Callerphone.Callerphone + "You need `Manage Channel` permission to run this command.").queue();
            return;
        }

        try {
            e.getMessage().reply(leavePool(e.getChannel().getId())).queue();
        } catch (Exception ex) {
            CommandListener.sendError(e.getMessage(), ex);
        }
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        e.reply(leavePool(e.getChannel().getId())).queue();
    }

    @Override
    public String getHelpF() {
        return "`" + Callerphone.Prefix + "leavepool` - Leave a channel pool.";
    }

    @Override
    public String[] getTriggers() {
        return "leave,leavepool,exitpool".split(",");
    }

    private String leavePool(String id) {
        int stat = ChannelPool.leavePool(id);
        if (stat == ChannelPool.ERROR) {
            return Callerphone.Callerphone + "This channel is not in a pool.";
        } else if (stat == ChannelPool.IS_HOST) {
            if (ChannelPool.hasPassword(id)) {
                return Callerphone.Callerphone + "This channel is already hosting a pool." +
                        "\nThis channel's pool ID is: `" + id + "`" +
                        "\nThis channel's password is: ||`" + ChannelPool.getPassword(id) + "`||" +
                        "\nEnd pool with: `" + Callerphone.Prefix + "endpool`";
            } else {
                return Callerphone.Callerphone + "This channel is already hosting a pool." +
                        "\nThis channel's pool ID is: `" + id + "`" +
                        "\nSet a password with: `" + Callerphone.Prefix + "pwdpool <password>`" +
                        "\nEnd pool with: `" + Callerphone.Prefix + "endpool`";
            }
        } else if (stat == ChannelPool.SUCCESS) {
            return Callerphone.Callerphone + "Successfully left channel pool!";
        }
        return Callerphone.Callerphone + "An error occurred.";
    }
}
