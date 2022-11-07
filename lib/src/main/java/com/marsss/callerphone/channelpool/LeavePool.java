package com.marsss.callerphone.channelpool;

import com.marsss.Command;
import com.marsss.callerphone.Callerphone;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class LeavePool implements Command {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        e.getMessage().reply(leavePool(e.getMember(),e.getChannel().getId())).queue();
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        e.reply(leavePool(e.getMember(), e.getChannel().getId())).queue();
    }

    public static String getHelp() {
        return "`" + Callerphone.Prefix + "leavepool` - Leave a channel pool.";
    }

    @Override
    public String getHelpF() {
        return "`" + Callerphone.Prefix + "leavepool` - Leave a channel pool.";
    }

    @Override
    public String[] getTriggers() {
        return "leave,leavepool,exitpool".split(",");
    }

    private String leavePool(Member member, String id) {
        if (!member.hasPermission(Permission.MANAGE_CHANNEL)) {
            return Callerphone.Callerphone + "You need `Manage Channel` permission to run this command.";
        }

        int stat = ChannelPool.leavePool(id);
        if (stat == 404) {
            return Callerphone.Callerphone + "This channel is not in a pool.";
        } else if (stat == 409) {
            return Callerphone.Callerphone + "This channel is hosting a pool.";
        } else if (stat == 200) {
            return Callerphone.Callerphone + "Successfully left channel pool!";
        }
        return Callerphone.Callerphone + "An error occurred.";
    }
}
