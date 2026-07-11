package com.itsmarsss.callerphone.channelpool.commands;

import com.itsmarsss.callerphone.Response;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.channelpool.ChannelPool;
import com.itsmarsss.callerphone.channelpool.PoolResponse;
import com.itsmarsss.callerphone.channelpool.PoolStatus;
import com.itsmarsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class KickPool implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        if (ChannelPool.permissionCheck(e.getMember(), e)) {
            return;
        }

        OptionMapping target = e.getOption("target");
        if (target == null) {
            e.reply(Response.MISSING_PARAM.toString()).setEphemeral(true).queue();
            return;
        }

        e.reply(poolKick(e.getChannel().getId(), target.getAsString())).setEphemeral(true).queue();
    }

    private String poolKick(String requestId, String kickId) {
        PoolStatus stat = ChannelPool.removeChild(requestId, kickId);
        switch (stat) {
            case IS_CHILD:
                return PoolResponse.NOT_HOSTING.toString();
            case SUCCESS:
                TextChannel child = ToolSet.getTextChannel(kickId);
                if (child != null) {
                    child.sendMessage(PoolResponse.KICKED_FROM_POOL.toString()).queue();
                }
                return String.format(PoolResponse.KICK_POOL_SUCCESS.toString(), kickId);
            case NOT_FOUND:
                return PoolResponse.REQUESTED_NOT_FOUND.toString();
            default:
                return Response.ERROR.toString();
        }
    }

    @Override
    public String getHelp() {
        return "</kickpool:1075169160692236418> - Kick channel from pool.";
    }

    @Override
    public String getName() {
        return "kickpool";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Kick channel from pool")
                .addOptions(new OptionData(OptionType.STRING, "target", "Target channel ID").setRequired(true))
                .setContexts(InteractionContextType.GUILD)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL));
    }
}
