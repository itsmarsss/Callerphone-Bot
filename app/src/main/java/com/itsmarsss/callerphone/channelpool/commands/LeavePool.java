package com.itsmarsss.callerphone.channelpool.commands;

import com.itsmarsss.callerphone.Response;
import com.itsmarsss.callerphone.channelpool.ChannelPool;
import com.itsmarsss.callerphone.channelpool.PoolResponse;
import com.itsmarsss.callerphone.channelpool.PoolStatus;
import com.itsmarsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
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
    public String getName() {
        return "leavepool";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), getHelp().split(" - ")[1])
                .setContexts(InteractionContextType.GUILD)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL));
    }
}
