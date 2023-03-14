package com.marsss.callerphone.channelpool.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.Response;
import com.marsss.callerphone.channelpool.ChannelPool;
import com.marsss.callerphone.channelpool.PoolResponse;
import com.marsss.callerphone.channelpool.PoolStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.guild.MessageReceivedEvent;

public class EndPool implements ICommand {

    @Override
    public void runCommand(MessageReceivedEvent e) {
        final Member MEMBER = e.getMember();

        if (ChannelPool.permissionCheck(MEMBER, e.getMessage())) {
            return;
        }

        e.getMessage().reply(endPool(e.getChannel().getId())).queue();
    }

    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        final Member MEMBER = e.getMember();

        if (ChannelPool.permissionCheck(MEMBER, e)) {
            return;
        }

        e.reply(endPool(e.getChannel().getId())).queue();
    }

    private String endPool(String id) {
        PoolStatus stat = ChannelPool.endPool(id);

        if (stat == PoolStatus.SUCCESS) {

            return PoolResponse.END_POOL_SUCCESS.toString();

        } else if (stat == PoolStatus.IS_CHILD) {

            return PoolResponse.NOT_HOSTING.toString();

        } else if (stat == PoolStatus.NOT_FOUND) {

            return PoolResponse.NOT_IN_POOL.toString();

        }

        return Response.ERROR.toString();
    }

    @Override
    public String getHelp() {
        return "`" + Callerphone.config.getPrefix() + "endpool` - End a channel pool.";
    }

    @Override
    public String[] getTriggers() {
        return "end,endpool,stoppool".split(",");
    }
}
