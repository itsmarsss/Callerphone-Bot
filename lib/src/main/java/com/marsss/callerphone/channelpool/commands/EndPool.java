package com.marsss.callerphone.channelpool.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.channelpool.ChannelPool;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class EndPool implements ICommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        e.getMessage().reply(endPool(e.getMember(), e.getChannel().getId())).queue();
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        e.reply(endPool(e.getMember(), e.getChannel().getId())).queue();
    }

    public static String getHelp() {
        return "`" + Callerphone.Prefix + "endpool` - End a channel pool.";
    }

    @Override
    public String getHelpF() {
        return "`" + Callerphone.Prefix + "endpool` - End a channel pool.";
    }

    @Override
    public String[] getTriggers() {
        return "end,endpool,stoppool".split(",");
    }

    private String endPool(Member member, String id) {
        if (!member.hasPermission(Permission.MANAGE_CHANNEL)) {
            return Callerphone.Callerphone + "You need `Manage Channel` permission to run this command.";
        }

        int stat = ChannelPool.endPool(id);
        if (stat == 404) {
            return Callerphone.Callerphone + "This channel is not hosting a pool.";
        } else if (stat == 200) {
            return Callerphone.Callerphone + "Successfully ended channel pool!";
        }
        return Callerphone.Callerphone + "An error occurred.";
    }
}
