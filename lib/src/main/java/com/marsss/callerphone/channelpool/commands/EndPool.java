package com.marsss.callerphone.channelpool.commands;

import com.marsss.commandType.ISlashCommand;
import com.marsss.callerphone.Response;
import com.marsss.callerphone.channelpool.ChannelPool;
import com.marsss.callerphone.channelpool.PoolResponse;
import com.marsss.callerphone.channelpool.PoolStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class EndPool implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        final Member MEMBER = e.getMember();

        if (ChannelPool.permissionCheck(MEMBER, e)) {
            return;
        }

        e.reply(endPool(e.getChannel().getId())).setEphemeral(true).queue();
    }

    private String endPool(String id) {
        PoolStatus stat = ChannelPool.endPool(id);

        switch (stat) {
            case SUCCESS:
                return PoolResponse.END_POOL_SUCCESS.toString();
            case IS_CHILD:
                return PoolResponse.NOT_HOSTING.toString();
            case NOT_FOUND:
                return PoolResponse.NOT_IN_POOL.toString();
        }

        return Response.ERROR.toString();
    }

    @Override
    public String getHelp() {
        return "</endpool:1075169056811921468> - End a channel pool.";
    }

    @Override
    public String[] getTriggers() {
        return "endpool,stoppool".split(",");
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getTriggers()[0], getHelp().split(" - ")[1])
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL));
    }
}
