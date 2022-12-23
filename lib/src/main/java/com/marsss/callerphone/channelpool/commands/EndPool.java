package com.marsss.callerphone.channelpool.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.channelpool.ChannelPool;
import com.marsss.callerphone.channelpool.PoolStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class EndPool implements ICommand {

    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        final Member MEMBER = e.getMember();
        if (MEMBER == null) {
            return;
        }

        if (ChannelPool.permissionCheck(MEMBER, e.getMessage())) {
            return;
        }

        e.getMessage().reply(endPool(e.getChannel().getId())).queue();
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

        e.reply(endPool(e.getChannel().getId())).queue();
    }

    private final String CP_EMJ = Callerphone.Callerphone;

    private String endPool(String id) {
        PoolStatus stat = ChannelPool.endPool(id);
        if (stat == PoolStatus.SUCCESS) {
            return CP_EMJ + "Successfully ended channel pool!";
        } else if (stat == PoolStatus.IS_CHILD) {
            return CP_EMJ + "This channel is not hosting a pool.";
        }
        return CP_EMJ + "An error occurred.";
    }

    @Override
    public String getHelp() {
        return "`" + Callerphone.Prefix + "endpool` - End a channel pool.";
    }

    @Override
    public String[] getTriggers() {
        return "end,endpool,stoppool".split(",");
    }
}
