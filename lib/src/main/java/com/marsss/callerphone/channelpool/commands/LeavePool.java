package com.marsss.callerphone.channelpool.commands;

import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.Response;
import com.marsss.callerphone.channelpool.ChannelPool;
import com.marsss.callerphone.channelpool.PoolResponse;
import com.marsss.callerphone.channelpool.PoolStatus;
import com.marsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class LeavePool implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        final Member MEMBER = e.getMember();

        if (ChannelPool.permissionCheck(MEMBER, e)) {
            return;
        }

        e.reply(leavePool(e.getChannel().getId())).setEphemeral(true).queue();
    }

    private String leavePool(String id) {
        PoolStatus stat = ChannelPool.leavePool(id);

        switch (stat) {
            case IS_HOST:
                return PoolResponse.ALREADY_HOSTING + "\n" +
                        String.format(PoolResponse.POOL_ID.toString(), id) + "\n" +
                        (ChannelPool.hasPassword(id)
                                ? String.format(PoolResponse.POOL_PWD.toString(), ChannelPool.getPassword(id))
                                : PoolResponse.POOL_SET_SETTINGS) + "\n" +
                        PoolResponse.POOL_END_WITH;
            case SUCCESS:
                return PoolResponse.LEAVE_POOL_SUCCESS.toString();
            case NOT_FOUND:
                return PoolResponse.NOT_IN_POOL.toString();
        }

        return Response.ERROR.toString();
    }

    @Override
    public String getHelp() {
        return "</leavepool:1075169067834548224> - Leave a channel pool.";
    }

    @Override
    public String[] getTriggers() {
        return "leavepool,exitpool".split(",");
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getTriggers()[0], getHelp().split(" - ")[1])
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL));
    }
}
