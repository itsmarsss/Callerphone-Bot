package com.marsss.callerphone.channelpool.commands;

import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.Response;
import com.marsss.callerphone.ToolSet;
import com.marsss.callerphone.channelpool.ChannelPool;
import com.marsss.callerphone.channelpool.PoolResponse;
import com.marsss.callerphone.channelpool.PoolStatus;
import com.marsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class PoolKick implements ISlashCommand {

    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        final Member MEMBER = e.getMember();

        if (ChannelPool.permissionCheck(MEMBER, e)) {
            return;
        }

        e.reply(poolKick(e.getChannel().getId(), e.getOption("target").getAsString())).queue();
    }

    private String poolKick(String requestID, String kickID) {
        PoolStatus stat = ChannelPool.removeChild(requestID, kickID);

        if (stat == PoolStatus.IS_CHILD) {

            return PoolResponse.NOT_HOSTING.toString();

        } else if (stat == PoolStatus.SUCCESS) {

            final TextChannel CHILD_CHANNEL = ToolSet.getTextChannel(kickID);
            if (CHILD_CHANNEL != null) {
                CHILD_CHANNEL.sendMessage(PoolResponse.KICKED_FROM_POOL.toString()).queue();
            }
            return String.format(PoolResponse.KICK_POOL_SUCCESS.toString(), kickID);

        } else if (stat == PoolStatus.NOT_FOUND) {

            return PoolResponse.REQUESTED_NOT_FOUND.toString();

        }

        return Response.ERROR.toString();
    }

    @Override
    public String getHelp() {
        return "`" + Callerphone.config.getPrefix() + "kickchan <channel ID>` - Kick channel from pool.";
    }

    @Override
    public String[] getTriggers() {
        return "poolkick,kick,kickchannel,kickchan".split(",");
    }
}