package com.marsss.callerphone.channelpool.commands;

import com.marsss.callerphone.Response;
import com.marsss.callerphone.ToolSet;
import com.marsss.callerphone.channelpool.ChannelPool;
import com.marsss.callerphone.channelpool.PoolResponse;
import com.marsss.callerphone.channelpool.PoolStatus;
import com.marsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class KickPool implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        final Member MEMBER = e.getMember();

        if (ChannelPool.permissionCheck(MEMBER, e)) {
            return;
        }

        e.reply(poolKick(e.getChannel().getId(), e.getOption("target").getAsString())).setEphemeral(true).queue();
    }

    private String poolKick(String requestID, String kickID) {
        PoolStatus stat = ChannelPool.removeChild(requestID, kickID);

        switch (stat) {
            case IS_CHILD:
                return PoolResponse.NOT_HOSTING.toString();
            case SUCCESS:
                final TextChannel CHILD_CHANNEL = ToolSet.getTextChannel(kickID);
                if (CHILD_CHANNEL != null) {
                    CHILD_CHANNEL.sendMessage(PoolResponse.KICKED_FROM_POOL.toString()).queue();
                }
                return String.format(PoolResponse.KICK_POOL_SUCCESS.toString(), kickID);
            case NOT_FOUND:
                return PoolResponse.REQUESTED_NOT_FOUND.toString();
        }

        return Response.ERROR.toString();
    }

    @Override
    public String getHelp() {
        return "</kickpool:1075169160692236418> - Kick channel from pool.";
    }

    @Override
    public String[] getTriggers() {
        return "kickpool,kickchannel,kickchan".split(",");
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getTriggers()[0], getHelp().split(" - ")[1])
                .addOptions(
                        new OptionData(OptionType.STRING, "target", "Target channel").setRequired(true)
                )
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL));
    }
}