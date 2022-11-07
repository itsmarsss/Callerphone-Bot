package com.marsss.callerphone.channelpool;

import com.marsss.Command;
import com.marsss.callerphone.Callerphone;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class JoinPool implements Command {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        final String host = e.getMessage().getContentRaw().split("\\s+")[1];
        e.getMessage().reply(joinPool(e.getMember(), e.getChannel(), host)).queue();
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        e.reply(joinPool(e.getMember(), e.getChannel(), e.getOption("hostID").getAsString())).queue();
    }

    public static String getHelp() {
        return "`" + Callerphone.Prefix + "joinpool` - Join a channel pool.";
    }

    @Override
    public String getHelpF() {
        return "`" + Callerphone.Prefix + "joinpool` - Join a channel pool.";
    }

    @Override
    public String[] getTriggers() {
        return "join,joinpool,addpool".split(",");
    }

    private String joinPool(Member member, MessageChannel channel, String host) {
        if (!member.hasPermission(Permission.MANAGE_CHANNEL)) {
            return "You need `Manage Channel` permission to run this command.";
        }

        int stat = ChannelPool.joinPool(host, channel.getId());
        if (stat == 413) {
            return Callerphone.Callerphone + "This channel is hosting a pool." +
                    "\n`This channel's pool ID is: " + channel.getId() + "`" +
                    "\nEnd pool with: `" + Callerphone.Prefix + "endpool`";
        } else if (stat == 414) {
            Callerphone.jda.getTextChannelById(host).sendMessage("Channel ID: " + channel.getId() + " attempted to join a full pool *(this one)*.").queue();
            return Callerphone.Callerphone + "This pool is already full 10/10.";
        } else if (stat == 409) {
            return Callerphone.Callerphone + "This channel is already in a pool." +
                    "\nLeave pool with: `" + Callerphone.Prefix + "leavepool`";
        } else if (stat == 404) {
            return Callerphone.Callerphone + "Requested pool ID *(" + host + ")* does not exist.";
        } else if (stat == 200) {
            return Callerphone.Callerphone + "Successfully joined channel pool hosted by `#" + Callerphone.jda.getTextChannelById(host).getName() + "`*(ID: " + host + ")*!";
        }
        return Callerphone.Callerphone + "An error occurred.";
    }
}
